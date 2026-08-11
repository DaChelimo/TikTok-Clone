# r2-upload Worker

Mints short-lived presigned upload URLs for Cloudflare R2, gated on a Firebase ID token.

## Why this exists

Video files live in R2 rather than Firebase Storage: R2 charges nothing for egress (the dominant
cost for a video feed) and does not force the Firebase project onto the Blaze billing plan.

R2 has no concept of a Firebase user, and the R2 credentials can't ship inside the APK, so
something server-side has to bridge the two. This Worker is that bridge, and it runs on the
Workers free tier — the same reason it isn't a Cloud Function, which would require Blaze.

## Flow

```
Android                     Worker                        R2
   │  POST /uploads                                        
   │  Authorization: Bearer <firebase id token>            
   ├───────────────────────────►│                          
   │                            │ verify token (JWKS)      
   │                            │ presign PUT videos/<uid>/<uuid>.mp4
   │  { uploadUrl, publicUrl }  │                          
   │◄───────────────────────────┤                          
   │                                                       
   │  PUT <uploadUrl>  (raw file bytes)                    
   ├──────────────────────────────────────────────────────►│
```

The object key is derived from the **verified** uid, never from the request body — a caller
cannot choose where its object lands or write into another user's prefix. The upload never
passes through the Worker, so neither the Workers request-body size limit nor double bandwidth
applies.

## Setup

Prerequisites: the R2 bucket and an R2 API token (Object Read & Write, scoped to that bucket).

1. **Install dependencies**

   ```bash
   npm install
   ```

2. **Fill in `wrangler.toml`** — replace the two `REPLACE_WITH_*` values:
   - `R2_ACCOUNT_ID` — Cloudflare account ID, shown on the R2 overview page.
   - `PUBLIC_BASE_URL` — the origin the bucket is served from. Prefer a custom domain
     (bucket → Settings → Custom Domains); `*.r2.dev` is rate-limited and dev-only.

   Also confirm `R2_BUCKET` matches your bucket name and `FIREBASE_PROJECT_ID` matches the
   project whose ID tokens should be accepted.

3. **Store the R2 credentials as secrets** (never in `wrangler.toml`):

   ```bash
   npx wrangler secret put R2_ACCESS_KEY_ID
   ```

   ```bash
   npx wrangler secret put R2_SECRET_ACCESS_KEY
   ```

4. **Deploy**

   ```bash
   npx wrangler deploy
   ```

5. **Point the app at it** — put the deployed URL in the repo-root `gradle.properties`:

   ```
   R2_WORKER_BASE_URL="https://tiktok-clone-r2-upload.<your-subdomain>.workers.dev"
   ```

   It reaches `R2VideoStorageRepo` via `BuildConfig.R2_WORKER_BASE_URL`.

## Local development

Put the two secrets in `.dev.vars` (gitignored), then:

```bash
npm run dev
```

Typecheck without deploying:

```bash
npm run typecheck
```

## Known gaps

- **No upload size cap.** A presigned PUT cannot enforce one; only a presigned POST policy can.
  Anyone holding a valid Firebase ID token can therefore upload an arbitrarily large object.
  Worth adding a lifecycle rule or moving to POST policies before this is public.
- **No cleanup of orphaned objects.** If the upload succeeds but the Realtime Database write
  fails, the object is left behind — the same accepted risk the Firebase Storage flow had
  (`PostVideoUseCase`).
