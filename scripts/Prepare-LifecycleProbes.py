#!/usr/bin/env python3
"""Generate a separate test project without touching accepted QA/runtime jars."""
import argparse
from pathlib import Path
import shutil
import re

def prepare(repo, template):
    version = next(line.split('=',1)[1].strip().strip('"') for line in (repo/'gradle/libs.versions.toml').read_text().splitlines() if line.startswith('minecraft ='))
    modern = version.startswith('26.')
    gui_split = version in ('26.2','26.3')
    destination = repo/'.qa/lifecycle'
    destination.mkdir(parents=True, exist_ok=True)
    shutil.copytree(repo/'qa-client/src',destination/'src', dirs_exist_ok=True)
    for name in ('build.gradle','settings.gradle'):
        (destination/name).write_text((repo/'qa-client'/name).read_text().replace('../','../../'))
    probe = destination/'src/main/java/qa/dogs/ClientProbe.java'
    source = probe.read_text().replace('private ObserverProbe observer;', 'private ObserverProbe observer;\n    private LifecycleProbe lifecycle;')
    source = source.replace('public void tick(Minecraft client) {','public void tick(Minecraft client) {\n        if (System.getProperty("qa.role", "single").equals("lifecycle")) {\n            if (lifecycle == null) lifecycle = new LifecycleProbe();\n            lifecycle.tick(client); return;\n        }')
    probe.write_text(source)
    lifecycle = template.read_text().replace('WOLF_PACKAGE','' if version in ('1.20.1','1.21.1') else 'wolf.')
    lifecycle = lifecycle.replace('OPEN_SCREEN','client.gui.screen()' if gui_split else 'client.screen')
    lifecycle = lifecycle.replace('OPEN_OVERLAY','client.gui.overlay()' if gui_split else 'client.getOverlay()')
    lifecycle = lifecycle.replace('SET_SCREEN','client.setScreenAndShow' if gui_split else 'client.setScreen')
    lifecycle = lifecycle.replace('SERVER_DATA','new ServerData("Dogs lifecycle", address, false)' if version=='1.20.1' else 'new ServerData("Dogs lifecycle", address, ServerData.Type.OTHER)')
    lifecycle = re.sub(r'\bCONNECT\b','ConnectScreen.startConnecting(new TitleScreen(), client, ServerAddress.parseString(address), data, false'+('' if version=='1.20.1' else ', null')+')',lifecycle)
    lifecycle = re.sub(r'\bDISCONNECT\b','client.clearLevel(new TitleScreen())' if version=='1.20.1' else 'client.disconnect(new TitleScreen())' if version=='1.21.1' else 'client.disconnect(new TitleScreen(), false)',lifecycle)
    (destination/'src/main/java/qa/dogs/LifecycleProbe.java').write_text(lifecycle)
    fixture = destination/'src/main/java/qa/dogs/ServerProbe.java'
    source = fixture.read_text().replace('Commands.literal("dogsqa")','''Commands.literal("dogsqa")
                .then(Commands.literal("dimension").executes(context -> {
                    var server = context.getSource().getServer();
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "execute in minecraft:the_nether run tp DogQA 0.5 90 0.5"); return 1;
                }))
                .then(Commands.literal("home").executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    if (io.github.derkottersberg.seamlessdogs.SeamlessDogs.isPetting(player.getUUID())) throw new IllegalStateException("Server retained a lifecycle session");
                    var server = context.getSource().getServer();
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "execute in minecraft:overworld run tp DogQA 0.5 65 0.5");
                    player.setGameMode(GameType.CREATIVE); return 1;
                }))
                .then(Commands.literal("die").executes(context -> {
                    var server = context.getSource().getServer();
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "kill DogQA"); return 1;
                }))''')
    fixture.write_text(source)
    print(f'Prepared independent lifecycle QA project: {version}')

if __name__=='__main__':
    parser=argparse.ArgumentParser(); parser.add_argument('--workspace',type=Path,required=True); args=parser.parse_args()
    root=args.workspace.resolve(); template=root/'seamless-dogs/qa-client/lifecycle/LifecycleProbe.java.template'
    for version in ('26.3','1.20.1','1.21.1','1.21.11','26.1','26.1.2','26.2'):
        prepare(root/'seamless-dogs' if version=='26.3' else root/f'.ports/dogs-multiversion/mc{version}/seamless-dogs', template)
