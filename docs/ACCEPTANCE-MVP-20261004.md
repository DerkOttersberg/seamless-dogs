# Local MVP acceptance — 4 October 2026 (Europe/Amsterdam)

**Supported matrix:** Seamless Dogs `0.1.0+mc26.3`, Minecraft Java **26.3**,
**Fabric** only. Tested with Fabric Loader `0.19.5`, Fabric API
`0.161.0+26.3`, SeamlessLib `2.0.2+mc26.3`, and Java 25. License: all rights
reserved. No remote repository, push, PR, or publication was requested.

## Passed gates for the final runtime jar

| Gate | Result |
| --- | --- |
| `clean check build` (Gradle 9.6.0 / Windows Java 25) | PASS, exit 0; `build-acceptance.log` |
| Common unit tests | 2 executed; no failures or skips |
| Loader isolation, metadata, Java 25 bytecode, icon/assets, no shaded API/QA code | PASS via `verifyCommonIsolation` and `verifyLoaderJars` |
| Native Fabric GameTests | 4 named dog scenarios plus Fabric's baseline; all 5 required tests passed |
| Packaged dedicated server | PASS on WSL; final jars only, bind `127.0.0.1:25683`; booted, saved, and stopped cleanly |
| Standalone packaged client | PASS with only Dogs + SeamlessLib + Fabric API, plus the separate QA driver; GUI scale 2 |
| Combined packaged client | PASS with the four active gameplay siblings and Mod Menu 21; GUI scale 1 |
| Screenshot review | Prompt, actual hand/arm, adult eyes, third-person framing, left hand, and settings reviewed |

GameTests cover valid ownership, natural session completion, unchanged sitting
and health, negative/unknown targets, another owner's dog, occupied hands,
reach, walls, duplicate requests, cancellation, cooldown, disconnect cleanup,
and both packet codecs. Named success markers guard against undiscovered tests.

The client driver presses the production key mapping, observes real server
state, checks the completed rendered dog/player poses and unmodified sleeve
child rotations, and receives a real **entity-bound wolf pant packet** carrying
the dog's entity ID. It also observes idle blink and happy textures, checks the
occupied-hand path, reloads resources and confirms eye texture regeneration,
opens the real settings screen, captures six frames, and exits cleanly.

Combined siblings: Pretty Meteors with Trails `2.0.2+mc26.3`, Seamless Crafting,
Seamless Deconstructing Workbench, and Sword Throw `2.1.1+mc26.3`. These are
existing accepted Fabric runtime jars. Comfort and retired Block Animations
were excluded. This verifies petting coexistence; it does not rerun every
sibling's gameplay suite.

## Exact artifacts and evidence

Final Dogs runtime SHA-256:

```text
c268a89667f826c8d13b7d26a9b619d80404f7d574e1f5af765a0f3572ab1e54
```

Matching SeamlessLib runtime SHA-256:

```text
d57ae1d513e48b47f1b16bccd3f423ae6152d53566601ced750207e862f5fbc9
```

Library source: the clean `seamless-api` 26.3 checkout at
`8c5f419e0f1a2ff86167e65d06b7f79c0138843c`. It was not modified.

Workspace evidence is in `qa-artifacts/seamless-dogs/`: final build/test logs,
JUnit XML, `server-accepted.log`, `client-accepted/`, and `combined-accepted/`.
The two client directories contain pass markers, logs, six screenshots, and
the exact mod hashes. Earlier attempts remain preserved, including the initial
QA probe-order failure and prototype P-key conflict.

Install artifacts are in `release-candidates/seamless-dogs-mvp-mc26.3/`, with
SHA-256 and source/dependency pins in `artifacts.json`. The install zip includes
three runtime jars under `mods/`; common, sources, and QA jars are excluded.

## Environment and remaining limits

WSL Ubuntu 24.04, Java `25.0.4`, private Xvfb `:99`, software **Vulkan llvmpipe**
(Mesa `25.2.8`, LLVM `20.1.2`), two client CPUs, low priority, 1 GiB client heap,
and a bounded 600-second lifetime. The shared isolation lock was honored; no
desktop input or focus was taken. Initial software OpenGL could not create a
matching GLX context and fell back to Vulkan. Final accepted runs explicitly
used Vulkan. This is not hardware OpenGL/Vulkan acceptance.

OpenAL used the **No Output** device to keep background QA silent. Its sound
engine started and the dog-bound playback packet was verified; loudspeaker
audibility and perceived volume on the owner's hardware remain unchecked.
Offline Realms authentication, unavailable optional narrator/flite, and the
vanilla missing `minecraft:end_of_frame` diagnostic did not prevent the final
world/feature scenarios from completing. No product mixin or fatal render
failures occurred in accepted runs.

Two simultaneous real multiplayer clients, late-tracking/reconnect visual
behavior on a dedicated server, classic/slim skin combinations, wolf armor,
every wolf variant, custom UV layouts, and broad mod packs are not fully tested.
The architecture supports tracking synchronization; it is not claimed as full
multiplayer release acceptance. Puppies keep vanilla eyes. Animation is
stylized rather than exact hand-to-head IK. Hosted CI and public publication
have not run.
