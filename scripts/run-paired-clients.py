#!/usr/bin/env python3
"""Two real installed clients and one loopback server on one private display.

The test-only fixture/observer jar never enters release archives. All child
processes share the ecosystem display lock and bounded CPU/memory/time limits.
"""
from __future__ import annotations
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


def wait_for(process, log, marker, seconds=180):
    deadline = time.monotonic() + seconds
    while marker not in log.read_text(errors='replace'):
        if process.poll() is not None or time.monotonic() > deadline:
            raise RuntimeError(f'Missing {marker}; see {log}')
        time.sleep(1)


def isolated(stage):
    if not os.environ.get('DISPLAY', '').startswith(':') or os.environ.get('DISPLAY') == ':0' or os.environ.get('WAYLAND_DISPLAY'):
        raise RuntimeError('Paired clients require the private Xvfb wrapper')
    metadata = json.loads((stage / 'pair.json').read_text())
    server = stage / 'server'
    server_log = stage / 'server-console.log'
    processes = []
    outputs = []
    try:
        output = server_log.open('w'); outputs.append(output)
        process = subprocess.Popen(metadata['serverCommand'], cwd=server, stdin=subprocess.DEVNULL,
                                   stdout=output, stderr=subprocess.STDOUT)
        processes.append(process)
        wait_for(process, server_log, 'Done (')
        for role in ('owner', 'observer'):
            profile = stage / role
            command = json.loads((profile / 'launch-command.json').read_text())
            output = (profile / 'client-console.log').open('w'); outputs.append(output)
            child = subprocess.Popen(command, cwd=profile / 'client', stdin=subprocess.DEVNULL,
                                     stdout=output, stderr=subprocess.STDOUT)
            processes.append(child)
            if role == 'owner':
                wait_for(child, server_log, 'DOGS_QA_FIXTURE', 180)
                HELPER['rcon'](metadata['rconPort'], metadata['password'], 'op DogQA')
        deadline = time.monotonic() + 600
        while any(child.poll() is None for child in processes[1:]):
            if processes[0].poll() is not None or time.monotonic() > deadline:
                raise RuntimeError('Paired clients/server timed out or server exited')
            for role, child in zip(('owner', 'observer'), processes[1:]):
                if child.poll() is not None and child.returncode != 0:
                    raise RuntimeError(f'{role} exited {child.returncode}; see {stage / role / "client-console.log"}')
            time.sleep(1)
        markers = {'owner': 'dogs-client-passed.txt', 'observer': 'dogs-observer-passed.txt'}
        for role, marker in markers.items():
            game = stage / role / 'client'
            if not (game / marker).is_file() or (game / 'dogs-client-failed.txt').exists():
                raise RuntimeError(f'Paired {role} did not pass')
        text = (stage / 'observer/client-console.log').read_text(errors='replace')
        for marker in ('DOGS_OBSERVER_RENDER_AND_SOUND_PASS', 'DOGS_OBSERVER_LATE_TRACKING_PASS', 'DOGS_OBSERVER_PASS'):
            if marker not in text:
                raise RuntimeError(f'Observer omitted {marker}')
        captures={path.name:HELPER['digest'](path)for path in (stage/'observer/client/screenshots').glob('observer-action-*.png')}
        for action in (1,2,3,4,6,7,8):
            if not any(name.startswith(f'observer-action-{action}-')for name in captures):
                raise RuntimeError(f'Missing observer viewpoint capture for action {action}')
        HELPER['rcon'](metadata['rconPort'], metadata['password'], 'save-all flush')
        HELPER['rcon'](metadata['rconPort'], metadata['password'], 'stop')
        processes[0].wait(timeout=60)
        if any(child.returncode != 0 for child in processes):
            raise RuntimeError('A paired process failed during shutdown')
        for profile in (stage / 'owner', stage / 'observer'):
            expected = json.loads((profile / 'SHA256SUMS.before.json').read_text())
            actual = {jar.name: HELPER['digest'](jar) for jar in sorted((profile / 'client/mods').glob('*.jar'))}
            if actual != expected:
                raise RuntimeError('Paired client jars changed')
        actual = {jar.name: HELPER['digest'](jar) for jar in sorted((server / 'mods').glob('*.jar'))}
        if actual != metadata['jars']:
            raise RuntimeError('Paired server jars changed')
        (stage / 'pair-passed.json').write_text(json.dumps({
            'minecraft': metadata['minecraft'], 'loader': metadata['loader'], 'jars': actual,
            'gate': 'two real clients: dog/cat keybind, first/third/left hand, owner-only prompt, hostile pet/settings C2S rejection, remote rigs/eyes/entity sounds, resource reload, late dog/cat tracking, dig/stretch, biscuits/groom/ear tilt and late expressive clips, owner/admin settings, active owner disconnect',
            'display': os.environ['DISPLAY'],'observerCaptures':captures}, indent=2))
        print(f"PASS paired real clients {metadata['minecraft']}/{metadata['loader']}", flush=True)
    finally:
        for process in reversed(processes):
            if process.poll() is None:
                process.terminate()
                try: process.wait(timeout=15)
                except subprocess.TimeoutExpired:
                    process.kill(); process.wait()
        for output in outputs: output.close()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--isolated', type=Path)
    parser.add_argument('--repo', type=Path)
    parser.add_argument('--loader', choices=('fabric', 'forge', 'neoforge'))
    parser.add_argument('--stage', type=Path)
    parser.add_argument('--java')
    parser.add_argument('--server-from', type=Path)
    parser.add_argument('--client-from', type=Path)
    args = parser.parse_args()
    if args.isolated:
        isolated(args.isolated.resolve()); return
    if not all((args.repo, args.loader, args.stage, args.java, args.server_from, args.client_from)):
        parser.error('Preparing a pair requires repo/loader/stage/java/server-from/client-from')
    stage = args.stage.resolve()
    source = args.server_from.resolve()
    client_source = args.client_from.resolve()
    if stage.exists() or not all(path.is_relative_to(ROOT) for path in (stage, source, client_source)):
        raise SystemExit('Need a fresh owned pair and owned official source installations')
    pins = HELPER['version_catalog'](args.repo / 'gradle/libs.versions.toml')
    accepted = json.loads((source / 'server-passed.json').read_text())
    if accepted['minecraft'] != pins['minecraft'] or accepted['loader'] != args.loader:
        raise SystemExit('Official server installation does not match')
    stage.mkdir(parents=True)
    server = stage / 'server'; server.mkdir()
    for directory in ('libraries', 'versions'):
        if (source / directory).is_dir(): shutil.copytree(source / directory, server / directory,copy_function=HELPER['copy_cache_file'])
    for pattern in ('*-shim.jar', 'fabric-server-launch.jar', 'fabric-server-launcher.properties', 'server.jar'):
        for path in source.glob(pattern): shutil.copy2(path, server / path.name)
    mods = server / 'mods'; mods.mkdir()
    # Copy current, final production bytes rather than the older source profile's mods.
    properties = dict(line.split('=', 1) for line in (args.repo / 'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
    jars = [args.repo / args.loader / 'build/libs' / f"seamless-dogs-{properties['mod_version']}-{args.loader}.jar",
            args.repo / 'qa-client/artifacts' / f'seamless-dogs-qa-{args.loader}.jar']
    for jar in jars: shutil.copy2(jar, mods / jar.name)
    if args.loader == 'fabric':
        for jar in (source / 'mods').glob('fabric-api-*.jar'): shutil.copy2(jar, mods / jar.name)
    port, rcon_port, password = HELPER['free_port'](), HELPER['free_port'](), secrets.token_hex(16)
    (server / 'eula.txt').write_text('eula=true\n')
    (server / 'server.properties').write_text(
        f'server-ip=127.0.0.1\nserver-port={port}\nonline-mode=false\nlevel-name=dogs-world\n'
        f'enable-rcon=true\nrcon.port={rcon_port}\nrcon.password={password}\n'
        'spawn-protection=0\nallow-flight=true\nwhite-list=false\nenforce-whitelist=false\nview-distance=3\nsimulation-distance=3\n'
        'max-tick-time=120000\npause-when-empty-seconds=0\nlevel-type=minecraft:flat\ngenerate-structures=false\n'
        'generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}\n')
    for role, username in (('owner', 'DogQA'), ('observer', 'DogObserver')):
        command = [sys.executable, str(Path(__file__).with_name('run-packaged-client.py')),
                   '--repo', str(args.repo), '--loader', args.loader,
                   '--stage', str(stage / role), '--java', args.java, '--installed-from', str(client_source),
                   '--address', f'127.0.0.1:{port}', '--username', username, '--role', role,
                   '--gui-scale', '3', '--width', '640', '--height', '480', '--prepare-only']
        with (stage / f'{role}-prepare.log').open('w') as output:
            subprocess.run(command, stdout=output, stderr=subprocess.STDOUT, check=True, timeout=900)
    launch = [args.java, '-Xms256M', '-Xmx1G', '-XX:ActiveProcessorCount=2']
    if args.loader == 'fabric': launch += ['-jar', 'fabric-server-launch.jar', 'nogui']
    else:
        group = 'net/minecraftforge/forge' if args.loader == 'forge' else 'net/neoforged/neoforge'
        launch += [f"@libraries/{group}/{pins[args.loader]}/unix_args.txt", 'nogui']
    metadata = {'minecraft': pins['minecraft'], 'loader': args.loader, 'rconPort': rcon_port,
                'password': password, 'serverCommand': launch,
                'jars': {jar.name: HELPER['digest'](jar) for jar in sorted(mods.glob('*.jar'))}}
    (stage / 'pair.json').write_text(json.dumps(metadata, indent=2))
    wrapper = stage / 'isolated-pair.sh'
    wrapper.write_text(Path('/mnt/c/Users/derko/Desktop/minecraft/tools/Run-IsolatedMinecraftClient.sh').read_text().replace('flock --nonblock 9', 'flock --wait 600 9'))
    wrapper.chmod(0o755)
    environment = dict(os.environ, ALSOFT_DRIVERS='null', ISOLATED_CLIENT_TIMEOUT_SECONDS='900')
    with (stage / 'pair-console.log').open('w') as output:
        result = subprocess.run([str(wrapper), sys.executable, str(Path(__file__).resolve()), '--isolated', str(stage)],
                                env=environment, stdout=output, stderr=subprocess.STDOUT, timeout=1560)
    if result.returncode or not (stage / 'pair-passed.json').is_file():
        raise RuntimeError(f'Paired clients failed; see {stage / "pair-console.log"}')
    print(f"PASS paired real clients {pins['minecraft']}/{args.loader}: {stage}", flush=True)


if __name__ == '__main__': main()
