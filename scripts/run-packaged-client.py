#!/usr/bin/env python3
"""Launch an installed Minecraft client with exact gameplay and test-only jars.

Uses official version JSONs/installers rather than a development game. Runs only
on a private Xvfb display through the ecosystem's exclusive client wrapper.
"""
from __future__ import annotations
import argparse
from concurrent.futures import ThreadPoolExecutor
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import time
import uuid
import zipfile
import runpy

helper = runpy.run_path(str(Path(__file__).with_name('run-packaged-server.py')))
version_catalog, digest, download = (helper[name] for name in ('version_catalog', 'digest', 'download'))


def artifact(url: str, path: Path, sha: str | None = None) -> None:
    if path.is_file() and (sha is None or hashlib.sha1(path.read_bytes()).hexdigest() == sha):
        return
    if not url:
        raise RuntimeError(f'Official installer did not generate {path}')
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_name(path.name + '.dogs-download')
    download(url, temporary)
    if sha is not None and hashlib.sha1(temporary.read_bytes()).hexdigest() != sha:
        raise RuntimeError(f'Official download checksum mismatch: {url}')
    temporary.replace(path)


def rules_allow(rules: list[dict] | None) -> bool:
    if not rules:
        return True
    result = False
    for rule in rules:
        system = rule.get('os', {})
        if system.get('name', 'linux') != 'linux':
            continue
        if 'arch' in system and not re.fullmatch(system['arch'], 'x86_64'):
            continue
        features = rule.get('features', {})
        if any(value != (key == 'has_custom_resolution') for key, value in features.items()):
            continue
        result = rule['action'] == 'allow'
    return result


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument('--repo', type=Path, required=True)
    parser.add_argument('--api', type=Path, required=True)
    parser.add_argument('--loader', choices=['fabric', 'forge', 'neoforge'], required=True)
    parser.add_argument('--stage', type=Path, required=True)
    parser.add_argument('--java', required=True)
    parser.add_argument('--fixture', type=Path)
    parser.add_argument('--prepare-only', action='store_true')
    parser.add_argument('--installed-from', type=Path, help='Reuse an owned official client installation')
    parser.add_argument('--address', help='Loopback dedicated-server address for paired clients')
    parser.add_argument('--username', default='DogQA')
    parser.add_argument('--role', choices=['single', 'owner', 'observer'], default='single')
    parser.add_argument('--gui-scale', default='2')
    parser.add_argument('--modmenu', action='store_true', help='Exercise the optional Fabric config entrypoint')
    parser.add_argument('--combined-dir', type=Path, help='Add matching accepted gameplay siblings')
    args = parser.parse_args()
    if os.name != 'posix' or not Path('/proc').exists():
        raise SystemExit('Client QA requires the isolated Linux display workflow')
    repo, api, stage = args.repo.resolve(), args.api.resolve(), args.stage.resolve()
    root = Path('/root/seamless-dogs-production-20261005')
    if stage.exists() or not stage.is_relative_to(root):
        raise SystemExit('Need a fresh owned client profile')
    if args.address and not re.fullmatch(r'127\.0\.0\.1:\d+', args.address):
        raise SystemExit('Paired clients connect only to the owned loopback server')
    if not args.address and (not args.fixture or not (args.fixture / 'level.dat').is_file()):
        raise SystemExit('Single player needs a disposable matching-version world')
    pins = version_catalog(repo / 'gradle/libs.versions.toml')
    minecraft, loader = pins['minecraft'], args.loader
    properties = dict(line.split('=', 1) for line in (repo / 'gradle.properties').read_text().splitlines()
                      if '=' in line and not line.startswith('#'))
    jars = [repo / loader / 'build/libs' / f"seamless-dogs-{properties['mod_version']}-{loader}.jar",
            api / loader / 'build/libs' / f"seamless-api-{pins['seamless-api']}-{loader}.jar",
            repo / 'qa-client/artifacts' / f'seamless-dogs-qa-{loader}.jar']
    for jar in jars:
        if not jar.is_file():
            raise SystemExit(f'Missing packaged jar: {jar}')
    game = stage / 'client'
    mods = game / 'mods'
    mods.mkdir(parents=True)
    for jar in jars:
        shutil.copy2(jar, mods / jar.name)
    if args.combined_dir:
        source = args.combined_dir.resolve()
        workspace = Path('/mnt/c/Users/derko/Desktop/minecraft')
        if not source.is_relative_to(workspace / 'release-candidates'):
            raise SystemExit('Combined QA accepts only owned local release candidates')
        siblings = [jar for jar in source.glob(f'*-{loader}.jar')
                    if not jar.name.startswith(('seamless-api-', 'seamless-dogs-'))]
        if not siblings or any(f'+mc{minecraft}-' not in jar.name for jar in siblings):
            raise SystemExit('Combined candidates do not match the game and loader')
        for jar in siblings:
            shutil.copy2(jar, mods / jar.name)
    if args.modmenu:
        if loader != 'fabric':
            raise SystemExit('Mod Menu is a Fabric optional integration')
        menu = pins['modmenu']
        download(f'https://maven.terraformersmc.com/releases/com/terraformersmc/modmenu/{menu}/modmenu-{menu}.jar', mods / f'modmenu-{menu}.jar')
    shutil.copy2(repo / 'qa-client/options.txt', game / 'options.txt')
    (game / 'config').mkdir()
    (game / 'config/fml.toml').write_text('earlyWindowControl=false\n')
    if args.fixture:
        shutil.copytree(args.fixture, game / 'saves/dogs-world')
    installation = stage / 'installation'
    if args.installed_from:
        source = args.installed_from.resolve()
        if not source.is_relative_to(root) or not (source / 'installation/versions' / minecraft).is_dir():
            raise SystemExit('Cached client must be an owned matching-version profile')
        source_command = json.loads((source / 'launch-command.json').read_text())
        if f'-Dqa.loader={loader}' not in source_command:
            raise SystemExit('Cached client loader does not match')
        runtime = pins['fabric-loader' if loader == 'fabric' else loader]
        expected_id = (f'fabric-loader-{runtime}-{minecraft}' if loader == 'fabric'
                       else f'{minecraft}-forge-{runtime.split("-", 1)[1]}' if loader == 'forge'
                       else f'neoforge-{runtime}')
        if not (source / 'installation/versions' / expected_id / f'{expected_id}.json').is_file():
            raise SystemExit('Cached client loader version does not match the pinned runtime')
        shutil.copytree(source / 'installation', installation)
    else:
        installation.mkdir()
    (installation / 'launcher_profiles.json').write_text('{"profiles":{}}\n')
    vanilla_path = Path('/mnt/c/Users/derko/.gradle/caches/fabric-loom') / minecraft / 'mojang_minecraft_info.json'
    if vanilla_path.is_file():
        vanilla = json.loads(vanilla_path.read_text())
    else:
        manifest_path = stage / 'version-manifest.json'
        download('https://piston-meta.mojang.com/mc/game/version_manifest_v2.json', manifest_path)
        entry = next(row for row in json.loads(manifest_path.read_text())['versions'] if row['id'] == minecraft)
        metadata_path = stage / 'minecraft.json'
        artifact(entry['url'], metadata_path, entry.get('sha1'))
        vanilla = json.loads(metadata_path.read_text())
    version_dir = installation / 'versions' / minecraft
    version_dir.mkdir(parents=True, exist_ok=True)
    (version_dir / f'{minecraft}.json').write_text(json.dumps(vanilla))
    original = version_dir / f'{minecraft}.jar'
    artifact(vanilla['downloads']['client']['url'], original, vanilla['downloads']['client']['sha1'])
    java = ['nice', '-n', '10', 'taskset', '-c', '0,1', args.java, '-XX:ActiveProcessorCount=2']
    installer = stage / 'installer.jar'
    if loader == 'fabric':
        download('https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.1.2/fabric-installer-1.1.2.jar', installer)
        install = java + ['-Xmx1G', '-jar', str(installer), 'client', '-dir', str(installation),
                          '-mcversion', minecraft, '-loader', pins['fabric-loader'], '-noprofile']
        fabric = pins['fabric-api']
        download(f'https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/{fabric}/fabric-api-{fabric}.jar', mods / f'fabric-api-{fabric}.jar')
    else:
        runtime = pins[loader]
        domain, group = ('https://maven.minecraftforge.net', 'net/minecraftforge/forge') if loader == 'forge' else ('https://maven.neoforged.net/releases', 'net/neoforged/neoforge')
        download(f'{domain}/{group}/{runtime}/{loader}-{runtime}-installer.jar', installer)
        install = java + ['-Xmx1G', '-jar', str(installer), '--installClient', str(installation)]
    with (stage / 'installer.log').open('w') as log:
        if args.installed_from:
            log.write(f'Reused official installation from {args.installed_from}\n')
        else:
            subprocess.run(install, stdout=log, stderr=subprocess.STDOUT, cwd=stage, check=True, timeout=600)
    profiles = list(installation.glob('versions/*/*.json'))
    modded_path = next(path for path in profiles if path.parent.name != minecraft)
    modded = json.loads(modded_path.read_text())
    natives = game / 'natives'
    natives.mkdir()
    libraries = {}
    for library in vanilla['libraries'] + modded['libraries']:
        if rules_allow(library.get('rules')):
            pieces = library['name'].split(':')
            key = ':'.join(pieces[:2] + pieces[3:])
            libraries[key] = library
    classpath = []
    for library in libraries.values():
        downloads = library.get('downloads', {})
        info = downloads.get('artifact')
        if info is None:
            group, name, version, *classifier = library['name'].split(':')
            suffix = '-' + classifier[0] if classifier else ''
            path = f"{group.replace('.', '/')}/{name}/{version}/{name}-{version}{suffix}.jar"
            info = {'path': path, 'url': library.get('url', 'https://libraries.minecraft.net/') + path}
        path = installation / 'libraries' / info['path']
        artifact(info.get('url', ''), path, info.get('sha1'))
        classpath.append(str(path))
        native = library.get('natives', {}).get('linux')
        if native:
            native_info = downloads['classifiers'][native.replace('${arch}', '64')]
            native_path = installation / 'libraries' / native_info['path']
            artifact(native_info['url'], native_path, native_info.get('sha1'))
            with zipfile.ZipFile(native_path) as archive:
                for entry in archive.namelist():
                    if entry.endswith('.so'):
                        (natives / Path(entry).name).write_bytes(archive.read(entry))
    # Legacy Forge's official ignoreList excludes ${version_name}.jar from
    # the module scan: patched Minecraft is supplied by its installed libraries.
    # Name the inherited vanilla jar like the selected launcher profile, so that
    # the official exclusion matches and does not create a duplicate module.
    inherited = original
    if any('${version_name}.jar' in value for value in modded.get('arguments', {}).get('jvm', []) if isinstance(value, str)):
        inherited = modded_path.with_suffix('.jar')
        shutil.copy2(original, inherited)
    classpath.append(str(inherited))
    assets = Path('/root/.gradle/caches/fabric-loom/assets')
    index_info = vanilla['assetIndex']
    index_path = assets / 'indexes' / f"{index_info['id']}.json"
    artifact(index_info['url'], index_path, index_info.get('sha1'))
    objects = json.loads(index_path.read_text())['objects']
    def asset_object(info: dict) -> None:
        sha = info['hash']
        artifact(f'https://resources.download.minecraft.net/{sha[:2]}/{sha}', assets / 'objects' / sha[:2] / sha, sha)
    with ThreadPoolExecutor(max_workers=4) as workers:
        unique_objects = {info['hash']: info for info in objects.values()}
        list(workers.map(asset_object, unique_objects.values()))
    offline = bytearray(hashlib.md5(('OfflinePlayer:' + args.username).encode()).digest())
    offline[6] = offline[6] & 15 | 48
    offline[8] = offline[8] & 63 | 128
    replacements = {'auth_player_name': args.username, 'auth_uuid': uuid.UUID(bytes=bytes(offline)).hex,
                    'auth_access_token': '0', 'auth_xuid': '0', 'clientid': '0', 'user_type': 'legacy',
                    'user_properties': '{}', 'version_name': modded['id'], 'version_type': 'release',
                    'game_directory': str(game), 'assets_root': str(assets), 'assets_index_name': index_info['id'],
                    'natives_directory': str(natives), 'launcher_name': 'SeamlessDogsQA', 'launcher_version': '1',
                    'classpath': ':'.join(classpath), 'classpath_separator': ':', 'library_directory': str(installation / 'libraries'),
                    'resolution_width': '1280', 'resolution_height': '720'}
    def expand(value: str) -> str:
        return re.sub(r'\$\{([^}]+)\}', lambda match: replacements[match[1]], value)
    def arguments(source: dict, kind: str) -> list[str]:
        result = []
        for argument in source.get('arguments', {}).get(kind, []):
            if isinstance(argument, str):
                result.append(expand(argument))
            elif rules_allow(argument.get('rules')):
                values = argument['value']
                result.extend(expand(value) for value in ([values] if isinstance(values, str) else values))
        return result
    jvm = arguments(vanilla, 'jvm') + arguments(modded, 'jvm')
    if '-cp' not in jvm and '-classpath' not in jvm:
        jvm += ['-cp', ':'.join(classpath)]
    command = [args.java, '-Xms256M', '-Xmx1G', '-XX:ActiveProcessorCount=2',
               f'-Dqa.minecraft={minecraft}', f'-Dqa.loader={loader}', f'-Dqa.role={args.role}', f'-Dqa.guiScale={args.gui_scale}']
    command += jvm + [modded['mainClass']] + arguments(vanilla, 'game') + arguments(modded, 'game')
    command += ['--quickPlayMultiplayer', args.address] if args.address else ['--quickPlaySingleplayer', 'dogs-world']
    if minecraft.startswith('26.'):
        command += ['--graphicsBackend', 'vulkan']
    (stage / 'launch-command.json').write_text(json.dumps(command, indent=2))
    before = {path.name: digest(path) for path in sorted(mods.glob('*.jar'))}
    (stage / 'SHA256SUMS.before.json').write_text(json.dumps(before, indent=2))
    print(f'Prepared official {minecraft} {loader} client: {stage}', flush=True)
    if args.prepare_only:
        return
    wrapper_source = repo.parent / 'tools/Run-IsolatedMinecraftClient.sh'
    if not wrapper_source.is_file():
        wrapper_source = Path('/mnt/c/Users/derko/Desktop/minecraft/tools/Run-IsolatedMinecraftClient.sh')
    wrapper = stage / 'isolated-client.sh'
    wrapper.write_text(wrapper_source.read_text().replace('flock --nonblock 9', 'flock --wait 600 9'))
    wrapper.chmod(0o755)
    environment = dict(os.environ, ALSOFT_DRIVERS='null', ISOLATED_CLIENT_TIMEOUT_SECONDS='600')
    with (stage / 'client-console.log').open('w') as log:
        result = subprocess.run([str(wrapper), *command], cwd=game, env=environment, stdout=log,
                                stderr=subprocess.STDOUT, timeout=1260)
    if result.returncode:
        raise RuntimeError(f'Packaged client exit {result.returncode}; see {stage / "client-console.log"}')
    passed = game / 'dogs-client-passed.txt'
    if not passed.is_file() or (game / 'dogs-client-failed.txt').exists():
        raise RuntimeError(f'Packaged client failed: {stage}')
    after = {path.name: digest(path) for path in sorted(mods.glob('*.jar'))}
    if before != after:
        raise RuntimeError('Gameplay/test jars changed during client QA')
    print(passed.read_text(), flush=True)


if __name__ == '__main__':
    main()
