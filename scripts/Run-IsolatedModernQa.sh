#!/usr/bin/env bash
# Exact packaged gameplay jars in new private Linux profiles, one locked QA job at a time.
set -euo pipefail
repo="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
loader="${1:?fabric, forge or neoforge}"
stage="$(realpath -m "${2:?Fresh Linux staging path}")"
fixture="$(realpath "${3:?Disposable matching-version world}")"
api="$(realpath "${4:?Matching SeamlessLib checkout}")"
case "$loader" in fabric|forge|neoforge) ;; *) exit 2 ;; esac
[[ ! -e "$stage" && -f "$fixture/level.dat" ]] || { echo 'Need a new staging directory and a valid copied world' >&2; exit 2; }
wrapper="${SEAMLESS_ISOLATED_WRAPPER:-$repo/../tools/Run-IsolatedMinecraftClient.sh}"
dogjar="$repo/$loader/build/libs/seamless-dogs-0.1.0+mc26.2-$loader.jar"
apijar="$api/$loader/build/libs/seamless-api-2.0.1+mc26.2-$loader.jar"
[[ -f "$dogjar" && -f "$apijar" && -f "$wrapper" ]] || exit 2
mkdir -p "$stage/product/$loader/build/libs" "$stage/client/mods" "$stage/client/saves"
tar -C "$repo" --exclude=.git --exclude=.gradle --exclude=build -cf - . | tar -C "$stage/product" -xf -
cp "$dogjar" "$stage/product/$loader/build/libs/"
cp "$dogjar" "$apijar" "$stage/client/mods/"
cp -a "$fixture" "$stage/client/saves/dogs-world"
cp "$repo/qa-client/options.txt" "$stage/client/options.txt"
cp "$repo/common/src/main/resources/pack.mcmeta" "$stage/product/qa-client/src/main/resources/pack.mcmeta"
if [[ "$loader" == fabric ]]; then
    curl --fail --location --retry 3 --silent --show-error \
        'https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.159.0+26.2/fabric-api-0.159.0+26.2.jar' \
        --output "$stage/client/mods/fabric-api-0.159.0+26.2.jar"
fi
cp "$wrapper" "$stage/isolated-client.sh"
sed -i 's/\r$//' "$stage/isolated-client.sh" "$stage/product/gradlew"
chmod +x "$stage/isolated-client.sh" "$stage/product/gradlew"
sha256sum "$stage/client/mods/"*.jar > "$stage/SHA256SUMS.before.txt"
cd "$stage/product"
# Waiting for the shared lock is outside the client wrapper; the wrapper rechecks it atomically.
flock --wait 50 /tmp/seamless-isolated-minecraft.lock true
ALSOFT_DRIVERS=null ISOLATED_CLIENT_TIMEOUT_SECONDS=900 \
    "$stage/isolated-client.sh" ./gradlew -p qa-client runClient \
    "-PqaLoader=$loader" "-PqaRunDir=$stage/client" -PqaBackend=vulkan --no-daemon --console=plain \
    -Dorg.gradle.jvmargs=-Xmx2G -Dorg.gradle.workers.max=2 > "$stage/client-console.log" 2>&1
[[ -f "$stage/client/dogs-client-passed.txt" && ! -e "$stage/client/dogs-client-failed.txt" ]] || exit 1
sha256sum --check "$stage/SHA256SUMS.before.txt"
cat "$stage/client/dogs-client-passed.txt"
