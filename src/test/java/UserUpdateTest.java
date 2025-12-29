import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UserUpdateTest extends BaseTest {

    private String accessToken;
    private String originalEmail;

    @Before
    public void setUp() {
        long timestamp = System.currentTimeMillis();
        originalEmail = "updatetest_" + timestamp + "@example.com";
        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"password123\", \"name\": \"Update Test\"}",
                originalEmail
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
    @Description("Изменение email с авторизацией")
    public void testUpdateEmailWithAuth() {
        String newEmail = "updated_" + originalEmail;

        given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body("{\"email\": \"" + newEmail + "\"}")
                .when()
                .patch("/api/auth/user")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail.toLowerCase()));
    }

    @Test
    @Description("Изменение name с авторизацией")
    public void testUpdateNameWithAuth() {
        given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body("{\"name\": \"Updated Name\"}")
                .when()
                .patch("/api/auth/user")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @Description("Изменение пароля с авторизацией")
    public void testUpdatePasswordWithAuth() {
        given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body("{\"password\": \"newpassword123\"}")
                .when()
                .patch("/api/auth/user")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @Description("Изменение данных без авторизации")
    public void testUpdateUserWithoutAuth() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"New Name\"}")
                .when()
                .patch("/api/auth/user")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
