# Compatibility — standalone 0.2.0

The active release contains eleven builds across four Minecraft branches.
Use the exact version and loader below. Seamless Dogs does not require SeamlessLib.
Fabric builds additionally require the matching Fabric API.

| Minecraft | Loader | Tested loader | Java | Fabric API |
| --- | --- | --- | --- | --- |
| 1.20.1 | fabric | 0.19.5 | 17 | 0.92.12+1.20.1 |
| 1.20.1 | forge | 1.20.1-47.4.26 | 17 | — |
| 1.21.1 | fabric | 0.19.5 | 21 | 0.116.17+1.21.1 |
| 1.21.1 | forge | 1.21.1-52.1.16 | 21 | — |
| 1.21.1 | neoforge | 21.1.255 | 21 | — |
| 26.2 | fabric | 0.19.5 | 25 | 0.159.0+26.2 |
| 26.2 | forge | 26.2-65.1.3 | 25 | — |
| 26.2 | neoforge | 26.2.0.75 | 25 | — |
| 26.3 | fabric | 0.19.5 | 25 | 0.161.0+26.3 |
| 26.3 | forge | 26.3-66.0.9 | 25 | — |
| 26.3 | neoforge | 26.3.0.48-beta | 25 | — |

## Recorded combined profiles

Only the following successful profiles establish compatibility for the current
gameplay JAR hashes. Filenames identify the exact tested builds; complete hashes
and original download metadata are in [the acceptance record](acceptance-0.2.0.json).
Unavailable matching builds are outside this test coverage.

### 1.20.1 fabric

Current combined acceptance is pending.

### 1.20.1 forge

Current combined acceptance is pending.

### 1.21.1 fabric

Current combined acceptance is pending.

### 1.21.1 forge

Current combined acceptance is pending.

### 1.21.1 neoforge

Current combined acceptance is pending.

### 26.2 fabric

- `ForgeConfigAPIPort-v26.2.1-mc26.2.x-Fabric.jar`
- `common-protection-api-2.0.0.jar`
- `entity_model_features-3.3.11-26.2-fabric.jar`
- `entity_texture_features-7.2.5-26.2-fabric.jar`
- `iris-fabric-1.11.4+mc26.2.jar`
- `modmenu-20.0.3.jar`
- `notenoughanimations-fabric-1.12.5-mc26.2.jar`
- `open-parties-and-claims-fabric-26.2-0.32.7.jar`
- `sodium-fabric-0.9.2+mc26.2.jar`

Active shader pack: `SeamlessDogsQa.zip`.

Available combined Seamless installation:

- `pretty-meteors-with-trails-2.0.1+mc26.2-fabric.jar`
- `seamless-api-2.0.1+mc26.2-fabric.jar`
- `seamless-crafting-2.1.0+mc26.2-fabric.jar`
- `seamless-deconstructing-workbench-2.1.0+mc26.2-fabric.jar`
- `seamless-dogs-0.2.0+mc26.2-fabric.jar`
- `sword-throw-2.1.0+mc26.2-fabric.jar`

The sibling mods retain their own SeamlessLib dependency. Dogs also passes
an independent installation without those sibling mods or SeamlessLib.

### 26.2 forge

- `ForgeConfigAPIPort-v26.2.1-mc26.2.x-Forge.jar`
- `open-parties-and-claims-forge-26.2-0.32.7.jar`

This profile uses the vanilla renderer; it does not establish shader compatibility.

Available combined Seamless installation:

- `pretty-meteors-with-trails-2.0.1+mc26.2-forge.jar`
- `seamless-api-2.0.1+mc26.2-forge.jar`
- `seamless-crafting-2.1.0+mc26.2-forge.jar`
- `seamless-deconstructing-workbench-2.1.0+mc26.2-forge.jar`
- `seamless-dogs-0.2.0+mc26.2-forge.jar`
- `sword-throw-2.1.0+mc26.2-forge.jar`

The sibling mods retain their own SeamlessLib dependency. Dogs also passes
an independent installation without those sibling mods or SeamlessLib.

### 26.2 neoforge

- `entity_model_features-3.3.11-26.2-neoforge.jar`
- `entity_texture_features-7.2.5-26.2-neoforge.jar`
- `iris-neoforge-1.11.4+mc26.2.jar`
- `notenoughanimations-neoforge-1.12.5-mc26.2.jar`
- `open-parties-and-claims-neoforge-26.2-0.32.7.jar`
- `sodium-neoforge-0.9.2+mc26.2.jar`

Active shader pack: `SeamlessDogsQa.zip`.

Available combined Seamless installation:

- `pretty-meteors-with-trails-2.0.1+mc26.2-neoforge.jar`
- `seamless-api-2.0.1+mc26.2-neoforge.jar`
- `seamless-crafting-2.1.0+mc26.2-neoforge.jar`
- `seamless-deconstructing-workbench-2.1.0+mc26.2-neoforge.jar`
- `seamless-dogs-0.2.0+mc26.2-neoforge.jar`
- `sword-throw-2.1.0+mc26.2-neoforge.jar`

The sibling mods retain their own SeamlessLib dependency. Dogs also passes
an independent installation without those sibling mods or SeamlessLib.

### 26.3 fabric

- `ForgeConfigAPIPort-v26.3.1-mc26.3.x-Fabric.jar`
- `common-protection-api-2.0.0.jar`
- `entity_model_features-3.3.11-26.3-fabric.jar`
- `entity_texture_features-7.2.5-26.3-fabric.jar`
- `iris-fabric-1.11.7+mc26.3.jar`
- `modmenu-21.0.0.jar`
- `notenoughanimations-fabric-1.12.6-mc26.3.jar`
- `open-parties-and-claims-fabric-26.3-0.32.8.jar`
- `sodium-fabric-0.9.2+mc26.3.jar`

Active shader pack: `SeamlessDogsQa.zip`.

Available combined Seamless installation:

- `pretty-meteors-with-trails-2.0.2+mc26.3-fabric.jar`
- `seamless-api-2.0.2+mc26.3-fabric.jar`
- `seamless-crafting-2.1.1+mc26.3-fabric.jar`
- `seamless-deconstructing-workbench-2.1.1+mc26.3-fabric.jar`
- `seamless-dogs-0.2.0+mc26.3-fabric.jar`
- `sword-throw-2.1.1+mc26.3-fabric.jar`

The sibling mods retain their own SeamlessLib dependency. Dogs also passes
an independent installation without those sibling mods or SeamlessLib.

### 26.3 forge

- `ForgeConfigAPIPort-v26.3.1-mc26.3.x-Forge.jar`
- `open-parties-and-claims-forge-26.3-0.32.8.jar`

This profile uses the vanilla renderer; it does not establish shader compatibility.

Available combined Seamless installation:

- `pretty-meteors-with-trails-2.0.2+mc26.3-forge.jar`
- `seamless-api-2.0.2+mc26.3-forge.jar`
- `seamless-crafting-2.1.1+mc26.3-forge.jar`
- `seamless-deconstructing-workbench-2.1.1+mc26.3-forge.jar`
- `seamless-dogs-0.2.0+mc26.3-forge.jar`
- `sword-throw-2.1.1+mc26.3-forge.jar`

The sibling mods retain their own SeamlessLib dependency. Dogs also passes
an independent installation without those sibling mods or SeamlessLib.

### 26.3 neoforge

- `entity_model_features-3.3.11-26.3-neoforge.jar`
- `entity_texture_features-7.2.5-26.3-neoforge.jar`
- `iris-neoforge-1.11.7+mc26.3.jar`
- `notenoughanimations-neoforge-1.12.6-mc26.3.jar`
- `open-parties-and-claims-neoforge-26.3-0.32.8.jar`
- `sodium-neoforge-0.9.2+mc26.3.jar`

Active shader pack: `SeamlessDogsQa.zip`.

Available combined Seamless installation:

- `pretty-meteors-with-trails-2.0.2+mc26.3-neoforge.jar`
- `seamless-api-2.0.2+mc26.3-neoforge.jar`
- `seamless-crafting-2.1.1+mc26.3-neoforge.jar`
- `seamless-deconstructing-workbench-2.1.1+mc26.3-neoforge.jar`
- `seamless-dogs-0.2.0+mc26.3-neoforge.jar`
- `sword-throw-2.1.1+mc26.3-neoforge.jar`

The sibling mods retain their own SeamlessLib dependency. Dogs also passes
an independent installation without those sibling mods or SeamlessLib.

## Scope and limitations

Additive model hooks preserve vanilla renderers, skins, collars and resource-pack
textures. Unrecognized rigs or eye UV layouts can fall back; affected visuals
can be disabled in Client visuals. No runtime animation or Architectury API is
required by Dogs.

Digging honors mobGriefing, spawn protection, native cancellable hooks, and the
recorded OPAC, FTB Chunks and Fabric Common Protection API adapters. Detected
claims are avoided even when owned by the pet owner. Supported integrations
that cannot be queried safely stop terrain-changing digs. Other claim systems
need verified adapters before claim-aware support can be advertised.

Tests use isolated Xvfb software rendering and silent OpenAL. Entity-bound sound
events and engine playback are checked; audible quality and every hardware
driver are outside that automated coverage. Upstream beta builds remain beta.
Hosted CI is reported separately from successful local tests. No CurseForge
publication was performed.

See [current acceptance](0.2.0-ACCEPTANCE.md) for native, singleplayer, two-client,
restart, lifecycle and visual gates. The [historical matrix](COMPATIBILITY-0.1.0-HISTORICAL.md)
does not establish compatibility for these 0.2.0 JARs.
