# Test-only lifecycle driver

Build the gameplay modules first. Build this separate harness for the selected
loader with `./gradlew -p qa-lifecycle clean build -PqaLoader=fabric` (or forge /
neoforge where applicable). Copy its packaged runtime JAR to `artifacts/` for
`scripts/run-lifecycle-client.py`. The harness uses a fresh loopback server and
private display to exercise dimension transfer, death/respawn, menu requests and
active disconnect/reconnect for dog petting and cat chest grooming.
The cat also crosses dimensions and dies/respawns during an active idle clip. It must never enter gameplay installation bundles.
