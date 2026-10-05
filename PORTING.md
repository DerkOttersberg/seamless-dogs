# Version branches and architecture

One Seamless Dogs product repository has simple Minecraft branches: `1.20.1`,
`1.21.1`, `1.21.11`, `26.1`, `26.1.2`, `26.2`, and `26.3`. Each branch contains
`common`, `fabric`, `forge`, and (except 1.20.1) `neoforge`. Each line implements
its actual Minecraft APIs; exact version metadata prevents loading a different
line's jar. NeoForge 1.20.1 is excluded because upstream ended support.

Common code owns server authorization, UUID-based sessions, cooldowns, bounded
packets, animation, texture derivation, and client settings. Loader-local
`PlatformServices` and `ClientPlatformServices` are injected explicitly by each
entrypoint. Native adapters own networking, keys, tracking, tick/lifecycle hooks,
and settings entrypoints. Common code imports no loader API. Architectury and
Loom are build tools; there is no runtime Architectury dependency, reflection,
or ServiceLoader adapter discovery.

The matching independent SeamlessLib source checkout is a task-backed composite
input and a separate required mod jar. Its public `com.derko.seamlessapi` contracts
and MIT license remain intact. No API implementation is copied or shaded. The
dog clip currently needs no new public library operation.

26.x uses official unobfuscated Minecraft classes and `loom-no-remap`.
1.20.1/1.21.x use official Mojang mappings, regular Loom, native remapping, and
Mixin refmaps. Java targets are 17, 21, and 25 respectively. Legacy wolf renderers
pass tail angle as setupAnim age: frame delta comes from prepareMobModel.
Legacy sleeves are siblings and copy the finished arm pose; newer sleeves are
children and retain their own transforms. Forge's HUD event/return differences
are handled by native adapters and branch-specific injections.

`qa-client` and native scenario source sets are separate test-only drivers. They
never enter gameplay jars. Verification guards each named native scenario;
Forge 1.21.11 uses a genuine official installed test server because the current
Loom joined development patches contain inconsistent anonymous codec classes.
The QA-only ticker drives native scenarios in that production environment.

Ports must pass the full feature matrix in docs/COMPATIBILITY.md. Changing a
runtime jar invalidates its SHA-based client/server acceptance. Failed attempts
remain recorded. Publishing matching library ports and hosted CI is a separate
remote action; local verification does not imply a public release.
