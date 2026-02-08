package io.home.staging.model.response;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class GeminiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDeserialization() throws Exception {
        String json = """
                {
                  "body" : {
                    "candidates" : [ {
                      "content" : {
                        "parts" : [ {
                          "inlineData" : {
                            "mimeType" : "image/jpeg",
                            "data" : "dGVzdC1pZGF0YQ=="
                          }
                        } ]
                      }
                    } ]
                  }
                }
                """;

        GeminiResponseWrapper response = objectMapper.readValue(json, GeminiResponseWrapper.class);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().candidates());
        assertEquals(1, response.getBody().candidates().size());

        var candidate = response.getBody().candidates().get(0);
        assertNotNull(candidate.content());
        assertEquals(1, candidate.content().parts().size());

        var part = candidate.content().parts().get(0);
        assertNotNull(part.inlineData());
        assertEquals("image/jpeg", part.inlineData().mimeType());
        assertEquals("dGVzdC1pZGF0YQ==", part.inlineData().data());

        byte[] decoded = Base64.getDecoder().decode(part.inlineData().data());
        assertEquals("test-idata", new String(decoded));
    }
}
