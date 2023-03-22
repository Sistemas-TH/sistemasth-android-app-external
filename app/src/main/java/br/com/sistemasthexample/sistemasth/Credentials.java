package br.com.sistemasthexample.sistemasth;

public class Credentials {


    public String Client;
    public String User;
    public String Password;


    public Credentials(String Client, String User, String Password) {
        this.Client = Client;
        this.User  = User;
        this.Password = Password;

    }

    public String getBase64() {
        final String credentials_join = String.format("%s:%s:%s", this.Client, this.User, this.Password);
        String credentials64 = new String(android.util.Base64.encode(credentials_join.getBytes(), android.util.Base64.DEFAULT));
        credentials64 = credentials64.replace("\r", "");
        credentials64 = credentials64.replace("\n", "");
        return credentials64;
    }

}
