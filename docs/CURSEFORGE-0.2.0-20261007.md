# CurseForge submission record: 0.2.0

All eleven final gameplay JARs were submitted through the integrated browser on
7 October 2026 to **Good Boy: Better Pets**, project **1730435**, author Seamlessmods.
Each completed transfer reached **Under Review**. Publication is automatic once
approved. The new project also awaits moderator approval; the public files page
returned 404 and the preview Download button was disabled at this check. This
record establishes submission and review status, not public availability.

| Minecraft | Loader | Type | CurseForge file | Observed status |
| --- | --- | --- | --- | --- |
| 1.20.1 | Fabric | Release | [9088340](https://authors.curseforge.com/#/projects/1730435/files/9088340) | Under Review |
| 1.20.1 | Forge | Release | [9088353](https://authors.curseforge.com/#/projects/1730435/files/9088353) | Under Review |
| 1.21.1 | Fabric | Release | [9088377](https://authors.curseforge.com/#/projects/1730435/files/9088377) | Under Review |
| 1.21.1 | Forge | Release | [9088382](https://authors.curseforge.com/#/projects/1730435/files/9088382) | Under Review |
| 1.21.1 | NeoForge | Release | [9088387](https://authors.curseforge.com/#/projects/1730435/files/9088387) | Under Review |
| 26.2 | Fabric | Release | [9088390](https://authors.curseforge.com/#/projects/1730435/files/9088390) | Under Review |
| 26.2 | Forge | Release | [9088394](https://authors.curseforge.com/#/projects/1730435/files/9088394) | Under Review |
| 26.2 | NeoForge | Release | [9088396](https://authors.curseforge.com/#/projects/1730435/files/9088396) | Under Review |
| 26.3 | Fabric | Release | [9088399](https://authors.curseforge.com/#/projects/1730435/files/9088399) | Under Review |
| 26.3 | Forge | Beta | [9088400](https://authors.curseforge.com/#/projects/1730435/files/9088400) | Under Review |
| 26.3 | NeoForge | Beta | [9088402](https://authors.curseforge.com/#/projects/1730435/files/9088402) | Under Review |

The [JSON receipt](curseforge-0.2.0-20261007.json) records full SHA-256 hashes,
per-file timestamps in UTC, Java and loader versions, exact changelogs,
dependencies, source commits and hashes of form/saved-metadata evidence. The
upload source snapshot includes final acceptance documentation; the accepted
build source commit is recorded separately. Gameplay bytes did not change.

Every file is tagged for its exact Minecraft version and one loader, with Client
and Server selected. Fabric files require Fabric API and list Mod Menu as
optional. Forge and NeoForge files have no additional library dependency.
SeamlessLib is neither required nor bundled. No QA, development, source or game
library JAR was uploaded. The 26.3 Forge and NeoForge files remain Beta because
their accepted upstream loader pins are beta; the other nine files are Release.

The project retains All Rights Reserved, its existing description and media.
Its Source tab now links the existing public repository:
https://github.com/DerkOttersberg/seamless-dogs.

All eleven cells pass [local acceptance](0.2.0-ACCEPTANCE.md), including packaged
singleplayer and dedicated-server tests with two clients. Exact optional mod
combinations and software-rendering/silent-audio limitations remain documented.
Hosted CI could not run because of GitHub's account billing lock; no hosted CI
pass is claimed. Public moderation remains the external release step.

Temporary Linux Minecraft caches were removed after testing (1,799,809,465
bytes). Ubuntu-24.04 and docker-desktop were verified Stopped after WSL shutdown.
Windows sources, worlds, evidence and install bundles were preserved. WSL was
shut down, not permanently disabled. The cleanup receipt is included in the JSON.
