package com.agentqa.test;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class HealthCheckTest {

    @Test
    public void testPublicApiHealthCheck() {
        given()
            .when()
                .get("https://reqres.in/api/users?page=2")
            .then()
                .statusCode(200)
                .body("page", equalTo(2));
    }
}
