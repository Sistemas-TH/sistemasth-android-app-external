package br.com.sistemasthexample.sistemasth.data;

import br.com.sistemasthexample.sistemasth.data.model.LoggedInUser;

import java.io.IOException;
import java.util.Locale;


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            // TODO: handle loggedInUser authentication
            username = username.toUpperCase(Locale.ROOT);

            if (username.equals("TH") && password.equals("123456")) {
                LoggedInUser fakeUser =
                        new LoggedInUser(
                                java.util.UUID.randomUUID().toString(),
                                "TH Sample");
                return new Result.Success<>(fakeUser);
            }

            return new Result.Error(new IOException("Credenciais Inválidas!"));
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}