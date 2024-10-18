package main.java.com.kingdomhallscheduler;

import java.io.Serializable;

public class Person implements Serializable {
    private static final long serialVersionUID =1L;
    private String name;
    private String[] skills;
    private String notes;

    public Person(String name, String[] skills, String notes) {
        this.name = name;
        this.skills = skills;
        this.notes = notes;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String[] getSkills() {return skills;}
    public void setSkills(String[] skills) {this.skills = skills;}

    public String getNotes() {return notes;}
    public void setNotes(String notes) {this.notes = notes;}

}
