# Desmond DJ Pro — Android install build

This is a native Android **early starter build** for Desmond DJ Pro. It is configured to compile an installable debug APK using GitHub Actions, so you can build it from your Android phone without a computer.

## Included in this build
- Two independent audio decks
- Load local audio files using Android's document picker
- Play/pause and seek
- Per-deck cue save and cue jump
- Crossfader controlling Deck A / Deck B output volume
- Pitch/speed adjustment where supported by Android's media codec
- Basic 3-band EQ where supported by the phone
- Dark Desmond DJ Pro branding

## Not included yet (do not expect these in this APK)
Advanced vinyl scratching, AI stem separation, STEMS FX, sampler banks, video mixing, Serato/Cross DJ switchable skins, waveform display, beat detection/sync, mix recording, and MIDI support are **not implemented in this early APK**. They require further development and testing.

## Build the APK using only your Android phone
1. Download this ZIP and extract it using your phone's Files app or an archive app.
2. Sign in to GitHub or create an account at https://github.com.
3. Create a repository named `desmond-dj-pro` (Private is fine).
4. Upload the **contents** of the extracted folder to the repository root. Ensure the `.github/workflows/build-apk.yml` file is included. If your phone hides `.github`, use GitHub's web interface to create that folder and workflow file.
5. Commit the files to the `main` branch. The workflow starts automatically.
6. In the repository, open **Actions**, select **Build Desmond DJ Pro APK**, and wait for a green check mark.
7. Open the completed run, find **Artifacts**, and download `Desmond-DJ-Pro-debug-apk`.
8. Extract the artifact ZIP. It contains `app-debug.apk`.
9. Tap `app-debug.apk` on your TECNO phone. If Android asks, allow your browser or Files app to install unknown apps, then tap Install.

## Technical details
- Android application ID: `com.pkdjpro.app` (kept stable for compatibility with earlier starter builds)
- Minimum Android: Android 8.0 (API 26)
- Compile SDK: 35
- Build: Gradle 8.9, Android Gradle Plugin 8.7.3, Kotlin 2.0.21

The ZIP is source code, **not an APK**. The APK is produced by the GitHub Actions build after you upload the source to a repository.
