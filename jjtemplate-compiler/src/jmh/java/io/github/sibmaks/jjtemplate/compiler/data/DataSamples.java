package io.github.sibmaks.jjtemplate.compiler.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Benchmark data set provider
 *
 * @author sibmaks
 * @since 0.4.1
 */
public final class DataSamples {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

    /**
     * Get benchmark data set, based on scenario
     *
     * @param scenario scenario
     * @return data set
     */
    public static Map<String, Object> byName(Scenario scenario) {
        return load(String.format("data/%s.json", scenario.dataset.name().toLowerCase()));
    }

    private static DataSample load(String path) {
        try (var is = DataSamples.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            return OBJECT_MAPPER.readValue(is, DataSample.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static final class DataSample extends HashMap<String, Object> {
    }
}
