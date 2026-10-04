"""Repository-local build scaffolding, kept reproducible for each modern version branch."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]

def write(relative, text):
    target = ROOT / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text, encoding="utf-8", newline="\n")

original = ROOT / "fabric/src/gametest/java/io/github/derkottersberg/seamlessdogs/fabric/gametest/DogsGameTests.java"
scenario = ROOT / "common/src/gametest/java/io/github/derkottersberg/seamlessdogs/gametest/DogsScenarios.java"
names = ["ownerCanPet", "rejectInvalidRequests", "cooldownAndCancellation", "codecRoundTrip"]
if not scenario.exists():
    import re
    source = original.read_text(encoding="utf-8")
    source = source.replace("package io.github.derkottersberg.seamlessdogs.fabric.gametest;", "package io.github.derkottersberg.seamlessdogs.gametest;")
    source = source.replace("import net.fabricmc.fabric.api.gametest.v1.GameTest;\n", "")
    source = source.replace("class DogsGameTests", "class DogsScenarios").replace("private Fixture fixture", "private static Fixture fixture")
    source = re.sub(r"    @GameTest\([^\n]+\)\n", "", source)
    source = source.replace("public void ", "public static void ")
    write(scenario.relative_to(ROOT), source)
write(original.relative_to(ROOT), """package io.github.derkottersberg.seamlessdogs.fabric.gametest;
import io.github.derkottersberg.seamlessdogs.gametest.DogsScenarios;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
public final class DogsGameTests {
""" + "\n".join(f"    @GameTest(maxTicks = 80) public void {name}(GameTestHelper helper) {{ DogsScenarios.{name}(helper); }}" for name in names) + "\n}\n")

for loader in ["forge", "neoforge"]:
    write(f"{loader}/src/gametest/resources/pack.mcmeta", (ROOT / "common/src/main/resources/pack.mcmeta").read_text(encoding="utf-8"))
    is_forge = loader == "forge"
    platform_dep = "forge libs.forge" if is_forge else "neoForge libs.neoforge"
    metadata_path = "META-INF/mods.toml" if is_forge else "META-INF/neoforge.mods.toml"
    naming = """configurations.forgeRuntimeLibrary {
    exclude group: 'dev.architectury', module: 'architectury-naming-service'
    exclude group: 'dev.architectury', module: 'architectury-mixin-remapper-service'
}
""" if is_forge else ""
    run_props = """            def identity = rootProject.file('gradle/official-named-empty.tiny').absolutePath
            property 'architectury.naming.mappingsPath', identity
            property 'architectury.naming.sourceNamespace', 'official'
            property 'architectury.mixinRemapper.mappingsPath', identity
            property 'architectury.mixinRemapper.sourceNamespace', 'official'
""" if is_forge else ""
    write(f"{loader}/build.gradle", """plugins { id 'dev.architectury.loom-no-remap' }
base { archivesName = rootProject.archives_base_name }
sourceSets.main {
    java.srcDir rootProject.file('common/src/main/java')
    resources.srcDir rootProject.file('common/src/main/resources')
}
sourceSets {
    gametest {
        java.srcDirs = ['src/gametest/java', rootProject.file('common/src/gametest/java')]
        resources.srcDirs = ['src/gametest/resources']
        compileClasspath += sourceSets.main.output + sourceSets.main.compileClasspath
        runtimeClasspath += output + compileClasspath + sourceSets.main.runtimeClasspath
    }
}
configurations {
    gametestImplementation.extendsFrom implementation
    gametestRuntimeOnly.extendsFrom runtimeOnly
}
""" + naming + f"""dependencies {{
    minecraft libs.minecraft
    {platform_dep}
    def apiJar = files(new File(rootProject.seamlessApiDirectory, "{loader}/build/libs/seamless-api-${{libs.versions.seamless.api.get()}}-{loader}.jar"))
    apiJar.builtBy gradle.includedBuild('seamless-api').task(':{loader}:jar')
    implementation apiJar
}}
loom {{
    mods {{
        seamlessdogs {{ sourceSet sourceSets.main }}
        seamlessdogstests {{ sourceSet sourceSets.gametest }}
    }}
    runs {{
        gameTestServer {{
            runtimeEnvironment = 'gametestserver'
            forgeTemplate 'gameTestServer'
            source sourceSets.gametest
            runDir 'build/gametest'
{run_props}            property '{loader}.enabledGameTestNamespaces', 'seamlessdogs'
        }}
    }}
}}
tasks.named('runGameTestServer') {{
    doLast {{
        def logFile = layout.buildDirectory.file('gametest/logs/latest.log').get().asFile
        def log = logFile.exists() ? logFile.text : ''
        {names!r}.each {{ name ->
            if (!log.contains("DOGS_GAMETEST_PASS ${{name}}")) throw new GradleException("Missing/pending/failed dog scenario ${{name}}")
        }}
        if (!(log =~ /All \d+ required tests passed/)) throw new GradleException('Native GameTests did not finish successfully')
    }}
}}
tasks.named('check') {{ dependsOn 'runGameTestServer' }}
processResources {{
    inputs.property 'version', project.version
    filesMatching('{metadata_path}') {{ expand version: project.version }}
}}
tasks.named('jar', Jar) {{
    from rootProject.file('LICENSE')
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {{ attributes 'Automatic-Module-Name': 'io.github.derkottersberg.seamlessdogs', 'MixinConfigs': 'seamlessdogs.mixins.json' }}
    archiveFileName = "seamless-dogs-${{project.version}}-{loader}.jar"
}}
tasks.named('sourcesJar', Jar) {{
    from project(':common').sourceSets.main.allSource
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName = "seamless-dogs-${{project.version}}-{loader}-sources.jar"
}}
""")
    native_min = "[66.0.9,67)" if is_forge else "[26.3.0.48-beta,26.4)"
    loader_min = "[66,)" if is_forge else "[11,)"
    required = "mandatory=true" if is_forge else 'type="required"'
    icon_key = "logoFile" if is_forge else "iconFile"
    write(f"{loader}/src/main/resources/{metadata_path}", f'''modLoader="javafml"
loaderVersion="{loader_min}"
license="All rights reserved"
[[mods]]
modId="seamlessdogs"
version="${{version}}"
displayName="Seamless Dogs"
authors="Derk Ottersberg"
{icon_key}="assets/seamlessdogs/icon.png"
description="Pet your own tamed wolves with an empty hand and a rebindable key. Dogs respond with expressive eyes, head and tail animation, and their own voice."
[[mixins]]
config="seamlessdogs.mixins.json"
[features.seamlessdogs]
javaVersion="[25,)"
[[dependencies.seamlessdogs]]
modId="{loader}"
{required}
versionRange="{native_min}"
ordering="NONE"
side="BOTH"
[[dependencies.seamlessdogs]]
modId="minecraft"
{required}
versionRange="[26.3]"
ordering="NONE"
side="BOTH"
[[dependencies.seamlessdogs]]
modId="seamlessapi"
{required}
versionRange="[2.0.2,3.0.0)"
ordering="AFTER"
side="BOTH"
''')
    write(f"{loader}/src/gametest/resources/{metadata_path}", f'''modLoader="javafml"
loaderVersion="{loader_min}"
license="All rights reserved"
[[mods]]
modId="seamlessdogstests"
version="1"
displayName="Seamless Dogs test driver (never distributed)"
[[dependencies.seamlessdogstests]]
modId="seamlessdogs"
{required}
versionRange="[0.1,)"
ordering="AFTER"
side="BOTH"
''')

    pkg = f"{loader}/src/gametest/java/io/github/derkottersberg/seamlessdogs/{loader}/gametest/DogsNativeGameTests.java"
    if is_forge:
        write(pkg, """package io.github.derkottersberg.seamlessdogs.forge.gametest;
import io.github.derkottersberg.seamlessdogs.gametest.DogsScenarios;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
@Mod("seamlessdogstests")
public final class DogsNativeGameTests {
    public DogsNativeGameTests(FMLJavaModLoadingContext context) {
        RegisterEvent.getBus(context.getModBusGroup()).addListener(event -> {
            if (event.getRegistryKey() != Registries.TEST_FUNCTION) return;
""" + "\n".join(f'            event.register(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "{name.lower()}"), () -> DogsScenarios::{name});' for name in names) + "\n        });\n    }\n}\n")
        for name in names:
            write(f"forge/src/gametest/resources/data/seamlessdogs/test_instance/{name.lower()}.json", json.dumps({
                "type": "minecraft:function", "environment": "minecraft:default",
                "function": f"seamlessdogs:{name.lower()}", "max_ticks": 80, "structure": "forge:empty3x3x3"
            }, indent=2) + "\n")
    else:
        write(pkg, """package io.github.derkottersberg.seamlessdogs.neoforge.gametest;
import io.github.derkottersberg.seamlessdogs.gametest.DogsScenarios;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
@Mod("seamlessdogstests")
public final class DogsNativeGameTests {
    public DogsNativeGameTests(IEventBus bus) {
        var functions = DeferredRegister.<Consumer<GameTestHelper>>create(Registries.TEST_FUNCTION, "seamlessdogs");
""" + "\n".join(f'        functions.register("{name.lower()}", () -> DogsScenarios::{name});' for name in names) + """
        functions.register(bus);
        bus.addListener((RegisterGameTestsEvent event) -> {
            var environment = event.registerEnvironment(Identifier.fromNamespaceAndPath("seamlessdogs", "test_environment"), new TestEnvironmentDefinition.AllOf());
""" + "\n".join(f'            event.registerTest(Identifier.fromNamespaceAndPath("seamlessdogs", "{name.lower()}"), new FunctionGameTestInstance(net.minecraft.resources.ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "{name.lower()}")), new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 80, 0, true)));' for name in names) + "\n        });\n    }\n}\n")
