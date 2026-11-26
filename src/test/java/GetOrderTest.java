import methods.RequestSpec;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.User;
import methods.OrderRequests;
import methods.UserRequests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class GetOrderTest {

    private UserRequests userRequests;
    private OrderRequests orderRequests;
    private String accessToken;

    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName()},
                {faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName()},
                {faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName()}
        };
    }

    @BeforeEach
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderRequests = new OrderRequests();
        userRequests = new UserRequests();
        accessToken = null;
    }

    @ParameterizedTest(name = "email={0}, name={2}")
    @MethodSource("getData")
    @DisplayName("Проверка получения заказов авторизованного пользователя")
    public void getOrderTest(String email, String password, String name) {
        User user = new User(email, password, name);

        Response responseCreate = userRequests.createUser(user);
        responseCreate.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = responseCreate.then()
                .extract()
                .path("accessToken");

        Response responseGetOrder = orderRequests.getOrderUser(accessToken);
        responseGetOrder.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders.total", notNullValue());
    }

    @Test
    @DisplayName("Проверка получения заказов неавторизованного пользователя")
    public void getOrderWithoutAuthorization() {
        Response responseGetOrderWithoutAuthorization =
                orderRequests.getOrderUserWithoutAuthorization();

        responseGetOrderWithoutAuthorization.then()
                .log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @AfterEach
    public void deleteUser() {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }

        Response responseDelete = userRequests.deleteUser(accessToken);
        responseDelete.then()
                .log().all()
                .statusCode(202)
                .body("success", equalTo(true));
    }
}