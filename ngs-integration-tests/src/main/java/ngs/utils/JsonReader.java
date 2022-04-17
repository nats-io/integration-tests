package ngs.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ngs.claim.Payload;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonReader {

    public static Payload getNatsClaims(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, Payload.class);
    }

    public static Payload getNatsClaims(byte[] jsonBytes) throws IOException {
        return getNatsClaims(new String(jsonBytes, StandardCharsets.UTF_8));
    }

    public static Payload getNatsClaims(Path path) throws IOException {
        return getNatsClaims(Files.readAllBytes(path));
    }

    public static Payload getNatsClaims(File file) throws IOException {
        return getNatsClaims(file.toPath());
    }

    public static Payload getNatsClaimsFromFile(String filename) throws IOException {
        return getNatsClaims(new File(filename));
    }
}
