import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import models.User;
import org.junit.BeforeClass;
import java.util.UUID;
import static io.restassured.RestAssured.given;

public class BaseTest {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru";
    }

    protected User createTestUser() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return new User(
                "testuser_" + uniqueId + "@example.com",
                "password123",
                "Test User"
        );
    }

    protected String registerUserAndGetToken(User user) {
        return given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/api/auth/register")
                .then()
                .extract()
                .path("accessToken");
    }

    protected void deleteUser(String token) {
        if (token != null) {
            given()
                    .header("Authorization", token)
                    .when()
                    .delete("/api/auth/user");
        }
    }
}