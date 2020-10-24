package comp.hacktx.backend.models;

import org.springframework.data.annotation.Id;

public class Report {

    @Id
    private String id;
    private String username;
    private long time;
    private int zipcode;

    public Report(String id, String username, long time, int zipcode) {
        this.id = id;
        this.username = username;
        this.time = time;
        this.zipcode = zipcode;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public long getTime() {
        return time;
    }

    public int getZipcode() {
        return zipcode;
    }
}
