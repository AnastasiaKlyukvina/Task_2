import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import models.Order;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class GetOrderTest extends BaseTest {

    private String accessToken;

    @Before
    public void setUp() {
        User testUser = createTestUser();
        accessToken = given()
                .contentType(ContentType.JSON)
                .body(testUser)
                .when()
                .post("/api/auth/register")
                .then()
                .extract()
                .path("accessToken");

        List<String> ingredients = given()
                .when()
                .get("/api/ingredients")
                .then()
                .extract()
                .path("data._id");

        if (!ingredients.isEmpty()) {
            Order order = new Order(Arrays.asList(ingredients.get(0)));

            given()
                    .header("Authorization", accessToken)
                    .contentType(ContentType.JSON)
                    .body(order)  // Используем POJO объект Order
                    .when()
                    .post("/api/orders");
        }
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
    @Description("Получение заказов авторизованного пользователя")
    public void testGetUserOrdersWithAuth() {
        given()
                .header("Authorization", accessToken)
                .when()
                .get("/api/orders")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @Description("Получение заказов неавторизованного пользователя")
    public void testGetUserOrdersWithoutAuth() {
        given()
                .when()
                .get("/api/orders")
                .then()
                .statusCode(401)
                .body("success", equalTo(false));
    }
}
