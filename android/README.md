# SafeTrust Android — Phase 2-4

Native Kotlin Android companion for the existing SafeTrust device authentication boundary.

## Scope

Authentication/connectivity only. No location, contacts, calls, SMS, microphone, camera, gallery/files, accessibility, device-admin, VPN interception, usage access, monitoring notifications, background collectors, or telemetry.

## Tooling

- Android Gradle Plugin 9.4.0
- Gradle 9.6.0
- JDK 17
- Kotlin via AGP 9 built-in Kotlin support
- Android API 33+ because the standard Ed25519 parameter API is available from API 33.

## Cryptographic identity

The device private key is generated locally in Android Keystore using Ed25519 and is never exported. The corresponding public key is converted from the X.509 SubjectPublicKeyInfo representation to the 32-byte Ed25519 public-key form expected by SafeTrust.

## Protocol

1. A user creates/enrolls a SafeTrust device through the existing user-authenticated control plane.
2. The user creates a 5-minute, single-use pairing code for that device.
3. Android generates its Ed25519 key locally and sends only the public key plus pairing code.
4. Android requests `/api/devices/:deviceId/challenge`.
5. Android signs `SafeTrust.DeviceAuth.v1\n<device_id>\n<challenge_id>\n<nonce>`.
6. Android posts the signature to `/api/devices/:deviceId/authenticate`.
7. Android stores the returned 15-minute device-session credential using an AES-GCM key held in Android Keystore.
8. Android verifies it through `/api/device-session/check`.

The device session is not a SafeTrust user login and is not accepted by normal user-authenticated endpoints.

## Pairing bootstrap

The existing key-registration endpoint requires a signed-in SafeTrust user. A native client must not emulate that with a copied browser session cookie. Phase 2-4 therefore adds a narrow bootstrap: a signed-in user creates a short-lived pairing code; Android presents it with its public key; the server consumes the code and binds the key to that exact device. No permanent pairing credential is created.

## Play Integrity readiness

Phase 2-6B.5C integrates the Standard Play Integrity API client and the SafeTrust request-hash/submission boundary. A real Cloud project number and server verification credentials are required before genuine end-to-end verification can be performed.

CI builds and runs the Android unit tests on Android-only changes.
