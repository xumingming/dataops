package io.github.xumingming.dataops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class Utils
{
    private Utils() {}

    public static ObjectMapper getObjectMapper()
    {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        mapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    public static String read(String filePath)
    {
        try (BufferedReader newReader = newReader(new File(filePath), StandardCharsets.UTF_8)) {
            List<String> lines = newReader.lines().collect(Collectors.toList());
            String sql = String.join("\n", lines);
            return sql;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedReader newReader(File file, Charset charset)
            throws FileNotFoundException
    {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
    }
}
