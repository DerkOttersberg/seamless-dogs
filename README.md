# Seamless Dogs

Pet your own tamed wolf: look at it within three blocks with an empty main hand
and press **G**. The prompt displays your rebound key. A two-second stroke moves
the hand in first and third person, tilts the dog's head, wags its tail, closes
its eyes happily, and plays a quiet pant from the dog. Tame adults and puppies
also blink at rest. Other players see the synchronized reaction.

This branch targets **Minecraft Java 26.2** with **Java 25**.
Choose exactly one matching loader jar and the same Minecraft/loader's
**SeamlessLib 2.0.1+mc26.2** (`seamlessapi`). Both clients and server need
Dogs and SeamlessLib for petting. Fabric also needs **Fabric API 0.159.0+26.2**.
Optional Fabric **Mod Menu 20.0.3** opens client settings; Forge/NeoForge
provide a native Mods settings entrypoint. Visual settings can also be changed
in `config/seamlessdogs-client.properties`.

| Loader | Verified build pin |
| --- | --- |
| Fabric | `0.19.5` |
| Forge | `26.2-65.1.3` |
| Neoforge | `26.2.0.75` |

The complete product targets seven version branches and 20 loader/version
combinations. See [compatibility and acceptance](docs/COMPATIBILITY.md) for
actual completed gates and [local evidence](docs/ACCEPTANCE.md). Beta loader
pins retain their upstream status. A successful build alone is not release acceptance.

Petting does not heal, tame, feed, or change the sitting command. It stops when
the player moves away, holds/uses an item, dies or changes dimension,
or disconnects, or the wolf becomes angry/acquires an attack target. The cooldown is three seconds from the beginning of the clip.
Servers authorize ownership, distance, visibility and activity; malformed,
unknown, duplicate and unauthorized requests cannot start a clip. No new world
content or persistent petting state is registered.

Motion uses original procedural animation on the vanilla rig. Hand contact is
stylized. Eye expressions preserve active texture-pack colors and support
scaled vanilla UVs; custom UV layouts can disable expressions. No game texture,
sound, or foreign rig is bundled. See [animation provenance](docs/ANIMATION.md).

## Build

Run Gradle with Java 25; matching Java 25 game toolchains are resolved
separately. Clone this Minecraft branch of SeamlessLib beside the product, then:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-25'
.\gradlew.bat clean check build -PseamlessApiDir=../seamless-api
```

The build runs unit tests, common isolation, metadata/refmap/bytecode checks,
and four discovered native scenarios on each loader. Gameplay jars are in each
loader's `build/libs/`. Common, development, sources and QA jars are not install
artifacts. SeamlessLib stays a separate dependency. Its verified local source
commit is `664efb8261545a170ebd966a784989fd1315d34f`; new library source ports have not been published remotely.

Background acceptance uses genuine installed servers/clients in private WSL
profiles, bounded resources and an exclusive Xvfb display lock. Two actual clients
verify remote rendering, dog sound, permissions, tracking and disconnect cleanup.
Separate lifecycle clients verify dimension change, death/respawn, menu input and
same-process reconnect. All 20 combinations passed local acceptance; see the
linked reports for exact hashes and limitations.
[PORTING.md](PORTING.md) describes the architecture. Product code/icon: **all rights
reserved**, as requested. SeamlessLib: MIT.
