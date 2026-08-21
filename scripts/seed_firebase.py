#!/usr/bin/env python3
"""Seeds the Realtime Database with three demo accounts and ten portrait videos.

Run against a development project only — it creates real Firebase Auth accounts that all
share one weak password, and it writes directly to the live database.

The tree it writes mirrors FirePath.kt exactly (users/<uid>/basic-data, videos/<id>,
users/<uid>/public-videos, tags/<tag>/..., taken-usernames/<name>); if those paths ever
change, this script has to change with them or the seeded data becomes unreachable.

Video files are hot-linked from Mixkit's CDN rather than uploaded, because the R2 bucket
isn't wired up yet. Every url below was verified as 720x1280 (9:16) before being embedded.

    python3 scripts/seed_firebase.py            # seed
    python3 scripts/seed_firebase.py --dry-run  # print what would be written
"""

import argparse
import json
import pathlib
import sys
import urllib.error
import urllib.request

GOOGLE_SERVICES = pathlib.Path(__file__).resolve().parent.parent / "app" / "google-services.json"

PASSWORD = "abc123"

# Characters Firebase rejects in a path key. Usernames become keys under taken-usernames/,
# so a username containing one of these cannot be stored at all.
ILLEGAL_KEY_CHARS = set('.$#[]/')

# (username, email, videos assigned by mixkit id)
#
# Usernames are deliberately dot-free: ValidateUsernameUseCase would accept "wave.rider", but
# claimUsername writes it as a path key and Firebase rejects that with HTTP 400. See
# assert_usable_as_key below — that mismatch is an app bug, not a constraint of this script.
ACCOUNTS = [
    ("waverider", "waverider@example.com", ["1164", "1166", "1194"]),
    ("bloomdaily", "bloomdaily@example.com", ["1168", "1173", "1186", "1187"]),
    ("goldenhour", "goldenhour@example.com", ["1170", "1191", "1188"]),
]

# duration is milliseconds — the unit DefaultLocalMediaRepo produces (MediaPlayer.duration)
# and TimeUtils.convertTimeToDisplayTime renders.
VIDEOS = {
    "1164": ("Deep water and nothing else. Could watch this loop all day #ocean #waves #calm", 20560, 4821, 51204),
    "1166": ("Flat calm, one mountain on the horizon #ocean #horizon #dusk", 24820, 3910, 42877),
    "1194": ("Found this little hut sitting right out on the water #lake #goldenhour #travel", 21290, 6402, 73115),
    "1168": ("Bougainvillea doing the absolute most against that blue #flowers #bougainvillea #spring", 20810, 5233, 60418),
    "1173": ("Pink blossom, silver leaves. Nature picked the palette #flowers #blossom #spring", 27610, 2874, 33902),
    "1186": ("Reaching for the sky #flowers #garden #summer", 20460, 4115, 47330),
    "1187": ("White blooms, blue sky, zero notes #flowers #white #garden", 23170, 3688, 41260),
    "1170": ("Palm silhouettes and a sun going down over the water #sunset #palms #goldenhour", 34330, 8940, 99512),
    "1191": ("One palm. One sunset. #sunset #palm #goldenhour", 22670, 7215, 82044),
    "1188": ("Looking straight up through the branches #trees #canopy #skyview", 15100, 2530, 29876),
}

VIDEO_URL = "https://assets.mixkit.co/videos/{vid}/{vid}-720.mp4"

# Staggered so the feed isn't one identical timestamp; epoch millis, oldest first.
BASE_TIMESTAMP_MS = 1754697600000
TIMESTAMP_STEP_MS = 3600_000


def load_config():
    cfg = json.loads(GOOGLE_SERVICES.read_text())
    return {
        "api_key": cfg["client"][0]["api_key"][0]["current_key"],
        "db_url": cfg["project_info"]["firebase_url"].rstrip("/"),
        "project_id": cfg["project_info"]["project_id"],
    }


def post_json(url, payload):
    req = urllib.request.Request(
        url,
        data=json.dumps(payload).encode(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=30) as r:
        return json.loads(r.read())


def db_put(db_url, path, value, dry_run):
    if dry_run:
        print(f"  PUT {path} = {json.dumps(value)[:110]}")
        return
    req = urllib.request.Request(
        f"{db_url}/{path}.json",
        data=json.dumps(value).encode(),
        headers={"Content-Type": "application/json"},
        method="PUT",
    )
    with urllib.request.urlopen(req, timeout=30) as r:
        r.read()


def create_account(api_key, email, password):
    """@return (uid, created) — an existing account is reused rather than treated as fatal."""
    url = f"https://identitytoolkit.googleapis.com/v1/accounts:signUp?key={api_key}"
    try:
        return post_json(url, {"email": email, "password": password, "returnSecureToken": True})["localId"], True
    except urllib.error.HTTPError as e:
        detail = json.loads(e.read()).get("error", {}).get("message", "")
        if detail != "EMAIL_EXISTS":
            raise
        signin = f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={api_key}"
        return post_json(signin, {"email": email, "password": password, "returnSecureToken": True})["localId"], False


def assert_usable_as_key(value, label):
    """Fails before any write, so a bad name can't leave half a seeded account behind."""
    bad = sorted(set(value) & ILLEGAL_KEY_CHARS)
    if bad:
        raise SystemExit(f"{label} {value!r} contains Firebase-illegal key characters: {''.join(bad)}")


def extract_tags(description):
    """Mirrors PostVideoUseCase.extractTags: keyed without the '#', valued with it."""
    return {w.lstrip("#"): w for w in description.split() if w.startswith("#")}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    cfg = load_config()
    print(f"project : {cfg['project_id']}")
    print(f"database: {cfg['db_url']}")
    print(f"mode    : {'DRY RUN' if args.dry_run else 'WRITING'}\n")

    for username, _, video_ids in ACCOUNTS:
        assert_usable_as_key(username, "username")
        for vid in video_ids:
            for tag in extract_tags(VIDEOS[vid][0]):
                assert_usable_as_key(tag, "tag")

    tag_counts = {}
    timestamp = BASE_TIMESTAMP_MS

    for username, email, video_ids in ACCOUNTS:
        if args.dry_run:
            uid, created = f"<uid-for-{username}>", True
        else:
            uid, created = create_account(cfg["api_key"], email, PASSWORD)
        print(f"{username} ({email}) -> {uid} {'[created]' if created else '[already existed]'}")

        total_likes = sum(VIDEOS[v][2] for v in video_ids)
        db_put(cfg["db_url"], f"users/{uid}/basic-data", {
            "username": username,
            "followers": 0,
            "following": 0,
            "totalLikes": total_likes,
            "profilePictureUrl": None,
            "uid": uid,
            "showLikedVideos": False,
        }, args.dry_run)
        db_put(cfg["db_url"], f"taken-usernames/{username}", username, args.dry_run)

        for vid in video_ids:
            description, duration_ms, likes, views = VIDEOS[vid]
            tags = extract_tags(description)
            video_id = f"seed-{vid}"

            db_put(cfg["db_url"], f"videos/{video_id}", {
                "url": VIDEO_URL.format(vid=vid),
                "description": description,
                "tags": tags,
                "duration": duration_ms,
                "videoId": video_id,
                "dateCreated": timestamp,
                "likes": likes,
                "views": views,
                "authorUid": uid,
                "totalCommentsSize": 0,
            }, args.dry_run)
            db_put(cfg["db_url"], f"users/{uid}/public-videos/{video_id}", video_id, args.dry_run)

            for tag in tags:
                tag_counts[tag] = tag_counts.get(tag, 0) + 1
                db_put(cfg["db_url"], f"tags/{tag}/tag-videos/{video_id}", video_id, args.dry_run)

            timestamp += TIMESTAMP_STEP_MS
            print(f"    {video_id}  {description[:58]}")
        print()

    for tag, count in sorted(tag_counts.items()):
        db_put(cfg["db_url"], f"tags/{tag}/info", {"name": tag, "count": count}, args.dry_run)
    print(f"tags written: {len(tag_counts)}")


if __name__ == "__main__":
    sys.exit(main())
