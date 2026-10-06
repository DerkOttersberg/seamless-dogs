"""Original expressive clips for native 26.3 rigs. No textures, audio or vanilla meshes."""
from pathlib import Path
import copy, json, math, uuid
ROOT=Path(__file__).resolve().parents[1]
template=json.loads((ROOT/'animation-source/cat_stretch.bbmodel').read_text())
def smooth(t):
    t=max(0,min(1,t));return t*t*(3-2*t)

def rotate(point,angles):
    x,y,z=point
    rx,ry,rz=map(math.radians,angles)
    y,z=y*math.cos(rx)-z*math.sin(rx),y*math.sin(rx)+z*math.cos(rx)
    x,z=x*math.cos(ry)+z*math.sin(ry),-x*math.sin(ry)+z*math.cos(ry)
    return [x*math.cos(rz)-y*math.sin(rz),x*math.sin(rz)+y*math.cos(rz),z]
def export(name,duration,pose=None,step=2,frames=None):
    if frames is None:
        frames=[]
        for tick in range(0,duration+1,step):
            parts=pose(tick)
            frames.append({'tick':tick,'parts':{k:[round(v,4) for v in p] for k,p in parts.items()} if tick not in (0,duration) else {}})
    (ROOT/f'common/src/main/resources/assets/seamlessdogs/animations/{name}.json').write_text(json.dumps({'frames':frames},indent=2)+'\n')
    model=copy.deepcopy(template);model['name']=name;model['model_identifier']='seamlessdogs:'+name
    animation=model['animations'][0];animation['name']=name;animation['length']=duration/20
    bones=sorted({bone for f in frames for bone in f['parts']})
    model['outliner']=[];animation['animators']={}
    for bone in bones:
        bone_id=str(uuid.uuid5(uuid.NAMESPACE_URL,name+'/'+bone))
        model['outliner'].append({'name':bone,'origin':[0,0,0],'uuid':bone_id,'export':True,'isOpen':True,'children':[]})
        keys=[]
        for frame in frames:
            p=frame['parts'].get(bone,[0]*6)
            for channel,values in [('rotation',p[:3]),('position',p[3:])]:
                keys.append({'channel':channel,'data_points':[dict(zip('xyz',map(str,values)))],'time':frame['tick']/20,
                    'interpolation':'bezier','uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,f'{name}/{bone}/{channel}/{frame["tick"]}'))})
        animation['animators'][bone_id]={'name':bone,'type':'bone','keyframes':keys}
    (ROOT/f'animation-source/{name}.bbmodel').write_text(json.dumps(model,indent=2)+'\n')
for baby in (False,True):
    def knead(t):
        w=smooth(t/24)*smooth((120-t)/20)
        cycle=math.sin((t-24)*math.pi/16)
        sway=cycle*w
        parts={'body':[0,0,.7*sway,(.04 if baby else .12)*sway,(-.05 if baby else -.45)*w,0],
            'head_alignment':[w,0,0,0,0,0],
            'head':[15*w,1.2*sway,-1*sway,(.06 if baby else .18)*sway,(.3 if baby else 1.25)*w,0],
            'tail1':[-5*w,3*sway,0,0,0,0], 'tail2':[0,-2*sway,0,0,0,0]}
        length=2 if baby else 10
        for side,phase in [('left',cycle),('right',-cycle)]:
            lift=max(0,phase)*w
            angle=-10*w-10*lift
            radians=math.radians(angle)
            # Use the bottom of the actual paw cube, not only its center. Small
            # lifts and a raised chest keep the rigid shoulder inside the coat.
            depth=-abs(math.sin(radians)) if baby else 2*math.sin(radians)
            parts[side+'_front_leg']=[angle,0,0,0,length*(1-math.cos(radians))+depth-(.15 if baby else .3)*lift*lift,
                -length*math.sin(radians)-(.16 if baby else .55)*w-(.1 if baby else .45)*lift]
        for side in ['left','right']:
            angle=3*w; r=math.radians(angle); length=2 if baby else 6
            parts[side+'_hind_leg']=[angle,0,0,0,length*(1-math.cos(r))+(0 if baby else 2)*math.sin(r),-length*math.sin(r)]
        return parts
    def groom_chest(t):
        w=smooth(t/24)*smooth((120-t)/20)
        pulse=math.sin((t-28)*math.pi/12)*w
        # Curl the muzzle towards the chest; front paws continue supporting weight.
        return {'body':[0,0,1.3*pulse,0,(.05 if baby else .15)*w,0],
            'head_alignment':[w,0,0,0,0,0],
            'head':[(65 if baby else 70)*w+3*pulse,8*pulse,2*pulse,0,(-1.05 if baby else .3)*w,(1.4 if baby else 1.6)*w],
            'tail1':[-3*w,2*pulse,0,0,0,0], 'tail2':[0,-pulse,0,0,0,0]}
    export('cat_knead'+('_baby' if baby else ''),120,knead)
    # Paw-to-face washing was retired at the owner's request.
    export('cat_groom_chest'+('_baby' if baby else ''),120,groom_chest)
def dig(t):
    w=smooth(t/16)*smooth((80-t)/18)
    digging=smooth((t-14)/4)*smooth((64-t)/6)
    sway=math.sin((t-18)*math.pi/4)*digging
    sniff=math.sin(t*math.pi/7)*smooth((20-t)/8)
    parts={'head_alignment':[w,0,0,0,0,0],
        'head':[(23+2*sniff)*w,2*sway,1.2*sway,.12*sway,.9*w,-.25*w],
        'upper_body':[4*w,0,-1.4*sway,.16*sway,.5*w,-.15*w],
        'body':[2*w,0,1.2*sway,.12*sway,.1*w,0],
        'tail':[-5*w,2*sway,0,0,0,0]}
    for side,delay in [('left',0),('right',4)]:
        phase=((t-18+delay)%8)/8
        # Lift/reach for the first half, scrape back on the ground for the second.
        if phase<.5:
            q=smooth(phase*2); angle=12-44*q; lift=1.5*math.sin(phase*2*math.pi)
        else:
            q=smooth((phase-.5)*2); angle=-32+44*q; lift=0
        angle=(-8*(1-digging)+angle*digging)*w
        r=math.radians(angle)
        parts[side+'_front_leg']=[angle,0,0,0,8*(1-math.cos(r))-abs(math.sin(r))-lift*digging*w,-.35*digging*w]
        angle=3*w;r=math.radians(angle)
        parts[side+'_hind_leg']=[angle,0,0,0,8*(1-math.cos(r))-abs(math.sin(r)),-8*math.sin(r)]
    return parts
export('dog_dig',80,dig,1)
def tilt(t):
    w=smooth(t/18)*smooth((60-t)/18)
    # Lead with attention, settle into the tilt, then return without an overshoot.
    return {'head':[-3*w,2*w,19*w,0,0,0], 'left_ear':[28*w,0,-38*w,0,0,0], 'right_ear':[-5*w,0,3*w,0,0,0]}
export('dog_head_tilt',60,tilt,6)
