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
            prompt = booleanValue(properties,"prompt");
            eyes = booleanValue(properties,"eyes");
            animation = booleanValue(properties,"animation");
        } catch (Exception e) { LoggerFactory.getLogger("seamlessdogs").warn("Could not load client settings", e); }
    }
    public static void save(boolean newPrompt, boolean newEyes, boolean newAnimation) {
        var properties = new Properties();
        if(Files.exists(file))try(var reader=Files.newBufferedReader(file)){properties.load(reader);}
        catch(Exception e){throw new IllegalArgumentException("Existing client settings are unreadable; original preserved.");}
        properties.setProperty("prompt", Boolean.toString(newPrompt));
        properties.setProperty("eyes", Boolean.toString(newEyes));
        properties.setProperty("animation", Boolean.toString(newAnimation));
        try {
            Files.createDirectories(file.getParent());
            Path temporary=file.resolveSibling(file.getFileName()+".tmp");
            try (var writer = Files.newBufferedWriter(temporary)) { properties.store(writer, "Seamless Dogs - this client's visuals only"); }
            if(Files.exists(file))Files.copy(file,file.resolveSibling(file.getFileName()+".bak"),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            try { Files.move(temporary,file,java.nio.file.StandardCopyOption.ATOMIC_MOVE,java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
            catch(java.nio.file.AtomicMoveNotSupportedException ignored) { Files.move(temporary,file,java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
        } catch (Exception e) { throw new IllegalArgumentException("Could not save settings: " + e.getMessage()); }
        prompt = newPrompt; eyes = newEyes; animation = newAnimation;
    }
    private static boolean booleanValue(Properties values,String key) {
        String value=values.getProperty(key,"true").trim();
        if(value.equalsIgnoreCase("true"))return true;
        if(value.equalsIgnoreCase("false"))return false;
        LoggerFactory.getLogger("seamlessdogs").warn("Invalid client setting {}; using its default until saved",key);return true;
    }
}
