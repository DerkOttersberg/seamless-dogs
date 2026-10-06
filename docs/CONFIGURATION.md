# Settings and datapacks

The settings key is unbound until assigned under Controls. Forge/NeoForge also
provide Mods settings buttons. Fabric’s optional Mod Menu opens the same screen.
Each page labels its scope. Reset changes its draft to defaults, Cancel leaves
saved settings alone, and Save applies the displayed page.

Client visuals retain `config/seamlessdogs-client.properties` and its `prompt`,
`eyes` and `animation` keys. Saves retain other properties, validate booleans,
write a temporary file, keep `.bak`, then replace the original atomically where
the filesystem supports it. Unsupported textures retain their original pixels.

World rules, preferences and cooldowns are stored in
`<world>/serverconfig/seamlessdogs-world.json`. Transfer this file with the world.
Owner settings always use the authenticated connection’s UUID. They cannot change
another owner’s preference. World rules require the singleplayer host or permission
level 2 on dedicated servers. A stale revision must refresh before saving; failed
saves are reported. Damaged/unsupported files are preserved and idle behavior is
disabled until repaired. Backups use the same filename plus `.bak`.

Defaults enable digging, bonus finds and pet idle expressions. The existing
`stretching` world rule now controls stretching, biscuits, grooming and dog gaze
reactions; its settings button is labelled “Pet idle expressions”. Owners can disable digging
for their own pets even when the server enables it. Disabling a rule interrupts
its current idle action. Existing deadlines and owner limits are retained.

Calm standing cats and kittens choose a stretch, biscuit kneading or grooming
at their 3–6 minute idle attempt. Biscuit and grooming clips last six seconds.
Dogs sometimes tilt their head and tuck an ear when their owner looks directly
at them within six blocks, with visibility and calm checks and a 1–2 minute
persisted reaction cooldown. A seated dog or puppy may react. Digging retains
its separate adult-only 10–20 minute interval and owner limit. Petting overrides
idle expressions; movement, damage, combat or disabled rules cancel them.

The expressive HELLO capability is required before starting an owner's new
actions. Observers negotiate independently; older clients receive only the
existing dog/cat/stretch/dig protocol. Settings keep wire ID 5, and new clips
use explicit IDs 6–8. Sounds are low-volume entity-bound vanilla purr/pant
variants, using baby variants and resource-pack sound substitutions.

On this branch, a datapack can override:

| Resource | Purpose |
| --- | --- |
| `data/seamlessdogs/tags/block/diggable.json` | Terrain; unsafe farmland/paths, fluids, block entities and supports stay excluded |
| `data/seamlessdogs/loot_table/digging/finds.json` | Bonus finds after successful removal; normal block drops are independent |
| `data/seamlessdogs/tags/enchantment/digging_books.json` | Non-curse book options, including treasure enchantments |

The bonus table uses a gift context with the dog as `this_entity` and its position
as `origin`. One pool uses weights 950 empty, 40 common, 8 mineral and 2 book,
totaling 1000. Block removal must succeed before the table is evaluated, once.
Datapacks may replace entries with modded items or enchantments.

Digging never searches unloaded chunks. It checks terrain and protection again
just before removal, including block identity, owner limit and native cancellation.
All detected supported claims are excluded even when the owner can edit them.
The status text identifies unavailable protection queries. Additional claim mods
need verified adapters; native cancellation alone does not establish universal
claim detection.

Grooming uses only chest cleaning. Both paw-to-face washing variants were
removed on 6 October 2026. The server sends variant 2 to observers and late
trackers. Existing wire IDs remain reserved; updated clients map old grooming
variants to chest cleaning too. Missing chest resources retain the vanilla pose.
Client eyes retain the pack's complete open-eye pixels between blinks; closed
lids contrast with both light and dark coats.
