# Releases

The Android Release workflow follows Habit Diary's release process. Pushing a
`vMAJOR.MINOR.PATCH` tag builds a signed APK and AAB, uploads the AAB to the Google
Play production track, and creates a GitHub release containing the APK. There is
no beta release path.

The tag supplies `versionName`; the workflow run number plus one supplies
`versionCode`. Code `1` is reserved for the first manual Play upload. Local
builds default to version `0.0.1`, code `1`. Keep automated version codes ahead
of any additional versions uploaded manually to Play.

Before the first automated release, create Unloop in Play Console, upload its
first bundle through the console, complete the required app setup, and grant the
configured service account release access to `com.charan.unloop`. Verify access
with `playconsole-cli tracks list --package com.charan.unloop`.

Update `app/src/main/assets/CHANGELOG.md` before tagging. The workflow uses it for
GitHub release notes and the first 500 bytes of the English Play release notes.

Required repository Actions secrets:

- `KEYSTORE_BASE64`: base64-encoded Unloop upload keystore.
- `KEYSTORE_PASSWORD`: keystore password.
- `KEY_ALIAS`: signing key alias.
- `KEY_PASSWORD`: signing key password.
- `GOOGLE_SERVICES_JSON_BASE64`: base64-encoded release Firebase configuration.
- `SERVICE_ACCOUNT_JSON`: Google Play service-account JSON credentials.

Signing files and credentials stay out of Git. The workflow restores them for
the build and removes them when the job finishes.

After Play setup is complete, release with:

```sh
git tag v0.0.1
git push origin v0.0.1
```
