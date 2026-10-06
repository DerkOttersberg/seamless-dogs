#!/usr/bin/env python3
"""Supplement feature acceptance with a genuine client/server lifecycle run."""
import argparse
import json
import os
from pathlib import Path
import runpy
import secrets
import shutil
import subprocess
import sys
import time

ROOT = Path('/root/seamless-dogs-production-20261005')
HELPER = runpy.run_path(str(Path(__file__).with_name('run-packaged-server.py')))

def wait(process, log, marker, seconds=180):
    deadline=time.monotonic()+seconds
    while marker not in log.read_text(errors='replace'):
        if process.poll() is not None or time.monotonic()>deadline: raise RuntimeError(f'Missing {marker}: {log}')
        time.sleep(1)

def isolated(stage):
    if not os.environ.get('DISPLAY','').startswith(':') or os.environ['DISPLAY']==':0' or os.environ.get('WAYLAND_DISPLAY'):
        raise RuntimeError('Lifecycle QA requires the private Xvfb wrapper')
    metadata=json.loads((stage/'lifecycle.json').read_text()); processes=[]; outputs=[]
    try:
        for role,command,directory in (('server',metadata['serverCommand'],stage/'server'),('client',json.loads((stage/'client-profile/launch-command.json').read_text()),stage/'client-profile/client')):
            log=stage/f'{role}-console.log'; output=log.open('w'); outputs.append(output)
            process=subprocess.Popen(command,cwd=directory,stdin=subprocess.DEVNULL,stdout=output,stderr=subprocess.STDOUT); processes.append(process)
            if role=='server': wait(process,log,'Done (')
        processes[1].wait(timeout=600)
        if processes[1].returncode: raise RuntimeError('Lifecycle client failed; inspect client-console.log')
        game=stage/'client-profile/client'
        if not(game/'dogs-lifecycle-passed.txt').is_file(): raise RuntimeError('Lifecycle marker missing')
        client_log=(stage/'client-console.log').read_text(errors='replace')
        markers=('DIMENSION','RESPAWN','MENU','RECONNECT','CAT_DIMENSION','CAT_RESPAWN','CAT_RECONNECT')
        for marker in markers:
            if f'DOGS_LIFECYCLE_{marker}_PASS' not in client_log: raise RuntimeError(f'Missing lifecycle {marker}')
        if 'Server retained a lifecycle session' in(stage/'server-console.log').read_text(errors='replace'):
            raise RuntimeError('A native lifecycle hook retained its server session')
        HELPER['rcon'](metadata['rconPort'],metadata['password'],'save-all flush')
        HELPER['rcon'](metadata['rconPort'],metadata['password'],'stop'); processes[0].wait(timeout=60)
        if processes[0].returncode: raise RuntimeError('Lifecycle server shutdown failed')
        for path,key in ((game/'mods','clientJars'),(stage/'server/mods','serverJars')):
            actual={jar.name:HELPER['digest'](jar)for jar in sorted(path.glob('*.jar'))}
            if actual!=metadata[key]: raise RuntimeError('Lifecycle jars changed')
        (stage/'lifecycle-passed.json').write_text(json.dumps({k:metadata[k]for k in ('minecraft','loader','clientJars','serverJars')}|{'gate':'actual dimension, death/respawn, menu, active same-JVM disconnect/reconnect; subsequent request accepted; cat chest-idle dimension/death/reconnect cleanup','display':os.environ['DISPLAY']},indent=2))
        print(f"PASS lifecycle {metadata['minecraft']}/{metadata['loader']}",flush=True)
    finally:
        for process in reversed(processes):
            if process.poll() is None:
                process.terminate()
                try: process.wait(timeout=15)
                except subprocess.TimeoutExpired: process.kill();process.wait()
        for output in outputs:output.close()

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--isolated',type=Path)
    for name in ('repo','stage','server-from','client-from'):parser.add_argument('--'+name,type=Path)
    parser.add_argument('--loader',choices=('fabric','forge','neoforge'));parser.add_argument('--java');args=parser.parse_args()
    if args.isolated:isolated(args.isolated.resolve());return
    if not all((args.repo,args.stage,args.server_from,args.client_from,args.loader,args.java)):parser.error('Missing lifecycle preparation arguments')
    stage,source,client_source=(path.resolve()for path in (args.stage,args.server_from,args.client_from))
    if stage.exists() or not all(HELPER['owned_profile'](path)for path in (stage,source,client_source)):raise SystemExit('Need fresh owned lifecycle profile and accepted installations')
    pins=HELPER['version_catalog'](args.repo/'gradle/libs.versions.toml')
    accepted=json.loads((source/'server-passed.json').read_text())
    if accepted['minecraft']!=pins['minecraft'] or accepted['loader']!=args.loader:raise SystemExit('Installation mismatch')
    stage.mkdir(parents=True);server=stage/'server';server.mkdir();mods=server/'mods';mods.mkdir()
    for directory in ('libraries','versions'):
        if(source/directory).is_dir():shutil.copytree(source/directory,server/directory,copy_function=HELPER['copy_cache_file'])
    for pattern in ('*-shim.jar','fabric-server-launch.jar','fabric-server-launcher.properties','server.jar'):
        for path in source.glob(pattern):shutil.copy2(path,server/path.name)
    properties=dict(line.split('=',1)for line in(args.repo/'gradle.properties').read_text().splitlines()if'='in line and not line.startswith('#'))
    qa=args.repo/'qa-lifecycle/artifacts'/f'seamless-dogs-qa-{args.loader}.jar'
    jars=[args.repo/args.loader/'build/libs'/f"seamless-dogs-{properties['mod_version']}-{args.loader}.jar",qa]
    for jar in jars:
        if not jar.is_file():raise SystemExit(f'Missing final lifecycle input: {jar}')
        shutil.copy2(jar,mods/jar.name)
    if args.loader=='fabric':
        for jar in(source/'mods').glob('fabric-api-*.jar'):shutil.copy2(jar,mods/jar.name)
    port,rcon_port,password=HELPER['free_port'](),HELPER['free_port'](),secrets.token_hex(16)
    (server/'eula.txt').write_text('eula=true\n')
    (server/'server.properties').write_text(f'server-ip=127.0.0.1\nserver-port={port}\nonline-mode=false\nlevel-name=dogs-world\nenable-rcon=true\nrcon.port={rcon_port}\nrcon.password={password}\nspawn-protection=0\nallow-flight=true\nwhite-list=false\nenforce-whitelist=false\nview-distance=3\nsimulation-distance=3\nmax-tick-time=120000\npause-when-empty-seconds=0\nlevel-type=minecraft:flat\ngenerate-structures=false\ngenerator-settings={{"layers":[{{"block":"minecraft:bedrock","height":1}},{{"block":"minecraft:dirt","height":2}},{{"block":"minecraft:grass_block","height":1}}],"biome":"minecraft:plains"}}\n')
    profile=stage/'client-profile'
    command=[sys.executable,str(Path(__file__).with_name('run-packaged-client.py')),'--repo',str(args.repo),'--loader',args.loader,'--stage',str(profile),'--java',args.java,'--installed-from',str(client_source),'--address',f'127.0.0.1:{port}','--prepare-only']
    with(stage/'client-prepare.log').open('w')as output:subprocess.run(command,stdout=output,stderr=subprocess.STDOUT,check=True,timeout=900)
    shutil.copy2(qa,profile/'client/mods'/qa.name)
    client_command=json.loads((profile/'launch-command.json').read_text());client_command=[arg.replace('-Dqa.role=single','-Dqa.role=lifecycle')for arg in client_command];client_command.insert(1,f'-Dqa.serverAddress=127.0.0.1:{port}')
    (profile/'launch-command.json').write_text(json.dumps(client_command,indent=2))
    launch=[args.java,'-Xms256M','-Xmx1G','-XX:ActiveProcessorCount=2']
    if args.loader=='fabric':launch+=['-jar','fabric-server-launch.jar','nogui']
    else:
        group='net/minecraftforge/forge'if args.loader=='forge'else'net/neoforged/neoforge'
        launch+=[f"@libraries/{group}/{pins[args.loader]}/unix_args.txt",'nogui']
    metadata={'minecraft':pins['minecraft'],'loader':args.loader,'rconPort':rcon_port,'password':password,'serverCommand':launch,'clientJars':{jar.name:HELPER['digest'](jar)for jar in sorted((profile/'client/mods').glob('*.jar'))},'serverJars':{jar.name:HELPER['digest'](jar)for jar in sorted(mods.glob('*.jar'))}}
    (stage/'lifecycle.json').write_text(json.dumps(metadata,indent=2))
    (profile/'SHA256SUMS.before.json').write_text(json.dumps(metadata['clientJars'],indent=2))
    wrapper=stage/'isolated-lifecycle.sh';wrapper.write_text(Path('/mnt/c/Users/derko/Desktop/minecraft/tools/Run-IsolatedMinecraftClient.sh').read_text().replace('flock --nonblock 9','flock --wait 600 9'));wrapper.chmod(0o755)
    with(stage/'lifecycle-console.log').open('w')as output:
        result=subprocess.run([str(wrapper),sys.executable,str(Path(__file__).resolve()),'--isolated',str(stage)],env=dict(os.environ,ALSOFT_DRIVERS='null',ISOLATED_CLIENT_TIMEOUT_SECONDS='900'),stdout=output,stderr=subprocess.STDOUT,timeout=1560)
    if result.returncode or not(stage/'lifecycle-passed.json').is_file():raise RuntimeError(f'Lifecycle failed: {stage}/lifecycle-console.log')
    print(f"PASS installed lifecycle {pins['minecraft']}/{args.loader}: {stage}",flush=True)

if __name__=='__main__':main()
