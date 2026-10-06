# Packaged feature driver

Build gameplay first, then `./gradlew -p qa-client clean build -PqaLoader=fabric`
(or forge/neoforge when supported). Copy the resulting packaged test JAR to
`qa-client/artifacts/` for the private WSL helpers. This module must never enter
release bundles. Its `dogsqa` commands create disposable fixtures and accelerate
rare actions; production Dogs has no such commands.

The driver exercises real keybinds, server authority, eyes, hands, entity sounds,
settings, resource reload, adult/baby petting, stretch, kneading, chest cleaning,
head tilt and grass/sand digging. Both paw-to-face washing variants are retired.
The observer rejects unauthorized requests and checks late tracking and owner
logout. Optional runs query real OpenPAC/FTB/CPAPI claims and open Mod Menu.
Shader acceptance also checks that Iris/Oculus loaded the supplied test pack,
created its pipeline and did not report compilation/fallback failure.

`ClipContinuityProbe` replays delayed same-sequence dog, cat and chest-cleaning
packets through the production handler. It also checks the pose at an actual
G-key idle-to-pet transition and requires the outgoing pose to be preserved.
