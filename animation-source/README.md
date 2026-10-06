# Original Seamless Dogs clips

These mesh-free Blockbench authoring files contain the original cat petting,
cat stretch and dog digging keyframes. No downloaded animation, Minecraft mesh,
texture or audio file is included. They share the mod's All Rights Reserved license.

Open a `.bbmodel` in Blockbench's generic model editor. Each group names the
corresponding vanilla model part. Rotations are additive degrees; positions are
additive vanilla model pixels. The source skeleton intentionally has no cubes:
attach a locally extracted matching vanilla rig for preview without committing
Minecraft's assets. Reset vanilla animation before applying the additive clip.

The runtime resources under `assets/seamlessdogs/animations` are the authoritative
exports. One Minecraft tick is 0.05 seconds. Export rotation and position channels
to each frame's six values in X/Y/Z order. The runtime blends adjacent keyframes
with smoothstep and fades interrupted clips back over six ticks. Empty first and
last frames preserve vanilla poses. The 26.3 kitten stretch has a separate export
for its two-pixel legs and horizontal torso; scaling the adult's ten-pixel legs
breaks that rig. Run `python animation-source/export-stretch.py` to export both
original stretches. Front-paw offsets compensate for leg rotation to keep contact
with the floor. The adult tail tip follows its sibling base's endpoint. Older
scaled adult kitten rigs use the adult clip. Version ports must validate their own model parts
and baby transform rather than assuming one rig for every Minecraft line.

The adult revision keeps its head nearer the shoulders as the torso tilts. Its
six-pixel hind legs alternate a small bend and paw readjustment during the hold;
offsets compensate around each paw centre to preserve floor contact. The approved
26.3 kitten export is unchanged by this adult refinement.

Capture complete clips from front, side, both third-person cameras and a second
client. Check paw contact, shoulders, hindquarters, tail, hand contact and recovery
against the actual packaged game before accepting an edited export.

`export-expressions.py` exports the original six-second biscuit and grooming
clips for adult and native baby cats, plus the three-second dog head tilt.
Biscuits alternate lifted paws with the opposite toe grounded. Chest grooming
curls the muzzle toward the chest while both front paws support its weight.
Both paw-to-face washing variants were retired on 6 October 2026. The dog clip articulates one ear at its base while
the other remains alert. Adult ear cubes retain their vanilla rest geometry,
UVs and deformation; no new renderer or texture is used. The runtime applies
ear motion only when the corresponding native bones exist.
`head_alignment` is a control channel (weight in its first component), rather
than a rendered bone. It blends ambient head rotation out of paw-contact clips
and restores it during recovery. Its value also participates in interruption
fading. Stretch and hand animation resources are unchanged by these new clips.

The eye refinement preserves the complete original iris row between blinks.
Closed frames draw a contrasting crease for both adult and kitten UV layouts.
The pixel compositor has no partial-lid state because vanilla cats have only one
row of eye pixels. Resource reload regenerates lids from the current pack.

Grooming retains action ID 7 and sends variant 2 for chest cleaning.
Variants 0/1 are reserved for older peers; updated clients render chest cleaning
for every grooming variant. Only the two chest exports ship. Missing resources
fall back to vanilla instead of a retired paw wash.

Kneading uses small alternating lift/press strokes, shoulder weight shifts and a
small tail response. Paw cube depth participates in ground compensation. The
chest and leg pivots keep the tops of rigid legs beneath the coat throughout the
clip; lifting the entire long adult leg produces visible shoulder protrusion.
The original paw-wash snapshots in `original-grooming/` are historical
source-only records. They are no longer exported or included in gameplay JARs.
Dog digging has a sniff, alternating lifted
reach and grounded scrape, chest counter-motion and rear-paw support. Native
wolf upper-body motion is additive. Neither animation changes eligibility,
cooldowns, terrain checks or rewards. `export-expressions.py` exports these
clips and the existing gaze reaction. Ground compensation is checked between
keys, as well as at keys. Stretch, chest-grooming, digging and gaze exports remain
unchanged by removal of paw washing.

The procedural hand clip finishes its stroke before its eight-tick withdrawal,
using an easing curve with zero endpoint speed and acceleration. Interrupted
clips freeze their phase and fade for six ticks; partial ticks advance the fade,
not the frozen phase. Playback uses client entity ticks, preserves the last
rendered fraction on stop, and never rewinds when render/tick queries disagree.
Server world-clock corrections do not change the clip's progress. The
first-person hook adds to vanilla's arm invocation and
blends its swing back continuously instead of canceling the entire render method.

The dig's alternating contact ticks (18, 22, ..., 58) drive eleven entity-bound
block hit sounds. Sound events, volume and pitch come from the target block type,
retaining resource-pack sound replacement and terrain-specific cues.
