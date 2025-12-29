import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UserLoginTest extends BaseTest {

    private String accessToken;

    @Before
    public void setUp() {
        long timestamp = System.currentTimeMillis();
        String email = "logintest_" + timestamp + "@example.com";
        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"password123\", \"name\": \"Login Test\"}",
                email
        );

        accessToken = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .extract()
                .path("accessToken");
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            given()
                    .header("Authorization", accessToken)
                    .when()
                    .delete("/api/auth/user");
        }
    }

    @Test
    @Description("Логин под существующим пользователем")
    public void testLoginWithValidCredentials() {
        long timestamp = System.currentTimeMillis();
        String email = "logintest_" + timestamp + "@example.com";
        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"password123\", \"name\": \"Login Test\"}",
                email
        );

        // Создание пользователя
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/register");

        // Авторизация
        given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"" + email + "\", \"password\": \"password123\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(email.toLowerCase()));
    }

    @Test
    @Description("Логин с неверным логином и паролем")
    public void testLoginWithInvalidCredentials() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"wrong@example.com\", \"password\": \"wrongpassword\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}