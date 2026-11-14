#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

: "${ANDROID_SDK_ROOT:?defina ANDROID_SDK_ROOT (pasta do Android SDK)}"
: "${ANDROID_NDK_HOME:?defina ANDROID_NDK_HOME (pasta do NDK r26)}"
: "${KS_PASS:?defina KS_PASS=senhaDoKeystore}"
BUILD="$ANDROID_SDK_ROOT/build-tools/34.0.0"

cmake -S native -B build/native \
  -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake" \
  -DANDROID_ABI=arm64-v8a \
  -DANDROID_PLATFORM=android-28 \
  -DCMAKE_BUILD_TYPE=Release
cmake --build build/native -j"$(nproc)"

mkdir -p app/src/main/assets/arm64-v8a
cp build/native/libffkit.so app/src/main/assets/arm64-v8a/libffkit.so

./gradlew :app:assembleRelease

APK=app/build/outputs/apk/release/app-release-unsigned.apk
mkdir -p dist
"$BUILD/zipalign" -f -p 4 "$APK" dist/ffkit.aligned.apk
"$BUILD/apksigner" sign \
  --ks keystore.jks --ks-key-alias ffkit \
  --ks-pass pass:$KS_PASS --key-pass pass:$KS_PASS \
  --out dist/ffkit.apk dist/ffkit.aligned.apk
rm dist/ffkit.aligned.apk
echo "[+] dist/ffkit.apk pronto. instala no celular: proximo, proximo, finalizar."
