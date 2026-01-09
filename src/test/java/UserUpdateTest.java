import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UserUpdateTest extends BaseTest {

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
    @Description("Изменение email с авторизацией")
    public void testUpdateEmailWithAuth() {
        User updateData = new User();
        updateData.setEmail("updated_" + testUser.getEmail());

        given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .patch("/api/auth/user")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(updateData.getEmail().toLowerCase()));
    }

    @Test
    @Description("Изменение name с авторизацией")
    public void testUpdateNameWithAuth() {
        User updateData = new User();
        updateData.setName("Updated Name");

        given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .patch("/api/auth/user")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.name", equalTo("Updated Name"));
    }

    @Test
    @Description("Изменение пароля с авторизацией")
    public void testUpdatePasswordWithAuth() {
        User updateData = new User();
        updateData.setPassword("newpassword123");

        given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .patch("/api/auth/user")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @Description("Изменение данных без авторизации")
    public void testUpdateUserWithoutAuth() {
        User updateData = new User();
        updateData.setName("New Name");

        given()
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .patch("/api/auth/user")
                .then()
                .statusCode(401)
                .body("success", equalTo(false));
    }
}