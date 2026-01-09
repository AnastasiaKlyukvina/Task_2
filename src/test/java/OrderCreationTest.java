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

public class OrderCreationTest extends BaseTest {

    private String accessToken;
    private List<String> ingredients;

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

        ingredients = given()
                .when()
                .get("/api/ingredients")
                .then()
                .extract()
                .path("data._id");
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
    @Description("Создание заказа с авторизацией")
    public void testCreateOrderWithAuth() {
        if (!ingredients.isEmpty() && ingredients.size() >= 2) {
            Order order = new Order(Arrays.asList(
                    ingredients.get(0),
                    ingredients.get(1)
            ));

            given()
                    .header("Authorization", accessToken)
                    .contentType(ContentType.JSON)
                    .body(order)
                    .when()
                    .post("/api/orders")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true));
        }
    }

    @Test
    @Description("Создание заказа без авторизации")
    public void testCreateOrderWithoutAuth() {
        if (!ingredients.isEmpty()) {
            Order order = new Order(Arrays.asList(ingredients.get(0)));

            given()
                    .contentType(ContentType.JSON)
                    .body(order)
                    .when()
                    .post("/api/orders")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true));
        }
    }

    @Test
    @Description("Создание заказа без ингредиентов")
    public void testCreateOrderWithoutIngredients() {
        Order order = new Order(Arrays.asList());

        given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body(order)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400)
                .body("success", equalTo(false));
    }

    @Test
    @Description("Создание заказа с неверным хешем ингредиентов")
    public void testCreateOrderWithInvalidIngredientHash() {
        Order order = new Order(Arrays.asList("invalid_hash_1", "invalid_hash_2"));

        given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body(order)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(500);
    }
}
