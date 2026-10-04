#!/usr/bin/env bash
# Run only from Linux/WSL. Packaged gameplay jars, private copied world, no desktop input.
set -euo pipefail
repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
stage="$(realpath -m "${1:?Supply a NEW Linux staging directory}")"
fixture="$(realpath "${2:?Supply a disposable 26.3 QA world directory}")"
api_root="$(realpath "${3:-$repo_root/../.ports/github-mc26.3/seamless-api}")"
wrapper="${SEAMLESS_ISOLATED_WRAPPER:-$repo_root/../tools/Run-IsolatedMinecraftClient.sh}"
[[ -f "$fixture/level.dat" && -f "$wrapper" ]] || { echo 'Missing world fixture or private-display wrapper' >&2; exit 2; }
[[ ! -e "$stage" ]] || { echo 'Use a new staging directory; existing profiles are preserved' >&2; exit 2; }
dog_jar="$repo_root/fabric/build/libs/seamless-dogs-0.1.0+mc26.3-fabric.jar"
api_jar="$api_root/fabric/build/libs/seamless-api-2.0.2+mc26.3-fabric.jar"
[[ -f "$dog_jar" && -f "$api_jar" ]] || { echo 'Build the matching product and library jars first' >&2; exit 2; }
mkdir -p "$stage/product" "$stage/client/mods" "$stage/client/saves" "$stage/product/fabric/build/libs"
tar -C "$repo_root" --exclude=.git --exclude=.gradle --exclude=build -cf - . | tar -C "$stage/product" -xf -
cp "$dog_jar" "$stage/product/fabric/build/libs/"
cp "$dog_jar" "$api_jar" "$stage/client/mods/"
cp -a "$fixture" "$stage/client/saves/dogs-world"
cp "$repo_root/qa-client/options.txt" "$stage/client/options.txt"
curl --fail --location --retry 3 --silent --show-error \
  'https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.161.0+26.3/fabric-api-0.161.0+26.3.jar' \
  --output "$stage/client/mods/fabric-api-0.161.0+26.3.jar"
cp "$wrapper" "$stage/isolated-client.sh"
sed -i 's/\r$//' "$stage/isolated-client.sh" "$stage/product/gradlew"
chmod +x "$stage/isolated-client.sh" "$stage/product/gradlew"
cd "$stage/product"
ALSOFT_DRIVERS=null ISOLATED_CLIENT_TIMEOUT_SECONDS=600 \
  "$stage/isolated-client.sh" ./gradlew -p qa-client runClient \
  "-PqaRunDir=$stage/client" -PqaBackend=vulkan --no-daemon --console=plain \
  -Dorg.gradle.jvmargs=-Xmx2G -Dorg.gradle.workers.max=2 > "$stage/client-console.log" 2>&1
[[ -f "$stage/client/dogs-client-passed.txt" ]] || { echo "Client evidence missing; inspect $stage/client-console.log" >&2; exit 1; }
sha256sum "$stage/client/mods/"*.jar > "$stage/SHA256SUMS.txt"
cat "$stage/client/dogs-client-passed.txt"
