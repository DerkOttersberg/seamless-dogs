# Historical 0.1.0 acceptance

This record applies to the earlier 0.1.0 builds. See [current 0.2.0 acceptance](0.2.0-ACCEPTANCE.md) for the standalone eleven-build release.

# Local acceptance — 5 October 2026

**Passed: 20/20 feature cells, 20/20 lifecycle cells, seven Mod Menu profiles and
five available combined-suite profiles.** The two combined Fabric profiles also
include Mod Menu, so the supplemental optional/combined total is ten profiles.
All accepted product/dependency jar hashes match the final build outputs.

The machine-readable [acceptance record](acceptance-20261005.json) identifies
each Minecraft/loader, both runtime SHA-256 values, source/dependency commits,
unit results, named native scenarios and accepted Linux profiles. The complete
logs, screenshots, server save/restart comparisons and pass markers are retained
at `C:/Users/derko/Desktop/minecraft/qa-artifacts/seamless-dogs/production-accepted`.
Original isolated installations and failed attempts remain in
`/root/seamless-dogs-production-20261005` and the workspace QA evidence directory.
Game assets, libraries, worlds and test drivers are excluded from install bundles.

The final clean/native evidence includes the legacy wolf frame-delta correction,
1.21.1 HUD signature and API pack-format fixes, native Forge HUD handling, and
jar guards against shipping intermediary development output. Genuine clients
found these regressions; acceptance was repeated against corrected runtime bytes.
The final log audit also corrected 1.21.11's shared client/server pack metadata;
both actual Minecraft codecs decode the resource file in regression tests.
The all-cell screenshot review caught 1.21.1's background blur affecting settings
labels. Its screen now renders the background before widgets and labels, and
all three affected builds repeated clean, installed-server, feature, multiplayer,
lifecycle checks against the corrected jar hashes. Fabric repeated its Mod Menu
entrypoint check as well. The original screenshots remain in the earlier profiles.
The lifecycle fixture's loading-screen timing and command registration corrections
are test-driver fixes. Failed attempts were preserved rather than removed.

Native scenarios on every loader are `ownerCanPet`, `rejectInvalidRequests`,
`cooldownAndCancellation` and `codecRoundTrip`. Their individual pass markers and
required-test result are checked, preventing undiscovered scenarios from passing.
Forge 1.21.11 uses genuine installed native scenarios as described in PORTING.md.
Persistence fixtures retain owner, UUID, sitting and health after save/restart;
1.20.1's vanilla tame-wolf loading restores its baseline health, and the wounded
live native scenario independently confirms petting never heals.

Reproduction helpers are `scripts/Verify-VersionBranches.ps1`,
`scripts/Build-ClientProbes.ps1`, `scripts/Build-LifecycleProbes.ps1`,
`scripts/run-packaged-server.py`, `scripts/run-packaged-client.py`,
`scripts/run-paired-clients.py` and `scripts/run-lifecycle-client.py`.
Lifecycle generation uses a separate `.qa/lifecycle` source project and artifact
directory; it cannot overwrite the feature-test or gameplay output. Each actual
run needs a fresh owned staging profile. The release helper rejects incomplete
acceptance, changed jar hashes and test code in gameplay jars.

See [COMPATIBILITY.md](COMPATIBILITY.md) for the matrix and practical limits.
This is local acceptance, not a public release or a passing hosted CI run.
Offline account/Realms requests, the unavailable Linux narrator library, Xvfb
cursor shapes, loader Netty kqueue/epoll debug-appender diagnostics and a QA-only
Mixin minimum-version diagnostic are classified separately in the evidence.
Windows OSHI performance-counter diagnostics and generation of a fresh native
GameTest server's initial properties are also classified from their actual cause.
The final audit covers build, native, installed-server and client logs, with no
unclassified ERROR/FATAL entries.
The existing Sword Throw sibling also logs its absent vanilla data fixer in
the 1.20.1 Fabric combined profile. None is silently classified as a Dogs failure
or used as a substitute for passing real feature markers.
