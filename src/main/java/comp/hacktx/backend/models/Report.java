package comp.hacktx.backend.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    private String id;
    private long time;
    private int zipcode;
    private int symptoms;

    public Report() {

    }

    public Report(String id, long time, int zipcode, int symptoms) {
        this.id = id;
        this.time = time;
        this.zipcode = zipcode;
        this.symptoms = symptoms;
    }

    public String getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public int getZipcode() {
        return zipcode;
    }

    public int getSymptoms() {
        return symptoms;
    }
}
