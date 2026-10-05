#!/usr/bin/env python3
"""Assemble local release files only after every exact-jar feature gate passed."""
from __future__ import annotations
import argparse
import hashlib
import json
from pathlib import Path
import runpy
import shutil
import subprocess
import zipfile

VERSIONS = ('1.20.1', '1.21.1', '1.21.11', '26.1', '26.1.2', '26.2', '26.3')
HELPER = runpy.run_path(str(Path(__file__).with_name('run-packaged-server.py')))


def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def commit(repo): return subprocess.check_output(['git', '-C', str(repo), 'rev-parse', 'HEAD'], text=True).strip()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--workspace', type=Path, required=True)
    parser.add_argument('--acceptance', type=Path, required=True, help='Consolidated exact-jar matrix and evidence')
    parser.add_argument('--destination', type=Path, required=True)
    args = parser.parse_args()
    root, target = args.workspace.resolve(), args.destination.resolve()
    if target.exists() or not target.is_relative_to(root / 'release-candidates'):
        raise SystemExit('Need a new owned release-candidate directory')
    acceptance = json.loads(args.acceptance.read_text())
    if acceptance.get('complete') is not True or len(acceptance.get('optionalAndCombined', [])) != 10:
        raise SystemExit('Complete feature, lifecycle and optional/combined acceptance is required')
    expected_cells = {(version, loader) for version in VERSIONS
                      for loader in ('fabric', 'forge') + (() if version == '1.20.1' else ('neoforge',))}
    records = acceptance['cells']
    if len(records) != 20 or {(row['minecraft'], row['loader']) for row in records} != expected_cells:
        raise SystemExit('The complete 20-cell matrix is required')
    prepared = []
    for row in records:
        version, loader = row['minecraft'], row['loader']
        if not all(row.get(name) is True for name in ('buildPassed', 'nativePassed', 'serverPassed', 'clientPassed', 'multiplayerPassed', 'lifecyclePassed')):
            raise SystemExit(f'Incomplete acceptance: {version}/{loader}')
        repo = root / 'seamless-dogs' if version == '26.3' else root / f'.ports/dogs-multiversion/mc{version}/seamless-dogs'
        api = root / '.ports/github-mc26.3/seamless-api' if version == '26.3' else repo.parent / 'seamless-api'
        pins = HELPER['version_catalog'](repo / 'gradle/libs.versions.toml')
        dogs = list((repo / loader / 'build/libs').glob(f'*-{loader}.jar'))
        library = list((api / loader / 'build/libs').glob(f'*-{loader}.jar'))
        if len(dogs) != 1 or len(library) != 1:
            raise SystemExit(f'Ambiguous runtime output: {version}/{loader}')
        jars = dogs + library
        if not all(row['jars'].get(jar.name) == sha(jar) for jar in jars):
            raise SystemExit(f'Runtime bytes changed after acceptance: {version}/{loader}')
        with zipfile.ZipFile(dogs[0]) as archive:
            if any(name.startswith('qa/') or '/gametest/' in name for name in archive.namelist()):
                raise SystemExit('Test code found in release jar')
        prepared.append((row, repo, api, pins, jars))
    target.mkdir(parents=True)
    manifest = {'product': 'Seamless Dogs', 'license': 'All rights reserved', 'publication': 'local only',
                'localAcceptance': 'passed', 'hostedCi': 'not executed; matching unpublished API refs require publication first',
                'sourceBranches': list(VERSIONS), 'artifacts': [], 'acceptance': acceptance}
    api_license = (root / '.ports/dogs-multiversion/mc1.21.1/seamless-api/LICENSE').read_text()
    for row, repo, api, pins, jars in prepared:
        version, loader = row['minecraft'], row['loader']
        folder = target / f'mc{version}' / loader
        mods = folder / 'mods'; mods.mkdir(parents=True)
        for jar in jars: shutil.copy2(jar, mods / jar.name)
        (folder / 'LICENSE-Seamless-Dogs.txt').write_text((repo / 'LICENSE').read_text())
        (folder / 'LICENSE-SeamlessLib-MIT.txt').write_text(api_license)
        extra = (f"Fabric API {pins['fabric-api']} is also required; download the official jar:\n"
                 f"https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/{pins['fabric-api']}/fabric-api-{pins['fabric-api']}.jar\n") if loader == 'fabric' else ''
        (folder / 'INSTALL.txt').write_text(
            f"Seamless Dogs — Minecraft Java {version}, {loader}, Java {pins['java']}\n\n"
            f"Install the selected loader ({pins['fabric-loader' if loader == 'fabric' else loader]}).\n"
            "Copy both mods/*.jar files to the matching client/server mods folder.\n" + extra +
            "Choose exactly one Minecraft/loader combination; never mix branches.\n\n"
            "Look at your own tamed wolf within three blocks with an empty main\n"
            "hand and press G. Rebind G in Options > Controls > Seamless Dogs.\n"
            "The server and participating clients need Dogs and SeamlessLib.\n"
            "Petting lasts two seconds, with a three-second cooldown from its start.\n"
            "It does not heal, feed, tame or change sitting. The wolf has expressions\n"
            "and a spatial pant; the hand/arm animates in both perspectives.\n\n"
            "Configure visuals through Mod Menu (Fabric) or the native Mods screen.\n"
            "Client config: config/seamlessdogs-client.properties.\n"
            "Motion is stylized; custom texture UVs can disable eye expressions.\n"
            + ("This NeoForge runtime is an upstream beta.\n" if loader == 'neoforge' and 'beta' in pins[loader] else ''))
        sums = ''.join(f'{sha(jar)}  mods/{jar.name}\n' for jar in jars)
        (folder / 'SHA256SUMS.txt').write_text(sums)
        archive_path = target / f'seamless-dogs-mc{version}-{loader}-install.zip'
        with zipfile.ZipFile(archive_path, 'w', zipfile.ZIP_DEFLATED) as archive:
            for path in sorted(folder.rglob('*')):
                if path.is_file(): archive.write(path, path.relative_to(folder).as_posix())
        manifest['artifacts'].append({'minecraft': version, 'loader': loader, 'java': pins['java'],
            'loaderVersion': pins['fabric-loader' if loader == 'fabric' else loader],
            'fabricApi': pins['fabric-api'] if loader == 'fabric' else None,
            'optionalModMenu': pins['modmenu'] if loader == 'fabric' else None,
            'sourceCommit': commit(repo), 'librarySourceCommit': commit(api),
            'jars': {jar.name: sha(jar) for jar in jars},
            'installZip': archive_path.name, 'installZipSha256': sha(archive_path)})
    (target / 'artifacts.json').write_text(json.dumps(manifest, indent=2) + '\n')
    (target / 'README.md').write_text(
        '# Seamless Dogs — verified local release candidates\n\n'
        'Twenty loader/version builds are organized by Minecraft version and loader.\n'
        'Each install zip contains Dogs plus the matching independent MIT SeamlessLib.\n'
        'Fabric API is downloaded separately using the exact link in INSTALL.txt.\n'
        'Source/dependency commits, SHA-256 hashes and actual feature evidence are\n'
        'recorded in artifacts.json. Test drivers, game libraries, Minecraft assets,\n'
        'worlds, development jars and source jars are excluded.\n\n'
        'Java 17: 1.20.1. Java 21: 1.21.1/1.21.11. Java 25: 26.x.\n'
        'NeoForge 1.20.1 is excluded; NeoForge 26.1/26.3 pins are upstream betas.\n'
        'Background software-rendered acceptance does not prove all hardware or\n'
        'arbitrary mod packs. Public publishing and hosted CI have not run.\n')
    print(f'Prepared 20 accepted local install bundles: {target}')


if __name__ == '__main__': main()
