import methods.RequestSpec;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Order;

import methods.OrderRequests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateOrderTest {

    private OrderRequests orderRequests;

    public static Object[][] getData() {
        return new Object[][]{
                {0, 1, 200},
                {0, 6, 200},
                {10, 15, 200},
                {0, 0, 400},
        };
    }

    @BeforeEach
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderRequests = new OrderRequests();
    }

    @ParameterizedTest(name = "from={0}, to={1}, expectedStatus={2}")
    @MethodSource("getData")
    @DisplayName("Проверка создания заказа")
    public void createOrder(int fromIndex, int toIndex, int expectedStatus) {
        Response responseGetIngredient = orderRequests.getIngredient();

        List<String> ingredients = new ArrayList<>(
                responseGetIngredient.then()
                        .log().all()
                        .statusCode(200)
                        .extract()
                        .path("data._id")
        );

        Order order = new Order(ingredients.subList(fromIndex, toIndex));
        Response responseCreate = orderRequests.createOrder(order);

        responseCreate.then()
                .log().all()
                .statusCode(expectedStatus);

        if (expectedStatus == 200) {
            responseCreate.then()
                    .assertThat()
                    .body("order.number", notNullValue())
                    .body("success", equalTo(true));
        } else if (expectedStatus == 400) {
            responseCreate.then()
                    .assertThat()
                    .body("success", equalTo(false))
                    .body("message", equalTo("Ingredient ids must be provided"));
        }
    }
}