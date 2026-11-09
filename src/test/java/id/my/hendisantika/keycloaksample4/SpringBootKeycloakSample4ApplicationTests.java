package id.my.hendisantika.keycloaksample4;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootKeycloakSample4ApplicationTests {

    private static final String REALM = "PowerRanger";
    private static final String CLIENT_ID = "green_ranger";
    private static final String PASSWORD = "password";

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.0")
            .withRealmImportFile("keycloak/PowerRanger.json");

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/" + REALM);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    private String getBearerToken(String username) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", CLIENT_ID);
        formData.add("username", username);
        formData.add("password", PASSWORD);

        String result = WebClient.builder().build()
                .post()
                .uri(keycloak.getAuthServerUrl() + "/realms/" + REALM + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return "Bearer " + new JacksonJsonParser().parseMap(result).get("access_token");
    }

    @Test
    void givenAnonymous_whenGetAll_shouldReturnOk() {
        given().when().get("/api/v1/demo/all")
                .then().statusCode(200)
                .body(equalTo("Hello All from Spring boot & Keycloak"));
    }

    @Test
    void givenAnonymous_whenGetUser_shouldReturnUnauthorized() {
        given().when().get("/api/v1/demo/user")
                .then().statusCode(401);
    }

    @Test
    void givenUser_whenGetUser_shouldReturnOk() {
        given().header("Authorization", getBearerToken("test-user"))
                .when().get("/api/v1/demo/user")
                .then().statusCode(200)
                .body(equalTo("Hello from Spring boot & Keycloak"));
    }

    @Test
    void givenUser_whenGetAdmin_shouldReturnForbidden() {
        given().header("Authorization", getBearerToken("test-user"))
                .when().get("/api/v1/demo/admin")
                .then().statusCode(403);
    }

    @Test
    void givenAdmin_whenGetAdmin_shouldReturnOk() {
        given().header("Authorization", getBearerToken("test-admin"))
                .when().get("/api/v1/demo/admin")
                .then().statusCode(200)
                .body(equalTo("Hello from Spring boot & Keycloak - ADMIN"));
    }
}
