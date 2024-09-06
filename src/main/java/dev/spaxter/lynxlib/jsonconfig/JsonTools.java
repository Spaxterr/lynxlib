package dev.spaxter.lynxlib.jsonconfig;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;

/**
 * Utility class for serializing and deserializing JSON.
 */
public class JsonTools {

    /**
     * Serialize an object into a JSON string.
     *
     * @param object The object to serialize.
     * @return JSON string
     * @throws JsonProcessingException If the JSON conversion fails.
     */
    public static String serialize(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    /**
     * Deserialize a JSON string to an object.
     *
     * @param json  The JSON string.
     * @param clazz The class to create.
     * @param <T>   Type to construct
     * @return Object constructed from JSON
     * @throws JsonProcessingException If the JSON conversion fails.
     */
    public static <T> T deserialize(String json, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        return mapper.readValue(json, clazz);
    }
}
