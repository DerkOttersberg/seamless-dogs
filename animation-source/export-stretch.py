"""Export original adult/26.3 kitten stretch clips; no vanilla meshes are included."""
from pathlib import Path
import copy
import json
import math
import uuid

ROOT = Path(__file__).resolve().parents[1]
template = json.loads((ROOT / 'animation-source/cat_stretch.bbmodel').read_text())
for baby in (False, True):
    name = 'cat_stretch_baby' if baby else 'cat_stretch'
    frames = [{'tick': 0, 'parts': {}}]
    for tick, weight, wiggle in [(8,.12,0),(22,1,0),(30,1,-1),(38,1,1),(46,1,-.65),(54,1,.4),(60,.94,0),(70,.25,0),(80,0,0)]:
        if not weight:
            frames.append({'tick': tick, 'parts': {}})
            continue
        angle = (-38 if baby else -42) * weight
        length = 2 if baby else 10
        # Raise each shoulder pivot by the shortening of its rotated leg,
        # keeping the paw on the floor throughout the reach and recovery.
        leg_y = length * (1 - math.cos(math.radians(angle)))
        leg_z = (-.25 if baby else -.3) * weight
        parts = {
            'body': [(12 if baby else 14)*weight,wiggle*.8,wiggle*.6,wiggle*(.04 if baby else .10),(-.10 if baby else 2)*weight,0],
            'head': [(4 if baby else 12)*weight,wiggle,wiggle*-.8,wiggle*.04,(.4 if baby else 2.8)*weight,(-.4 if baby else -1)*weight],
            'left_front_leg': [angle,0,0,0,leg_y,leg_z],
            'right_front_leg': [angle,0,0,0,leg_y,leg_z],
            'left_hind_leg': [-4*weight,0,0,0,length*(1-math.cos(math.radians(-4*weight))),0],
            'right_hind_leg': [-4*weight,0,0,0,length*(1-math.cos(math.radians(-4*weight))),0],
            'tail1': [-6*weight,wiggle*3,0,wiggle*.04,(-.55 if baby else -1.6)*weight,(-.1 if baby else -.4)*weight],
        }
        if not baby:
            # The adult's neck must overlap the front of the pitching torso.
            # Its head is a sibling bone, so a forward offset opens a visible gap.
            parts['head'] = [8*weight,wiggle,wiggle*-.8,wiggle*.04,2.4*weight,.6*weight]
            # Adult hind legs are six pixels long (front legs are ten). Alternate
            # a small hock bend and paw readjustment during the held stretch.
            # Compensate around the paw's local (0,6,2) centre to retain contact.
            for side, direction in [('left',1),('right',-1)]:
                hind_angle = -8*weight + direction*wiggle*6
                radians = math.radians(hind_angle)
                paw_shift = .35*weight + direction*wiggle*.18
                parts[f'{side}_hind_leg'] = [hind_angle,0,0,0,
                    6-(6*math.cos(radians)-2*math.sin(radians)),
                    paw_shift+2-(6*math.sin(radians)+2*math.cos(radians))]
            parts['tail2'] = [-6*weight,wiggle*2,0,0,0,0]
        frames.append({'tick':tick,'parts':{key:[round(v,4) for v in values] for key,values in parts.items()}})
    (ROOT / f'common/src/main/resources/assets/seamlessdogs/animations/{name}.json').write_text(json.dumps({'frames':frames},indent=2)+'\n')
    model = copy.deepcopy(template)
    model['name'] = name
    model['model_identifier'] = 'seamlessdogs:' + name
    animation = model['animations'][0]
    animation['name'] = name
    for group in model['outliner']:
        key = group['name']
        animator = animation['animators'][group['uuid']]
        animator['keyframes'] = []
        for frame in frames:
            pose = frame['parts'].get(key,[0]*6)
            for channel, values in [('rotation',pose[:3]),('position',pose[3:])]:
                animator['keyframes'].append({'channel':channel,'data_points':[dict(zip('xyz',map(str,values)))],
                    'time':frame['tick']/20,'interpolation':'bezier',
                    'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,f'{name}/{key}/{channel}/{frame["tick"]}'))})
    (ROOT / f'animation-source/{name}.bbmodel').write_text(json.dumps(model,indent=2)+'\n')
