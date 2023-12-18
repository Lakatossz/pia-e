package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Activity {

    private long id;
    private String name;
    private String shortcut;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateFrom;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateUntil;
    private float probability;
    private String description;

    public Activity id(long id) {
        this.id = id;
        return this;
    }

    public Activity name(String name) {
        this.name = name;
        return this;
    }

    public Activity shortcut(String shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    public Activity dateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    public Activity dateUntil(Date dateUntil) {
        this.dateUntil = dateUntil;
        return this;
    }

    public Activity probability(float probability) {
        this.probability = probability;
        return this;
    }

    public Activity description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", name='" + name + '\'' +
                ", shortcut='" + shortcut + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateUntil=" + dateUntil +
                ", probability=" + probability +
                ", description=" + description;
    }
}
