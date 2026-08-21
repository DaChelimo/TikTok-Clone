import { AwsClient } from 'aws4fetch'
import { createRemoteJWKSet, jwtVerify } from 'jose'

export interface Env {
  FIREBASE_PROJECT_ID: string
  R2_ACCOUNT_ID: string
  R2_BUCKET: string
  /** Origin the bucket is publicly served from — custom domain, or the r2.dev subdomain. */
  PUBLIC_BASE_URL: string
  R2_ACCESS_KEY_ID: string
  R2_SECRET_ACCESS_KEY: string
}

/**
 * Firebase signs ID tokens with rotating keys; this endpoint serves them as JWKs.
 *
 * Deliberately not the x509 endpoint the Admin SDK uses — WebCrypto's importKey has no
 * X.509 certificate format, so the JWK endpoint is the only one usable from a Worker.
 * Module scope so the fetched key set is cached for the lifetime of the isolate.
 */
const FIREBASE_JWKS = createRemoteJWKSet(
  new URL('https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com'),
)

/** Upload URL lifetime. Long enough for a slow mobile connection, short enough to limit replay. */
const PRESIGN_EXPIRY_SECONDS = 900

/**
 * Extensions are chosen from this map rather than from a client-supplied filename, so a
 * caller cannot steer the object key.
 */
const ALLOWED_CONTENT_TYPES: Record<string, string> = {
  'video/mp4': 'mp4',
  'video/quicktime': 'mov',
  'video/webm': 'webm',
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url)

    if (url.pathname !== '/uploads') return json({ error: 'not_found' }, 404)
    if (request.method !== 'POST') return json({ error: 'method_not_allowed' }, 405)

    const uid = await authenticate(request, env)
    if (!uid) return json({ error: 'unauthorized' }, 401)

    const contentType = await readContentType(request)
    const extension = ALLOWED_CONTENT_TYPES[contentType]
    if (!extension) return json({ error: 'unsupported_content_type', contentType }, 415)

    // uid comes from the verified token, never from the request body — that is what stops one
    // user writing into another user's prefix, and what makes a path-traversal key impossible.
    const objectKey = `videos/${uid}/${crypto.randomUUID()}.${extension}`
    const uploadUrl = await presignPut(objectKey, contentType, env)

    return json({
      uploadUrl,
      publicUrl: `${env.PUBLIC_BASE_URL.replace(/\/$/, '')}/${objectKey}`,
      objectKey,
      contentType,
      expiresInSeconds: PRESIGN_EXPIRY_SECONDS,
    })
  },
}

/** @return the Firebase uid, or null if the bearer token is missing or fails verification. */
async function authenticate(request: Request, env: Env): Promise<string | null> {
  const header = request.headers.get('Authorization') ?? ''
  const token = header.startsWith('Bearer ') ? header.slice('Bearer '.length).trim() : ''
  if (!token) return null

  try {
    const { payload } = await jwtVerify(token, FIREBASE_JWKS, {
      issuer: `https://securetoken.google.com/${env.FIREBASE_PROJECT_ID}`,
      audience: env.FIREBASE_PROJECT_ID,
      algorithms: ['RS256'],
    })

    // jose validates iss/aud/exp/nbf but knows nothing about auth_time, which Firebase's own
    // verification requires to be in the past. Without it a forward-dated token would pass.
    const authTime = payload.auth_time
    if (typeof authTime !== 'number' || authTime > Math.floor(Date.now() / 1000)) return null

    return typeof payload.sub === 'string' && payload.sub !== '' ? payload.sub : null
  } catch {
    return null
  }
}

async function readContentType(request: Request): Promise<string> {
  try {
    const body = (await request.json()) as { contentType?: unknown }
    return typeof body.contentType === 'string' ? body.contentType : 'video/mp4'
  } catch {
    return 'video/mp4'
  }
}

/**
 * Signs content-type into the query signature, so the object cannot be stored under a type
 * other than the one that was authorised. The client must send back the identical header.
 */
async function presignPut(objectKey: string, contentType: string, env: Env): Promise<string> {
  const aws = new AwsClient({
    accessKeyId: env.R2_ACCESS_KEY_ID,
    secretAccessKey: env.R2_SECRET_ACCESS_KEY,
    service: 's3',
    region: 'auto',
  })

  const target = new URL(
    `https://${env.R2_ACCOUNT_ID}.r2.cloudflarestorage.com/${env.R2_BUCKET}/${objectKey}`,
  )
  target.searchParams.set('X-Amz-Expires', String(PRESIGN_EXPIRY_SECONDS))

  const signed = await aws.sign(target, {
    method: 'PUT',
    headers: { 'content-type': contentType },
    aws: { signQuery: true },
  })

  return signed.url
}

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json' },
  })
}
