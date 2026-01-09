import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UserLoginTest extends BaseTest {

    private String accessToken;
    private User testUser;

    @Before
    public void setUp() {
        testUser = createTestUser();

        accessToken = given()
                .contentType(ContentType.JSON)
                .body(testUser)
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
        given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"" + testUser.getEmail() + "\", \"password\": \"" + testUser.getPassword() + "\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(testUser.getEmail().toLowerCase()))
                .body("user.name", equalTo(testUser.getName()));
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
                .body("success", equalTo(false));
    }
}