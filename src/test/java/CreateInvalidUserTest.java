import methods.RequestSpec;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.User;
import methods.UserRequests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.Matchers.equalTo;

public class CreateInvalidUserTest {

    private UserRequests userRequests;
    private String accessToken;

    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {null, faker.internet().password(), faker.name().firstName(),
                        403, false, "Email, password and name are required fields"},
                {faker.internet().emailAddress(), null, faker.name().firstName(),
                        403, false, "Email, password and name are required fields"},
                {faker.internet().emailAddress(), faker.internet().password(), null,
                        403, false, "Email, password and name are required fields"},
                {faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName(),
                        200, true, null}
        };
    }

    @BeforeEach
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        userRequests = new UserRequests();
        accessToken = null;
    }

    @ParameterizedTest(name = "status={3}, success={4}")
    @MethodSource("getData")
    @DisplayName("Проверка создания пользователя с НЕвалидными данными и дублированием")
    public void createInvalidUser(
            String email,
            String password,
            String name,
            int expectedStatus,
            boolean expectedSuccess,
            String expectedMessage
    ) {
        User user = new User(email, password, name);

        Response responseCreate = userRequests.createUser(user);

        responseCreate.then()
                .log().all()
                .statusCode(expectedStatus)
                .body("success", equalTo(expectedSuccess));

        if (expectedStatus == 200) {
            accessToken = responseCreate.then()
                    .extract()
                    .path("accessToken");

            Response responseCreateDouble = userRequests.createUser(user);
            responseCreateDouble.then()
                    .log().all()
                    .statusCode(403)
                    .body("success", equalTo(false))
                    .body("message", equalTo("User already exists"));
        } else {
            responseCreate.then()
                    .assertThat()
                    .body("message", equalTo(expectedMessage));
        }
    }

    @AfterEach
    public void deleteUser() {
        if (accessToken != null && !accessToken.isBlank()) {
            userRequests.deleteUser(accessToken);
        }
    }
}