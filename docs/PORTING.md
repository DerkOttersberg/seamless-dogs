# Porting Seamless Dogs 0.2.0

This product is standalone by the owner's explicit choice on 5 October 2026.
It preserves the common-code/explicit-loader-adapter architecture and requires
no SeamlessLib checkout or runtime JAR. Do not copy or embed library classes.

The maintained branches are 1.20.1 (Fabric/Forge), 1.21.1, 26.2 and 26.3
(Fabric/Forge/NeoForge): eleven release cells. Minecraft and loader pins live in
gradle/libs.versions.toml. Use a Java 25 Gradle host; the game toolchains are
Java 17 for 1.20.1, 21 for 1.21.1 and 25 for 26.2/26.3.

Run `gradlew clean check build` in a clean checkout of this branch. Native
GameTests are part of check. Legacy production JARs come from remapJar, not
build/devlibs. Modern no-remap JARs come from jar.

Common code owns eligibility, scheduling, authority, animation clips,
persistence and packet validation. Loader adapters own packet transport,
permissions, native hooks, optional protection integration and menu entries.
Client code never loads on a dedicated server. Fabric requires Fabric API;
optional Mod Menu and protection integrations remain loader-local.

Keep the protocol IDs, world/config filenames and atomic-save backups stable.
Retarget native vanilla models, baby scale, texture pixel formats, sound
variants and hand submission hooks for each Minecraft version. Preserve the
chest grooming clip and blend back to vanilla poses.

Release acceptance requires the final JAR hashes, native tests and genuine
packaged singleplayer, two-client multiplayer, lifecycle and combined-suite
tests. Historical dependency-containing results do not validate the standalone
JARs. `scripts/run-packaged-*.py` uses private, disposable WSL profiles.
