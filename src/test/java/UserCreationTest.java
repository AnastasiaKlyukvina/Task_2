import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import models.User;
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
        User newUser = createTestUser();

        given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newUser.getEmail().toLowerCase()))
                .body("user.name", equalTo(newUser.getName()));

        accessToken = given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when()
                .post("/api/auth/register")
                .then()
                .extract()
                .path("accessToken");
    }

    @Test
    @Description("Создание уже существующего пользователя")
    public void testCreateExistingUser() {
        User existingUser = new User("existing_user@example.com", "password123", "Existing User");

        accessToken = given()
                .contentType(ContentType.JSON)
                .body(existingUser)
                .when()
                .post("/api/auth/register")
                .then()
                .extract()
                .path("accessToken");

        given()
                .contentType(ContentType.JSON)
                .body(existingUser)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false));
    }

    @Test
    @Description("Создание пользователя без email")
    public void testCreateUserWithoutEmail() {
        User userWithoutEmail = new User();
        userWithoutEmail.setPassword("password123");
        userWithoutEmail.setName("Test User");

        given()
                .contentType(ContentType.JSON)
                .body(userWithoutEmail)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false));
    }

    @Test
    @Description("Создание пользователя без password")
    public void testCreateUserWithoutPassword() {
        User userWithoutPassword = new User();
        userWithoutPassword.setEmail("test@example.com");
        userWithoutPassword.setName("Test User");

        given()
                .contentType(ContentType.JSON)
                .body(userWithoutPassword)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false));
    }

    @Test
    @Description("Создание пользователя без name")
    public void testCreateUserWithoutName() {
        User userWithoutName = new User();
        userWithoutName.setEmail("test@example.com");
        userWithoutName.setPassword("password123");
        given()
                .contentType(ContentType.JSON)
                .body(userWithoutName)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false));
    }
}