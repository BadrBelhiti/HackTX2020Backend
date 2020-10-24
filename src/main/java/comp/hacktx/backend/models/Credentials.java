package comp.hacktx.backend.models;

public class Credentials {

    private String username;
    private String password;

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean assertNonNull() {
        return this.username != null && this.password != null;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
