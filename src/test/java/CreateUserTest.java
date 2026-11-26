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
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CreateUserTest {

    private UserRequests userRequests;
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
        userRequests = new UserRequests();
    }

    @ParameterizedTest(name = "email={0}, name={2}")
    @MethodSource("getData")
    @DisplayName("Проверка создания пользователя")
    public void createUser(String email, String password, String name) {
        User user = new User(email, password, name);

        Response responseCreate = userRequests.createUser(user);

        responseCreate.then()
                .log().all()
                .statusCode(200)
                .assertThat()
                .body("success", equalTo(true));

        accessToken = responseCreate.then()
                .extract()
                .path("accessToken");

        assertNotNull(accessToken, "accessToken should not be null");
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