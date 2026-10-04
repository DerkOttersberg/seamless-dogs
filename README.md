# Seamless Dogs

Minimal MVP for **Minecraft Java 26.3 / Fabric**. Look at your own tamed wolf,
within three blocks, with an empty main hand, and press **G**. A small prompt
shows the rebound key. Petting lasts two seconds, with a one-second rest before
the next interaction. It never changes a sit command, health, items, or taming.

The hand makes a gentle stroke in first and third person; the wolf tilts its
head, wags its tail, and closes its eyes happily. Adult tame wolves also blink
at rest. The wolf's own sound variant plays a quiet pant **from the dog entity**,
so its voice and spatial position are preserved. The server accepts the request
and broadcasts it to nearby players, including observers who begin tracking
an active interaction.

Install the Fabric runtime jar with **Fabric Loader 0.19.5+, Fabric API
0.161.0+26.3, SeamlessLib 2.0.2+mc26.3 (Fabric), and Java 25**. Both server and
clients need the mod for petting. The prompt stays hidden on an unsupported
server. Install Mod Menu 21 optionally for the client settings screen. Key
bindings are under Options > Controls > Key Binds > Seamless Dogs. Visual
settings also live in `config/seamlessdogs-client.properties`.

## Deliberate MVP limits

- One Minecraft version and one loader. Forge and NeoForge are future adapters.
- Original procedural animation on the vanilla rig; no external animations,
  replacement wolf model, GeckoLib, or inverse kinematics. The hand motion is
  stylized and does not guarantee contact at every distance or camera angle.
- Happy eyes and idle blinking use the existing adult wolf UV layout. Puppies
  receive the petting/head/tail animation and their own sound, but keep their
  vanilla eyes. Eye edits derive from the current resource pack, with a fallback
  for unsupported dimensions. Packs using a different UV layout may disable
  eye expressions in settings.
- Petting stops if you move away, hold an item, enter combat, change dimension,
die, or disconnect. No gameplay bonuses or new registered world content.

## Build

The wrapper requires Java 25. The default composite points to the workspace's
verified 26.3 SeamlessLib checkout, not the older sibling 26.2 checkout:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25'
.\gradlew.bat clean check build
```

For a standalone checkout, clone SeamlessLib's `26.3` source alongside this
project and use `-PseamlessApiDir=../seamless-api`. The dependency stays a separate
mod jar. Runtime and source jars are in `fabric/build/libs/`; the common jar is
an internal build artifact. `check` runs unit tests, common isolation, metadata,
bytecode/shading checks, and four native Fabric GameTests.

See [PORTING.md](PORTING.md), [docs/ANIMATION.md](docs/ANIMATION.md), and
[docs/ACCEPTANCE.md](docs/ACCEPTANCE.md). All rights reserved, as requested.
