# Compatibility and production acceptance

All rights reserved. No build is promoted to production acceptance from compilation alone.
Each Minecraft branch contains all supported loaders, with an independent matching SeamlessLib jar.

| Minecraft | Fabric | Forge | NeoForge |
|---|---|---|---|
| 1.20.1 | planned | planned | excluded: upstream ended support |
| 1.21.1 | planned | planned | planned |
| 1.21.11 | planned | planned | planned |
| 26.1 | planned | planned | planned |
| 26.1.2 | planned | planned | planned |
| 26.2 | planned | planned | planned |
| 26.3 | MVP accepted; production work in progress | in progress | in progress |

Evidence gates for every cell: clean build, executed unit/native GameTests, jar and dependency
inspection, packaged dedicated server save/restart, isolated feature client, combined suite when
matching sibling ports exist, and multiplayer lifecycle coverage. Record unsupported or unavailable
integration environments explicitly. Background clients use WSL private Xvfb, bounded resources,
and the shared exclusive client lock. Test drivers and copied worlds never enter release jars.

Pins were verified against the official Fabric, Forge and NeoForge Maven repositories on
2026-10-05. Newest pins are not automatically compatible with existing accepted suite jars.
1.21.1 and 26.1.2 are established NeoForge targets; 26.1 base and 26.3 currently have beta NeoForge
builds. Calling the mod production ready cannot make the underlying beta loader stable.

Upstream references: [NeoForge 26.1](https://neoforged.net/news/26.1release/),
[Minecraft 26.1.2](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-1-2),
[NeoForge support history](https://neoforged.net/news/2024-retrospection/).
