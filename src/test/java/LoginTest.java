import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import methods.RequestSpec;
import models.Login;
import models.User;
import methods.UserRequests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.Matchers.equalTo;

public class LoginTest {

    private UserRequests userRequests;
    private User user;
    private String accessToken;

    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {"dododo@yandex.com", "77dopizzaDO!", 200, true, 200, null},
                {"dododo@yandex.com", faker.internet().password(), 401, false, 200, "email or password are incorrect"},
                {null, "77dopizzaDO!", 401, false, 200, "email or password are incorrect"},
                {"dododo@yandex.com", null, 401, false, 200, "email or password are incorrect"},
                {faker.internet().emailAddress(), "123456789", 401, false, 200, "email or password are incorrect"}
        };
    }

    @BeforeEach
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        userRequests = new UserRequests();
        user = new User("dododo@yandex.com", "77dopizzaDO!", "Doom");
        accessToken = null;
    }

    @ParameterizedTest(name = "email={0}, expectedStatus={2}")
    @MethodSource("getData")
    @DisplayName("Проверка логина пользователя")
    public void loginUser(
            String email,
            String password,
            int expectedStatus,
            boolean expectedSuccess,
            int expectedCreateStatus,
            String expectedMessage
    ) {
        Response responseCreate = userRequests.createUser(user);
        responseCreate.then().log().all().statusCode(expectedCreateStatus);

        if (expectedCreateStatus == 200) {
            accessToken = responseCreate.then().extract().path("accessToken");
        }

        Login login = new Login(email, password);
        Response responseLogin = userRequests.loginUser(login);

        responseLogin.then()
                .log().all()
                .statusCode(expectedStatus)
                .body("success", equalTo(expectedSuccess));

        if (expectedMessage != null) {
            responseLogin.then().body("message", equalTo(expectedMessage));
        }
    }

    @AfterEach
    public void deleteUser() {
        if (accessToken != null && !accessToken.isBlank()) {
            userRequests.deleteUser(accessToken).then()
                    .log().all()
                    .statusCode(202)
                    .body("success", equalTo(true));
        }
    }
}