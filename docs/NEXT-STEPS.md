# Remaining work for 0.2.0

Work was stopped at the user's request before removal of the WSL Minecraft test
environment. The four branches preserve the current implementation and test
drivers. Six 26.2/26.3 cells pass local acceptance; five legacy cells are pending.
The mod is standalone, petting is rebindable, and paw-to-face grooming is removed.

1. Complete the interrupted 1.20.1 build of the new player pose restoration fix; output hashes from this interrupted attempt are not release artifacts.
2. Validate the new 1.20.1 and 1.21.1 player pose restoration in fresh packaged singleplayer, two-client, restart, lifecycle, integration and visual checks.
3. Repeat 1.20.1 native scenarios on the final gameplay bytes; previous repetition evidence belongs to the earlier build.
4. Complete the separate 1.21.1 NeoForge Embeddium profile.
5. Restore hosted CI after the GitHub account billing lock is resolved.
6. Prepare final install bundles only after all eleven acceptance cells pass. Publication remains a separate action.

Recreate an isolated runtime environment before resuming packaged tests; the old
Linux workspaces are being removed at the user's request. Evidence, failed runs
and reviewed captures remain on the Windows D drive. Do not use old-hash legacy
passes to claim readiness for the new player pose implementation.

No final install bundles or CurseForge/GitHub release publication were prepared.
Silent audio and software rendering retain the limitations recorded in the
acceptance record. Test drivers are excluded from gameplay JARs.
