package me.arblitroshani.thesisproject.model;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class NameStatRealm extends RealmObject {

    @PrimaryKey
    @Required
    private String id;

    @Required
    private String name;

    private int year;

    private int occurrences;

    @Required
    private String sex;

    public NameStatRealm() {
        this.id = UUID.randomUUID().toString();
        this.name = "";
        this.year = 2017;
        this.occurrences = 0;
        this.sex = "F";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
