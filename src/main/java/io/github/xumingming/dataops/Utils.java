package io.github.xumingming.dataops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

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
}
