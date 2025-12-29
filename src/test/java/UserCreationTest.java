import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UserCreationTest extends BaseTest {

    private String accessToken;

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
    @Description("Создание уникального пользователя")
    public void testCreateUniqueUser() {
        long timestamp = System.currentTimeMillis();
        String email = "testuser_" + timestamp + "@example.com";

        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"password123\", \"name\": \"Test User\"}",
                email
        );

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(email.toLowerCase()))
                .body("user.name", equalTo("Test User"));
    }

    @Test
    @Description("Создание уже существующего пользователя")
    public void testCreateExistingUser() {
        String email = "existing_user@example.com";
        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"password123\", \"name\": \"Existing User\"}",
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

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @Description("Создание пользователя без email")
    public void testCreateUserWithoutEmail() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"password\": \"password123\", \"name\": \"Test User\"}")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @Description("Создание пользователя без password")
    public void testCreateUserWithoutPassword() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"test@example.com\", \"name\": \"Test User\"}")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @Description("Создание пользователя без name")
    public void testCreateUserWithoutName() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"test@example.com\", \"password\": \"password123\"}")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}