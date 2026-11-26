import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import methods.RequestSpec;
import methods.UserRequests;
import models.Login;
import models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.Matchers.equalTo;

public class UpdateUserDataTest {

    private UserRequests userRequests;
    private String accessToken;
    private String currentEmail;
    private String currentPassword;
    private String currentName;

    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                // Обновляются все данные
                {"all",  faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName()},
                // Обновляется email
                {"email", faker.internet().emailAddress(), "012349", "Ivan"},
                // Обновляется пароль
                {"password", null, faker.internet().password(), "Ivan"},
                // Обновляется имя
                {"name", null, "012349", faker.name().firstName()}
        };
    }

    @BeforeEach
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        userRequests = new UserRequests();
        accessToken = null;

        Faker faker = new Faker();
        currentEmail = faker.internet().emailAddress();
        currentPassword = "012349";
        currentName = "Ivan";

        User user = new User(currentEmail, currentPassword, currentName);
        Response responseCreate = userRequests.createUser(user);
        responseCreate.then().log().all().statusCode(200).body("success", equalTo(true));

        accessToken = responseCreate.then().extract().path("accessToken");
    }

    @ParameterizedTest(name = "case={0}")
    @MethodSource("getData")
    @DisplayName("Проверка изменения данных авторизованного пользователя")
    public void modifyUserDataTest(
            String caseName,
            String newEmail,
            String newPassword,
            String newName
    ) {
        String expectedEmail = newEmail != null ? newEmail : currentEmail;
        String expectedName = newName != null ? newName : currentName;
        String expectedPassword = newPassword != null ? newPassword : currentPassword;

        // Проверяем исходные данные
        Response responseGetUser = userRequests.getUser(accessToken);
        responseGetUser.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(currentEmail))
                .body("user.name", equalTo(currentName));

        // Обновляем
        Response responseUpdate = userRequests.updateUser(
                new User(expectedEmail, expectedPassword, expectedName),
                accessToken
        );
        responseUpdate.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(expectedEmail))
                .body("user.name", equalTo(expectedName));

        // Проверяем после обновления
        Response responseGetUserAfterUpdate = userRequests.getUser(accessToken);
        responseGetUserAfterUpdate.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(expectedEmail))
                .body("user.name", equalTo(expectedName));

        // Логин с актуальными данными
        Login login = new Login(expectedEmail, expectedPassword);
        Response responseLogin = userRequests.loginUser(login);
        responseLogin.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @ParameterizedTest(name = "unauthorized, case={0}")
    @MethodSource("getData")
    @DisplayName("Проверка изменения данных неавторизованного пользователя")
    public void modifyUserDataWithoutAuthorizationTest(
            String caseName,
            String newEmail,
            String newPassword,
            String newName
    ) {
        String expectedEmail = newEmail != null ? newEmail : currentEmail;
        String expectedPassword = newPassword != null ? newPassword : currentPassword;
        String expectedName = newName != null ? newName : currentName;

        Response responseUpdateWithoutAuthorization = userRequests.updateUserWithoutAuthorization(
                new User(expectedEmail, expectedPassword, expectedName)
        );
        responseUpdateWithoutAuthorization.then()
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
        userRequests.deleteUser(accessToken).then()
                .log().all()
                .statusCode(202)
                .body("success", equalTo(true));
    }
}