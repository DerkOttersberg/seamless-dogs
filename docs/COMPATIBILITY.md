# Compatibility and local acceptance — 5 October 2026

All 20 combinations below passed the full local acceptance workflow on frozen
Dogs and matching independent SeamlessLib jars. Each branch contains loader-neutral
common code and native adapters; no runtime Architectury or animation library is
required. Dogs is all rights reserved. SeamlessLib remains MIT.

| Minecraft | Java | Fabric | Forge | NeoForge |
| --- | --- | --- | --- | --- |
| 1.20.1 | 17 | 0.19.5 — passed | 1.20.1-47.4.26 — passed | excluded: support ended |
| 1.21.1 | 21 | 0.19.5 — passed | 1.21.1-52.1.16 — passed | 21.1.255 — passed |
| 1.21.11 | 21 | 0.19.5 — passed | 1.21.11-61.2.1 — passed | 21.11.45 — passed |
| 26.1 | 25 | 0.19.5 — passed | 26.1-62.0.9 — passed | 26.1.0.19-beta — passed |
| 26.1.2 | 25 | 0.19.5 — passed | 26.1.2-64.1.3 — passed | 26.1.2.114 — passed |
| 26.2 | 25 | 0.19.5 — passed | 26.2-65.1.3 — passed | 26.2.0.75 — passed |
| 26.3 | 25 | 0.19.5 — passed | 26.3-66.0.9 — passed | 26.3.0.48-beta — passed |

For every cell: clean builds, unit tests, four explicitly discovered native
scenarios, metadata/refmap/bytecode/no-shading checks, genuine packaged dedicated
server save/restart, a standalone installed client, two real multiplayer clients,
and a separate genuine lifecycle client passed. Gameplay and tests used Java 17
on 1.20.1, Java 21 on 1.21.x, and Java 25 on 26.x. Gradle uses Java 25.

Real feature actions covered the rebindable key and prompt; first/third/left-hand
animation; adult/puppy rigs and expressive eyes; idle blink; entity-bound pant;
ownership and invalid requests; occupied-hand rejection; cooldown and return to
vanilla pose; resource reload; GUI scales 2 and 3; remote rendering; late tracking;
active disconnect; dimension change; death/respawn; menu input; and reconnect in
the same client process followed by another successful pet. Native wounded-wolf
scenarios confirm no healing and unchanged sitting commands.

All seven Fabric versions passed the real Mod Menu config entrypoint. Available
combined installations passed on 26.3 Fabric/Forge/NeoForge and 1.20.1 Fabric/Forge
with Seamless Crafting, Seamless Meteors, Seamless Throw Weapons and Seamless
Workbench. Those are five combined profiles, in addition to standalone coverage.
Other matching sibling suites have not been claimed as tested. Retired Block
Animations and experimental Comfort were excluded.
Combined profiles exercised Dogs with those siblings installed; they do not
replace the siblings' own full feature acceptance.

Tests ran in private WSL Ubuntu 24.04 Xvfb profiles with an exclusive display lock,
two CPUs, 1 GiB heap per process, low priority and bounded time. Software OpenGL
covered 1.20/1.21; software Vulkan covered 26.x. A silent audio output verified
real sound packets bound to the dog, rather than loudspeaker audibility. These
checks do not establish every hardware driver, custom-UV resource pack, mod pack,
wolf variant/armor and classic/slim skin combination, or every lifecycle failure
and latency permutation. Hand contact is stylized.
Clean builds and development-native scenarios ran on Windows; Forge 1.21.11's
separate official native scenario server also ran on Windows. The installed
server save/restart, feature, multiplayer and lifecycle matrices ran in WSL.

NeoForge 26.1 and 26.3 pins are upstream betas and retain that status. NeoForge
1.20.1 is excluded because upstream ended support. Forge 1.21.11 native scenarios
ran on the genuine installed server: Loom's joined development patches currently
have inconsistent anonymous codec classes. QA helpers never ship in gameplay jars.

Exact source/dependency commits, hashes and profile paths are recorded in
[acceptance JSON](acceptance-20261005.json), [jar checksums](SHA256SUMS-20261005.txt)
and [the acceptance report](ACCEPTANCE.md). Failed attempts are retained.
Install bundles are local release candidates. No remote push, public publication
or hosted CI is claimed; unpublished matching library ports must be made available
before their pinned hosted builds can run.

Official upstream context: [NeoForge 26.1](https://neoforged.net/news/26.1release/),
[Minecraft 26.1.2](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-1-2),
[NeoForge support history](https://neoforged.net/news/2024-retrospection/).
