# Remaining work for 0.2.0

The active scope is eleven cells: 1.20.1 Fabric/Forge, and 1.21.1, 26.2 and
26.3 Fabric/Forge/NeoForge. Paw-to-face grooming is removed everywhere in this
scope; chest cleaning remains. Dogs is standalone without SeamlessLib.

All eleven gameplay JARs pass builds, JAR guards and eight native scenarios.
The current hashes and exact packaged passes are in `acceptance-0.2.0.json`.
The release is **not production ready**. Pending entries are not failures or passes.

1. Resolve GitHub's account billing lock so branch CI jobs can actually start.
   The recorded failure occurred before any build step, and is separate from
   the successful local build/native tests.
2. Provide enough host disk headroom for the remaining isolated runtime tests.
   Owned test dispatchers were stopped after C fell below 2 GB free. Duplicate
   immutable cache JARs in the disposable Dogs WSL profiles were consolidated
   without altering their bytes. No user game/world or sibling source was changed.
3. Finish the exact-hash standalone singleplayer, dedicated save/restart, paired
   two-client and lifecycle matrix. For the new behaviors, verify settings and
   cooldown persistence through a genuine packaged save/restart, in addition to
   the native persistence checks and the existing packaged wolf-state restart.
4. Run matching renderer/protection builds and the combined Seamless suite.
   Verify actual own/foreign claim denial, optional integrations absent/present,
   active Iris/Oculus shader pipelines without fallback, and the separate
   1.21.1 NeoForge Embeddium case. Other protection systems remain unclaimed.
5. Exercise the newly compiled observer screenshot assertions. Review sequences
   for each port, first person, both third-person views, observers, both hands,
   classic/slim player skins, adults/babies, resource reloads and actual GUI scales.
   The 26.3 pet/stretches/knead/chest-cleaning captures have been inspected; full
   observer/UI/port visual acceptance remains pending. Silent audio tests verify
   entity-bound sound events; audible quality still needs interactive checking.
6. Freeze the final gameplay bytes, refresh acceptance/evidence and source links,
   then prepare the eleven install bundles using `scripts/prepare-local-release.py`.
   Its guard rejects incomplete acceptance. CurseForge publication is separate.

The maintained QA drivers live in `qa-client` and `qa-lifecycle`. Production JARs
exclude them. Packaged runner scripts are in `scripts`; run them only against
fresh, owned disposable profiles on private displays. Preserve failed attempts.

Private workspace checkpoint: `qa-artifacts/seamless-dogs/standalone-0.2.0`.
Build/native logs end in `playback1`; current observer QA builds use `observer2`
for 26.2/26.3 and `observer1` for 1.20.1/1.21.1. The earlier missing-import
capture-driver attempts are retained as failures; all eleven corrected drivers
compile. Private matrix dispatchers are stopped, so resumption must be explicit.
