package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Activity {

    private long id;
    private String name;
    private LocalDate dateFrom;
    private LocalDate dateUntil;
    private float probability;

    public Activity id(long id) {
        this.id = id;
        return this;
    }

    public Activity name(String name) {
        this.name = name;
        return this;
    }

    public Activity dateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    public Activity dateUntil(LocalDate dateUntil) {
        this.dateUntil = dateUntil;
        return this;
    }

    public Activity probability(float probability) {
        this.probability = probability;
        return this;
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", name='" + name + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateUntil=" + dateUntil +
                ", probability=" + probability;
    }
}
