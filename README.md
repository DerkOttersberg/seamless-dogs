# Seamless Dogs

Version **0.2.0** adds cats, kittens, occasional dog digging and pet idle expressions.
Look at your own tamed pet within three blocks, hold an empty main hand and press
**G**. The contextual prompt shows the key you assigned. Other players see the
reaction, and the pet’s own voice follows the animal. Petting preserves ownership,
health and sitting commands. Dogs and cats blink; relaxed pets lean into the stroke.

Calm adult dogs attempt a four-second dig every **10–20 minutes**. Standing cats
and kittens choose a stretch, biscuit kneading or grooming every **3–6 minutes**.
Biscuits last six seconds with alternating paw presses. Chest cleaning also lasts
six seconds, with grounded paws, gentle head motion, closed eyes and quiet purring. Idle actions need a grounded, stationary
pet within 16 blocks of its online owner. Sitting, sleeping, combat, damage,
swimming, riding and following the owner prevent them. Petting interrupts idle
actions with a brief pose recovery. Tracking updates preserve animation progress.
Interrupted attempts keep their cooldown.

Calm dogs and puppies can tilt their head, tuck one ear and look back when their
owner watches them within six blocks. These brief reactions have a **1–2 minute**
cooldown, require visibility, and also work while seated. The “Pet idle expressions”
server switch controls stretches, biscuits, grooming and these gaze reactions.
Seamless Dogs is standalone: SeamlessLib is neither required nor bundled.
The 0.2.0 release effort covers the four version branches listed below.
Fresh acceptance is recorded per loader and final JAR hash.

A completed dig removes one exposed eligible dirt or sand block. Plants, fluids,
block entities, farmland, paths and unsafe support positions are excluded.
Most digs yield only that block’s normal drop. Additional finds are 4% stick/bone/
flint, 0.8% coal/raw copper/raw iron and 0.2% enchanted book. Books can include
Mending and other treasure enchantments; default books exclude curses. Each owner
is limited to one successful dig per ten minutes across all their dogs. Block tags,
loot and enchantment eligibility can be changed by datapacks.

The settings screen has **Client visuals**, **My pets** and **World/server** pages.
An owner can turn off digging for all their dogs in that world. The singleplayer
host or a dedicated-server administrator with permission level 2 can change
all three server rules. Visuals are local. Save applies the displayed page;
Cancel discards its draft. The settings key starts unbound. Forge/NeoForge provide
native Mods entries; Fabric supports optional Mod Menu. See
[settings and datapacks](docs/CONFIGURATION.md).

This branch targets **Minecraft Java 1.21.1**, **Java 21** and requires Dogs on both client and server.
Fabric also requires **Fabric API 0.116.17+1.21.1**. Choose exactly one loader JAR.

| Loader | Build pin |
| --- | --- |
| Fabric | `0.19.5` |
| Forge | `1.21.1-52.1.16` |
| NeoForge | `21.1.255` |

The active 0.2.0 release matrix has **11 loader/version cells**: 1.20.1 Fabric/Forge;
1.21.1, 26.2 and 26.3 Fabric/Forge/NeoForge. Older local branches are retained as history.
**The older 0.1.0 acceptance does not establish readiness of 0.2.0.** Fresh feature
evidence and final hashes must pass before release; see
[0.2.0 acceptance](docs/0.2.0-ACCEPTANCE.md). Upstream beta loader pins keep that status.
Publication is a separate action.

The server authorizes petting, selects idle actions and generates terrain changes
and loot. Clients render negotiated action clips. Old dog-petting clients retain
the original protocol; new idle behaviors require the owner’s new capability.
Packets reject unauthorized, malformed, stale and excessive requests. World rules,
owner preferences and cooldowns use world-local atomic saves/backups.

Digging respects `mobGriefing`, spawn protection and cancellable native breaking
hooks. Optional Open Parties and Claims and Fabric Common Protection API adapters
exclude detected claims, including the owner’s own. Unqueryable supported
integrations disable terrain changes and explain their status in settings.
FTB Chunks queries exclude every claim on matching loader builds. Unsupported
FTB builds pause terrain changes safely; exact tested pins appear in the acceptance record. Other protection systems need verified adapters before claiming
claim-aware support. See [compatibility](docs/COMPATIBILITY.md) for exact evidence.

Rendering adds original poses to vanilla rigs, retaining skins, collars and
resource-pack textures. Unsupported detected models/layouts fall back; affected
visuals can be disabled. The [Blockbench authoring sources](animation-source/README.md)
and [animation provenance](docs/ANIMATION.md) explain the original clips. No game
texture, audio, downloaded animation or foreign rig is bundled.

Build this repository independently with a Java 25 Gradle host (game toolchains are selected per branch):

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-25'
.\gradlew.bat clean check build
```

Native tests exercise ownership, codecs, cancellation, terrain, permissions,
scheduling, protection denial, persistence and single bonus-loot generation.
Background acceptance uses genuine packaged singleplayer/dedicated games and two
clients on private WSL displays. Test-only fixture JARs never enter install bundles.
See [PORTING.md](PORTING.md) for common code and explicit loader adapter boundaries.
Seamless Dogs code and original assets: **All Rights Reserved**.

The latest 1.21.1 Fabric review preview fixes disappearing cat eyes, adds left-paw
washing and chest cleaning alongside the original grooming clip, and refines
kneading and digging. Server-selected grooming variants synchronize to observers
and late trackers. Hand withdrawal finishes the stroke, preserves fractional
frames on interruption and uses a steady client clock. Kneading keeps the
shoulders beneath the coat. Face grooming uses the exact original adult/kitten
animation, with a mirrored left-paw wash and the existing chest variant.
Digging plays the target
block's hit sound from the dog on alternating paw contacts. The approved animations have been ported to the active four branches; see
`docs/0.2.0-ACCEPTANCE.md` for the exact validated bytes and remaining release gates.

Source and issues: [DerkOttersberg/seamless-dogs](https://github.com/DerkOttersberg/seamless-dogs).
Use the matching [1.20.1](https://github.com/DerkOttersberg/seamless-dogs/tree/1.20.1),
[1.21.1](https://github.com/DerkOttersberg/seamless-dogs/tree/1.21.1),
[26.2](https://github.com/DerkOttersberg/seamless-dogs/tree/26.2) or
[26.3](https://github.com/DerkOttersberg/seamless-dogs/tree/26.3) branch.
