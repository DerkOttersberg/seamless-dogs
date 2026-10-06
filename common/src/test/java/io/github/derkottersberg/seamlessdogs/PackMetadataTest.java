package io.github.derkottersberg.seamlessdogs;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
class PackMetadataTest {
 @Test void packagedMetadataMatchesMinecraftLine()throws Exception{
  var pack=JsonParser.parseString(Files.readString(Path.of("src/main/resources/pack.mcmeta"))).getAsJsonObject().getAsJsonObject("pack");
  assertEquals(34,pack.get("pack_format").getAsInt());
 }
}
