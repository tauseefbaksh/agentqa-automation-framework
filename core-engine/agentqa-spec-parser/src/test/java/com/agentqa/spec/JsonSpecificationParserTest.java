package com.agentqa.spec;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JsonSpecificationParserTest {
    private final JsonSpecificationParser parser = new JsonSpecificationParser();

    @Test
    void parsesResourceWithUiApiStepsAndAssertions() {
        TestSpecification specification = parser.parseResource("specs/login.json");

        assertEquals("User login", specification.getName());
        assertEquals(2, specification.getSteps().size());
        assertEquals(StepType.UI, specification.getSteps().get(0).getType());
        assertEquals(200, specification.getAssertions().get(0).getExpected());
    }

    @Test
    void rejectsMalformedJson() {
        assertThrows(SpecificationParseException.class, () -> parser.parse(
                new ByteArrayInputStream("{\"name\":".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void rejectsUnknownProperties() {
        assertThrows(SpecificationParseException.class, () -> parser.parse(
                new ByteArrayInputStream("{\"name\":\"x\",\"unexpected\":true}"
                        .getBytes(StandardCharsets.UTF_8))));
    }
}
