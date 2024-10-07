package dev.spaxter.lynxlib.unittest.jsonconfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.spaxter.lynxlib.jsonconfig.JsonTools;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.IOException;

class JsonToolsTests {

    static class TestObject {
        public String name;
        public int age;

        public TestObject() {
        }

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    void serializeValidJson_ShouldSerialize() throws IOException {
        // Arrange
        TestObject obj = new TestObject("John", 25);

        // Act
        String json = JsonTools.serialize(obj);

        // Assert
        assertTrue(json.contains("\"name\" : \"John\""));
        assertTrue(json.contains("\"age\" : 25"));
    }

    @Test
    void deserializeValidJson_ShouldDeserialize() throws IOException {
        // Arrange
        String json = "{'name' : 'Jane', 'age' : 30}".replace("'", "\"");

        // Act
        TestObject obj = JsonTools.deserialize(json, TestObject.class);

        // Assert
        assertEquals(obj.name, "Jane");
        assertEquals(obj.age, 30);
    }

    @Test
    void deserializeInvalidJson_ShouldThrowException() {
        // Arrange
        String invalidJson = "{invalid";

        // Assert
        assertThrows(JsonProcessingException.class, () -> JsonTools.deserialize(invalidJson, TestObject.class));
    }
}
