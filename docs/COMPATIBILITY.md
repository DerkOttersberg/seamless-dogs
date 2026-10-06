# Compatibility — 0.2.0

The current release preparation targets 1.20.1 (Fabric/Forge), 1.21.1, 26.2 and
26.3 (Fabric/Forge/NeoForge): eleven cells. See [current acceptance](0.2.0-ACCEPTANCE.md).
The [historical 0.1.0 matrix](COMPATIBILITY-0.1.0-HISTORICAL.md) does not validate
0.2.0 or the new actions.

Compatibility uses additive vanilla model hooks and explicit native network,
permission and claim adapters. Custom rigs or eye UV layouts fall back safely;
local animation and eye controls can disable these visuals. Preserve resource
pack skins/collars. No runtime animation library or Architectury API is required
by Seamless Dogs; an optional installed mod may have its own dependencies.

OPAC and Fabric Common Protection API use public queries. FTB Chunks uses its
public claim manager where a matching supported build exists. Every detected
claim is excluded, including the owner's own and administrator bypass areas.
Unsupported/unqueryable detected integrations pause terrain-changing digging.
Other claim systems need verified adapters before claiming claim-aware support.

Exact renderer/protection pins, available combined suite tests, screenshots,
limitations and final JAR hashes belong in the current acceptance record.
Isolated audio verifies entity-bound packets; audible quality and hardware-driver
behavior need interactive testing. Upstream alpha/beta loader or renderer builds
retain that status.
