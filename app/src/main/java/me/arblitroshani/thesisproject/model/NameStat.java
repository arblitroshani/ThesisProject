package me.arblitroshani.thesisproject.model;

public class NameStat {

    private String name;
    private int year;
    private int occurrences;
    private String sex;

    public NameStat() {}

    public NameStat(String name, int year, int occurrences, String sex) {
        this.name = name;
        this.year = year;
        this.occurrences = occurrences;
        this.sex = sex;
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
