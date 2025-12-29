import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class OrderCreationTest extends BaseTest {

    private String accessToken;
    private List<String> ingredients;

    @Before
    public void setUp() {
        long timestamp = System.currentTimeMillis();
        String email = "ordertest_" + timestamp + "@example.com";
        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"password123\", \"name\": \"Order Test\"}",
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
        if (!ingredients.isEmpty()) {
            String ingredientsBody = String.format(
                    "{\"ingredients\": [\"%s\", \"%s\"]}",
                    ingredients.get(0), ingredients.get(1)
            );

            given()
                    .header("Authorization", accessToken)
                    .contentType(ContentType.JSON)
                    .body(ingredientsBody)
                    .when()
                    .post("/api/orders")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("order.status", equalTo("done"));
        }
    }

    @Test
    @Description("Создание заказа без авторизации")
    public void testCreateOrderWithoutAuth() {
        if (!ingredients.isEmpty()) {
            String ingredientsBody = String.format(
                    "{\"ingredients\": [\"%s\"]}",
                    ingredients.get(0)
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(ingredientsBody)
                    .when()
                    .post("/api/orders")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true));
        }
    }

    @Test
    @Description("Создание заказа с ингредиентами")
    public void testCreateOrderWithIngredients() {
        if (!ingredients.isEmpty()) {
            String ingredientsBody = String.format(
                    "{\"ingredients\": [\"%s\"]}",
                    ingredients.get(0)
            );

            given()
                    .header("Authorization", accessToken)
                    .contentType(ContentType.JSON)
                    .body(ingredientsBody)
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
        given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body("{\"ingredients\": []}")
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @Description("Создание заказа с неверным хешем ингредиентов")
    public void testCreateOrderWithInvalidIngredientHash() {
        given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body("{\"ingredients\": [\"invalid_hash_1\", \"invalid_hash_2\"]}")
                .when()
                .post("/api/orders")
                .then()
                .statusCode(500);
    }
}
