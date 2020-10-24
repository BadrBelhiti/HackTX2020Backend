package comp.hacktx.backend.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    private String id;
    private String username;
    private String password;
    private long lastReport;
    private int points;
    private int streak;

    public User() {

    }

    public User(String id, String username, String password, long lastReport, int points, int streak) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.lastReport = lastReport;
        this.points = points;
        this.streak = streak;
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

    public int getStreak() {
        return streak;
    }
}
