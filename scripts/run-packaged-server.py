#!/usr/bin/env python3
"""Boot exact release jars with official installers, save a wolf, then restart.

Linux-only, loopback-only, disposable profiles. No development Minecraft runtime
or test mod enters this gate. Failed runs and installer output remain available.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import secrets
import shutil
import socket
import struct
import subprocess
import time
import uuid

try:
    import tomllib
except ModuleNotFoundError:  # Windows' existing Python 3.10 is sufficient.
    tomllib = None


def copy_cache_file(source, destination):
    """Share immutable cached JARs; metadata and worlds always remain independent."""
    source_path=Path(source).resolve()
    if source_path.suffix=='.jar' and source_path.is_relative_to(Path('/root/seamless-dogs-production-20261005')):
        try:
            os.link(source_path,destination)
            return str(destination)
        except OSError:
            pass
    return shutil.copy2(source,destination)


def version_catalog(path: Path) -> dict[str, str]:
    source = path.read_text()
    if tomllib is not None:
        return tomllib.loads(source)["versions"]
    section = source.split("[versions]", 1)[1].split("[", 1)[0]
    return dict(re.findall(r'^([\w-]+)\s*=\s*"([^"]+)"\s*$', section, re.MULTILINE))


def download(url: str, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["curl", "--fail", "--location", "--retry", "3", "--silent",
                    "--show-error", url, "--output", str(destination)], check=True, timeout=180)


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def recv_exact(connection: socket.socket, length: int) -> bytes:
    result = bytearray()
    while len(result) < length:
        part = connection.recv(length - len(result))
        if not part:
            raise RuntimeError("RCON closed before the response completed")
        result.extend(part)
    return bytes(result)


def receive(connection: socket.socket) -> tuple[int, int, str]:
    length = struct.unpack("<i", recv_exact(connection, 4))[0]
    if not 10 <= length <= 1048576:
        raise RuntimeError(f"Invalid RCON length {length}")
    payload = recv_exact(connection, length)
    request, kind = struct.unpack("<ii", payload[:8])
    return request, kind, payload[8:-2].decode("utf-8")


def send(connection: socket.socket, request: int, kind: int, text: str) -> None:
    body = struct.pack("<ii", request, kind) + text.encode() + b"\0\0"
    connection.sendall(struct.pack("<i", len(body)) + body)


def rcon(port: int, password: str, command: str) -> str:
    with socket.create_connection(("127.0.0.1", port), timeout=10) as connection:
        connection.settimeout(10)
        send(connection, 101, 3, password)
        request, _, _ = receive(connection)
        if request != 101:
            raise RuntimeError("RCON authentication failed")
        send(connection, 102, 2, command)
        request, _, value = receive(connection)
        if request != 102:
            raise RuntimeError("Unexpected RCON response")
        return value.strip()


def free_port() -> int:
    with socket.socket() as connection:
        connection.bind(("127.0.0.1", 0))
        return connection.getsockname()[1]


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo", type=Path, required=True)
    parser.add_argument("--combined-dir", type=Path, help="Matching sibling suite, including its library")
    parser.add_argument("--loader", choices=["fabric", "forge", "neoforge"], required=True)
    parser.add_argument("--stage", type=Path, required=True)
    parser.add_argument("--java", required=True)
    parser.add_argument("--runtime-loader", help="Optional second loader version compatibility gate")
    parser.add_argument("--gametest-jar", type=Path, help="Test-only native scenarios, never a release dependency")
    parser.add_argument("--installed-from", type=Path, help="Reuse immutable official libraries from an earlier owned profile")
    args = parser.parse_args()
    repo, stage = args.repo.resolve(), args.stage.resolve()
    allowed = [repo / ".qa", Path("/root/seamless-dogs-production-20261005")]
    if stage.exists() or not any(stage.is_relative_to(root.resolve()) for root in allowed):
        raise SystemExit("Need a fresh profile under the owned Dogs QA directory")
    catalog = version_catalog(repo / "gradle/libs.versions.toml")
    properties = dict(line.split("=", 1) for line in (repo / "gradle.properties").read_text().splitlines()
                      if "=" in line and not line.startswith("#"))
    minecraft, loader = catalog["minecraft"], args.loader
    version = properties["mod_version"]
    runtime = args.runtime_loader or catalog["fabric-loader" if loader == "fabric" else loader]
    dog_jar = repo / loader / "build/libs" / f"seamless-dogs-{version}-{loader}.jar"
    for jar in (dog_jar,):
        if not jar.is_file():
            raise SystemExit(f"Missing packaged jar: {jar}")
    stage.mkdir(parents=True)
    mods = stage / "mods"
    mods.mkdir()
    for jar in (dog_jar,):
        shutil.copy2(jar, mods / jar.name)
    if args.combined_dir:
        source=args.combined_dir.resolve()
        workspace=Path('/mnt/c/Users/derko/Desktop/minecraft')
        if not source.is_relative_to(workspace/'release-candidates'): raise SystemExit('Combined inputs must be owned release candidates')
        siblings=list(source.glob(f'*-{loader}.jar'))
        if not siblings or any(f'+mc{minecraft}-' not in jar.name for jar in siblings): raise SystemExit('Combined input version mismatch')
        for jar in siblings:
            if not jar.name.startswith('seamless-dogs-'): shutil.copy2(jar,mods/jar.name)
    if args.gametest_jar:
        shutil.copy2(args.gametest_jar, mods / args.gametest_jar.name)
    base = (["nice", "-n", "10", "taskset", "-c", "0,1"] if os.name == "posix" else [])
    base += [args.java, "-XX:ActiveProcessorCount=2"]
    installer = stage / "installer.jar"
    if loader == "fabric":
        download("https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.1.2/fabric-installer-1.1.2.jar", installer)
        install = base + ["-Xmx1G", "-jar", str(installer), "server", "-mcversion", minecraft,
                          "-loader", runtime, "-downloadMinecraft"]
        launch = base + ["-Xms256M", "-Xmx1G", "-jar", "fabric-server-launch.jar", "nogui"]
        fabric = catalog["fabric-api"]
        download(f"https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/{fabric}/fabric-api-{fabric}.jar",
                 mods / f"fabric-api-{fabric}.jar")
    else:
        domain, group = ("https://maven.minecraftforge.net", "net/minecraftforge/forge") if loader == "forge" else ("https://maven.neoforged.net/releases", "net/neoforged/neoforge")
        download(f"{domain}/{group}/{runtime}/{loader}-{runtime}-installer.jar", installer)
        install = base + ["-Xmx1G", "-jar", str(installer), "--installServer"]
        argument_file = "unix_args.txt" if os.name == "posix" else "win_args.txt"
        launch = base + ["-Xms256M", "-Xmx1G", f"@libraries/{group}/{runtime}/{argument_file}", "nogui"]
    if args.installed_from:
        source = args.installed_from.resolve()
        if loader == "fabric" or not any(source.is_relative_to(root.resolve()) for root in allowed):
            raise SystemExit("Installer reuse requires an owned Forge/NeoForge profile")
        required = source / "libraries" / group / runtime / argument_file
        if not required.is_file():
            raise SystemExit("Cached installation does not match the selected loader version")
        shutil.copytree(source / "libraries", stage / "libraries", copy_function=copy_cache_file)
        for shim in source.glob("*-shim.jar"):
            shutil.copy2(shim, stage / shim.name)
        (stage / "installer.log").write_text(f"Reused official installer libraries from {source}\n")
    else:
        with (stage / "installer.log").open("w") as output:
            subprocess.run(install, cwd=stage, stdout=output, stderr=subprocess.STDOUT, check=True, timeout=600)
    if args.gametest_jar:
        launch.insert(len(base), "-Dforge.enableGameTest=true")
    hashes = {jar.name: digest(jar) for jar in sorted(mods.glob("*.jar"))}
    (stage / "SHA256SUMS.before.txt").write_text("".join(f"{sha}  mods/{name}\n" for name, sha in hashes.items()))
    password, port = secrets.token_hex(16), free_port()
    (stage / "eula.txt").write_text("eula=true\n")
    (stage / "server.properties").write_text(
        f"server-ip=127.0.0.1\nserver-port=0\nlevel-name=dogs-world\nonline-mode=false\n"
        f"enable-rcon=true\nrcon.port={port}\nrcon.password={password}\n"
        "spawn-protection=0\nallow-flight=true\nview-distance=3\nsimulation-distance=3\n"
        "max-tick-time=120000\npause-when-empty-seconds=0\nlevel-type=minecraft:flat\ngenerate-structures=false\n"
        'generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}\n')
    owner = uuid.UUID("b2cabd7c-5728-4ade-8e27-518d1af98780")
    owner_ints = struct.unpack(">iiii", owner.bytes)
    owner_nbt = "[I;" + ",".join(map(str, owner_ints)) + "]"
    selector = "@e[type=minecraft:wolf,tag=dogs_persistence,limit=1]"
    records: list[dict[str, str]] = []
    for run in (1, 2):
        log_path = stage / f"server-{run}.log"
        with log_path.open("w") as log:
            process = subprocess.Popen(launch, cwd=stage, stdin=subprocess.DEVNULL, stdout=log, stderr=subprocess.STDOUT,
                                       creationflags=subprocess.CREATE_NO_WINDOW if os.name == "nt" else 0)
            try:
                deadline = time.monotonic() + 180
                while "Done (" not in log_path.read_text(errors="replace"):
                    if process.poll() is not None or time.monotonic() > deadline:
                        raise RuntimeError(f"Packaged server did not start; see {log_path}")
                    time.sleep(1)
                commands: dict[str, str] = {}
                if "Couldn't load mod:seamless" in log_path.read_text(errors="replace"):
                    raise RuntimeError(f"Invalid mod resource metadata: {log_path}")
                def command(value: str) -> str:
                    result = rcon(port, password, value)
                    commands[value] = result
                    (stage / f"commands-{run}.json").write_text(json.dumps(commands, indent=2))
                    return result
                command("forceload add 0 0")
                if run == 1:
                    command("fill -3 64 -3 3 64 3 minecraft:grass_block")
                    command("fill -3 65 -3 3 70 3 minecraft:air")
                    result = command("summon minecraft:wolf 0.5 65 0.5 {Tags:[\"dogs_persistence\"],"
                        f"Owner:{owner_nbt},owner:{owner_nbt},Sitting:1b,sitting:1b,Health:4f,health:4f,"
                        "NoAI:1b,NoGravity:1b,PersistenceRequired:1b}")
                    if "Summoned" not in result:
                        raise RuntimeError(f"Failed to summon persistence fixture: {result}")
                    if args.gametest_jar:
                        command("forceload add -16 -16 64 16")
                        time.sleep(2)
                        names = ["ownerCanPet", "rejectInvalidRequests", "cooldownAndCancellation", "codecRoundTrip"]
                        for index, name in enumerate(names):
                            scenario = name.lower()
                            response = command(f"execute positioned {index * 15} 75 0 run test run seamlessdogs:{scenario}")
                            if "Unknown" in response or "Incorrect" in response:
                                raise RuntimeError(f"Native scenario was not discovered: {response}")
                            command("tick unfreeze")
                            deadline = time.monotonic() + 90
                            while f"DOGS_GAMETEST_PASS {name}" not in log_path.read_text(errors="replace"):
                                if process.poll() is not None or time.monotonic() > deadline:
                                    raise RuntimeError(f"Native scenario {name} failed or was not executed: {log_path}")
                                time.sleep(0.5)
                # Give loaded entity sections a few ticks to settle after restart.
                time.sleep(2)
                state: dict[str, str] = {}
                for logical, possible in {"owner": ["Owner", "owner"], "sitting": ["Sitting", "sitting"],
                                          "health": ["Health", "health"], "uuid": ["UUID", "uuid"]}.items():
                    for key in possible:
                        value = command(f"data get entity {selector} {key}")
                        if "entity data" in value:
                            state[logical] = value.split("entity data: ", 1)[-1]
                            break
                    if logical not in state:
                        raise RuntimeError(f"Missing wolf {logical}: {commands}")
                # 1.20.1 vanilla Wolf#setTame heals to 20 while loading Owner
                # NBT. Use its full-health state for persistence; the native
                # wounded-dog scenario separately verifies petting never heals.
                expected_health = "20.0f" if minecraft == "1.20.1" else "4.0f"
                if state["owner"].replace(" ", "") != owner_nbt or state["sitting"] != "1b" or state["health"] != expected_health:
                    raise RuntimeError(f"Unexpected saved tame wolf state: {state}")
                if records and state != records[0]:
                    raise RuntimeError(f"Wolf changed after save/restart: {records[0]} -> {state}")
                records.append(state)
                command("save-all flush")
                command("stop")
                process.wait(timeout=60)
                if process.returncode != 0:
                    raise RuntimeError(f"Server shutdown exit {process.returncode}")
                (stage / f"commands-{run}.json").write_text(json.dumps(commands, indent=2))
            finally:
                if process.poll() is None:
                    process.terminate()
                    try:
                        process.wait(timeout=15)
                    except subprocess.TimeoutExpired:
                        process.kill()
                        process.wait()
    after = {jar.name: digest(jar) for jar in sorted(mods.glob("*.jar"))}
    if hashes != after:
        raise RuntimeError("Packaged jars changed during the gate")
    result = {"minecraft": minecraft, "loader": loader, "runtimeLoader": runtime,
              "jars": hashes, "wolfBefore": records[0], "wolfAfter": records[1],
              "gate": "official packaged server; clean startup/save/restart; owner/sitting/health/UUID preserved"}
    (stage / "server-passed.json").write_text(json.dumps(result, indent=2) + "\n")
    print(f"PASS packaged {minecraft} {loader} {runtime}: save/restart preserves tamed wolf", flush=True)


if __name__ == "__main__":
    main()
