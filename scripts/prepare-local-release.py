#!/usr/bin/env python3
"""Package the eleven standalone release cells after exact-JAR acceptance."""
from __future__ import annotations
import argparse,hashlib,json,shutil,subprocess,zipfile
from pathlib import Path

VERSIONS=('1.20.1','1.21.1','26.2','26.3')
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def write(path,text):path.write_text(text,encoding='utf-8')

def main():
    p=argparse.ArgumentParser();p.add_argument('--workspace',type=Path,required=True);p.add_argument('--acceptance',type=Path,required=True);p.add_argument('--destination',type=Path,required=True);a=p.parse_args()
    w,target=a.workspace.resolve(),a.destination.resolve()
    if target.exists() or not target.is_relative_to(w/'release-candidates'):raise SystemExit('Use a fresh owned release directory')
    acceptance=json.loads(a.acceptance.read_text())
    cells={(v,l) for v in VERSIONS for l in ('fabric','forge')+(() if v=='1.20.1' else ('neoforge',))}
    rows=acceptance.get('cells',[])
    if acceptance.get('complete') is not True or len(rows)!=11 or {(r['minecraft'],r['loader'])for r in rows}!=cells:raise SystemExit('Complete eleven-cell standalone acceptance is required')
    prepared=[]
    for row in rows:
        v,l=row['minecraft'],row['loader'];repo=w/'seamless-dogs' if v=='26.3' else w/f'.ports/dogs-multiversion/mc{v}/seamless-dogs'
        if not all(row.get(k) is True for k in ('buildPassed','nativePassed','serverPassed','clientPassed','multiplayerPassed','lifecyclePassed','combinedPassed')):raise SystemExit(f'Incomplete acceptance: {v}/{l}')
        jars=list((repo/l/'build/libs').glob(f'*-{l}.jar'))
        if len(jars)!=1 or row['jars'].get(jars[0].name)!=sha(jars[0]):raise SystemExit(f'Runtime bytes changed after acceptance: {v}/{l}')
        with zipfile.ZipFile(jars[0]) as z:
            if any(n.startswith(('qa/','com/derko/seamlessapi/')) or '/gametest/' in n for n in z.namelist()):raise SystemExit('Test code or library classes found in the gameplay JAR')
            metadata=z.read('fabric.mod.json' if l=='fabric' else 'META-INF/mods.toml' if l=='forge' else 'META-INF/neoforge.mods.toml').decode()
            if 'seamlessapi' in metadata:raise SystemExit('Standalone metadata still declares the library')
        prepared.append((row,repo,jars[0]))
    target.mkdir(parents=True)
    manifest={'product':'Seamless Dogs','version':'0.2.0','license':'All Rights Reserved','standalone':True,'publication':'local only','artifacts':[],'acceptance':acceptance}
    for row,repo,jar in prepared:
        v,l=row['minecraft'],row['loader'];folder=target/f'mc{v}'/l;mods=folder/'mods';mods.mkdir(parents=True)
        shutil.copy2(jar,mods/jar.name);write(folder/'LICENSE.txt',(repo/'LICENSE').read_text())
        extra=f"Fabric API {row['fabricApi']} is also required. Download it from the official Fabric project.\n" if l=='fabric' else ''
        write(folder/'INSTALL.txt',f'''Seamless Dogs 0.2.0 — Minecraft {v}, {l}, Java {row['java']}

Install the selected loader ({row['loaderVersion']}). Copy the Dogs JAR in mods/
to the matching client and server mods folders. Choose one Minecraft/loader
combination. SeamlessLib is not required or bundled.
{extra}
Look at your own tamed dog, cat or kitten within three blocks with an empty
main hand and press G. Rebind it in Options > Controls > Seamless Dogs.
Petting preserves ownership, health and sitting. Calm cats stretch, knead or
groom every 3–6 minutes; adult dogs attempt a dig every 10–20 minutes.
Sounds originate from the pet. Both clients and server need this gameplay mod.

Settings: optional Fabric Mod Menu or the Forge/NeoForge Mods screen. The
settings key starts unbound. Client visuals, owner digging preferences and
administrator world settings have separate scopes and Save/Cancel controls.
Digging respects mobGriefing and supported claim systems. Unknown protection
systems need a verified adapter before claim-aware support can be advertised.

Original keyframes use vanilla rigs and resource-pack textures. Custom rigs or
UV layouts can fall back; individual visuals can be disabled in settings.
See acceptance.json for exact tests and limitations. All Rights Reserved.
''')
        write(folder/'acceptance.json',json.dumps({'cell':row,'limitations':acceptance.get('limitations',[])},indent=2)+'\n')
        write(folder/'SHA256SUMS.txt',f'{sha(jar)}  mods/{jar.name}\n')
        archive_path=target/f'seamless-dogs-0.2.0-mc{v}-{l}-standalone-install.zip'
        with zipfile.ZipFile(archive_path,'w',zipfile.ZIP_DEFLATED) as z:
            for path in sorted(folder.rglob('*')):
                if path.is_file():z.write(path,path.relative_to(folder).as_posix())
        manifest['artifacts'].append({'minecraft':v,'loader':l,'java':row['java'],'loaderVersion':row['loaderVersion'],'fabricApi':row.get('fabricApi'),'sourceCommit':subprocess.check_output(['git','-C',str(repo),'rev-parse','HEAD'],text=True).strip(),'jars':{jar.name:sha(jar)},'installZip':archive_path.name,'installZipSha256':sha(archive_path)})
    write(target/'artifacts.json',json.dumps(manifest,indent=2)+'\n');write(target/'acceptance.json',json.dumps(acceptance,indent=2)+'\n')
    write(target/'README.md','''# Seamless Dogs 0.2.0 — standalone install bundles

Eleven builds cover 1.20.1 Fabric/Forge and 1.21.1, 26.2, 26.3 Fabric/Forge/NeoForge.
Each ZIP contains one Dogs gameplay JAR and its installation/license/hash notes.
Fabric API is an additional Fabric requirement. SeamlessLib is not included or
required. Use the exact Minecraft version and loader. The acceptance record
lists tested combinations, evidence and limitations. Publication is separate.
''')
    print(f'Prepared eleven accepted standalone install bundles: {target}')

if __name__=='__main__':main()
