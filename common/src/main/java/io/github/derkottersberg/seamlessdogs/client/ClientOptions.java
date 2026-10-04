package io.github.derkottersberg.seamlessdogs.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.slf4j.LoggerFactory;

public final class ClientOptions {
    public static boolean prompt = true, eyes = true, animation = true;
    private static Path file;
    public static void load(Path directory) {
        file = directory.resolve("seamlessdogs-client.properties");
        if (!Files.exists(file)) return;
        try (var reader = Files.newBufferedReader(file)) {
            var properties = new Properties(); properties.load(reader);
            prompt = Boolean.parseBoolean(properties.getProperty("prompt", "true"));
            eyes = Boolean.parseBoolean(properties.getProperty("eyes", "true"));
            animation = Boolean.parseBoolean(properties.getProperty("animation", "true"));
        } catch (Exception e) { LoggerFactory.getLogger("seamlessdogs").warn("Could not load client settings", e); }
    }
    public static void save(boolean newPrompt, boolean newEyes, boolean newAnimation) {
        var properties = new Properties();
        properties.setProperty("prompt", Boolean.toString(newPrompt));
        properties.setProperty("eyes", Boolean.toString(newEyes));
        properties.setProperty("animation", Boolean.toString(newAnimation));
        try {
            Files.createDirectories(file.getParent());
            try (var writer = Files.newBufferedWriter(file)) { properties.store(writer, "Seamless Dogs - this client's visuals only"); }
        } catch (Exception e) { throw new IllegalArgumentException("Could not save settings: " + e.getMessage()); }
        prompt = newPrompt; eyes = newEyes; animation = newAnimation;
    }
}
