# Compatibility and local production acceptance

The product uses one Minecraft branch per line with native loader modules and
an independent matching SeamlessLib jar. All rights reserved for Dogs; MIT for
SeamlessLib. No runtime Architectury or animation-library dependency is required.

| Minecraft | Java | Fabric | Forge | NeoForge |
| --- | --- | --- | --- | --- |
| 1.20.1 | 17 | client + multiplayer passed | client + multiplayer passed | excluded: upstream ended support |
| 1.21.1 | 21 | client checks running | client checks pending | client checks pending |
| 1.21.11 | 21 | client checks pending | client checks pending | client checks pending |
| 26.1 | 25 | client checks pending | client checks pending | client checks pending; beta loader |
| 26.1.2 | 25 | client checks pending | client checks pending | client checks pending |
| 26.2 | 25 | client checks pending | client checks pending | client checks pending |
| 26.3 | 25 | client + multiplayer passed | client + multiplayer passed | client + multiplayer passed; beta loader |

All 20 cells passed clean builds, unit/native scenarios and jar/refmap/bytecode
inspection, plus genuine official dedicated servers with save/restart. The older
wolf timing fix was rebuilt and its server gates repeated on the new exact jars.
Final client/combined/multiplayer coverage is still being completed; this table
is interim and does not claim every cell is production accepted yet.

Acceptance requires real feature actions, frozen hashes, named scenario markers,
standalone and available combined profiles, two actual multiplayer clients,
multiple GUI scales, resource reload, and optional settings integration. Background
clients use private WSL Xvfb with bounded resources and the shared exclusive lock.
Test drivers and copied worlds never enter release jars.

Pins were verified against official Maven repositories on 5 October 2026.
NeoForge 26.1 base and 26.3 currently use upstream beta builds. Local software
OpenGL/Vulkan coverage does not establish all hardware or arbitrary mod-pack
compatibility. New matching library source ports and hosted CI are local work,
not published releases. Publishing requires a separate explicit request.

Upstream references: [NeoForge 26.1](https://neoforged.net/news/26.1release/),
[Minecraft 26.1.2](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-1-2),
[NeoForge support history](https://neoforged.net/news/2024-retrospection/).
