package methods;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Login;
import models.User;

import static constants.ApiConstants.*;
import static io.restassured.RestAssured.given;

public class UserRequests {

    @Step("Создание пользователя")
    public Response createUser(User user){ // Метод для создания пользователя, принимает объект User и возвращает ответ от сервера
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post(USER_REGISTER);
    }

    @Step("Логин пользователя")
    public Response loginUser(Login login){ // Метод для входа пользователя, принимает объект Login и возвращает ответ от сервера
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(login)
                .when()
                .post(USER_LOGIN);
    }

    @Step("Получить информацию о пользователе")
    public Response getUser(String token){ // Метод для получения информации о пользователе, принимает токен и возвращает ответ от сервера
        return given()
                .header("Authorization", token)
                .get(USER);
    }

    @Step("Обновление данных авторизованного пользователя")
    public Response updateUser(User user, String token){ // Метод для обновления данных авторизованного пользователя, принимает объект User и токен, возвращает ответ от сервера
        return given()
                .header("Authorization", token)
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .patch(USER);
    }

    @Step("Обновление данных пользователя без авторизации")
    public Response updateUserWithoutAuthorization(User user){ // Метод для обновления данных пользователя без авторизации, принимает объект User и возвращает ответ от сервера
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .patch(USER);
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String token){ // Метод для удаления пользователя, принимает токен и возвращает ответ от сервера
        return given()
                .header("Authorization", token)
                .delete(USER);
    }
}