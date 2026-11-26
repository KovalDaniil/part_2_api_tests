import methods.RequestSpec;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Order;
import methods.OrderRequests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class InvalidOrderHashCreationTest {

    private OrderRequests orderRequests;

    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {List.of(faker.number().digits(7))},   // Невалидный хэш (7 символов)
                {List.of(faker.number().digits(23))},  // Невалидный хэш (23 символа)
                {List.of(faker.number().digits(25))}   // Невалидный хэш (25 символов)
        };
    }

    @BeforeEach
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderRequests = new OrderRequests();
    }

    @ParameterizedTest(name = "ingredient={0}")
    @MethodSource("getData")
    @DisplayName("Проверка отправки невалидного хэша при создании заказа")
    public void createOrderInvalidHash(List<String> ingredients) {
        Order order = new Order(ingredients);

        Response responseCreate = orderRequests.createOrder(order);
        responseCreate.then()
                .log().all()
                .statusCode(500);
    }
}