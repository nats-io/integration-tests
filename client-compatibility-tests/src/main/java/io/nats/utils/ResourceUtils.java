package io.nats.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@SuppressWarnings("DataFlowIssue")
public abstract class ResourceUtils {
    public static List<String> dataAsLines(String fileName) {
        return resourceAsLines("data/" + fileName);
    }

    public static String dataAsString(String fileName) {
        return resourceAsString("data/" + fileName);
    }

    public static List<String> resourceAsLines(String fileName) {
        try {
            ClassLoader classLoader = ResourceUtils.class.getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            return Files.readAllLines(file.toPath());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static File resourceAsFile(String fileName) {
        try {
            ClassLoader classLoader = ResourceUtils.class.getClassLoader();
            return new File(classLoader.getResource(fileName).getFile());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] resourceAsBytes(String fileName) {
        try {
            ClassLoader classLoader = ResourceUtils.class.getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            return Files.readAllBytes(file.toPath());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String resourceAsString(String fileName) {
        try {
            ClassLoader classLoader = ResourceUtils.class.getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream resourceAsInputStream(String fileName) {
        try {
            return ResourceUtils.class.getClassLoader().getResourceAsStream(fileName);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String createTempFile(String prefix, String suffix, String[] lines) throws IOException {
        File f = File.createTempFile(prefix, suffix);
        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
        for (String line : lines) {
            writer.write(line);
            writer.write(System.lineSeparator());
        }
        writer.flush();
        writer.close();
        return f.getAbsolutePath();
    }
}
