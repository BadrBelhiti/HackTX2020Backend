package comp.hacktx.backend.models;

import org.springframework.data.annotation.Id;

public class User {

    @Id
    private String id;
    private String username;
    private String password;
    private long lastReport;
    private int points;

    public User(String id, String username, String password, long lastReport, int points) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.lastReport = lastReport;
        this.points = points;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public long getLastReport() {
        return lastReport;
    }

    public int getPoints() {
        return points;
    }
}
