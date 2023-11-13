package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Function extends Activity {

    private float defaultTime;

    public Function() {

    }

    public Function(long id, String name, LocalDate dateFrom,
                    LocalDate dateUntil, float probability, float defaultTime) {
        super(id, name, dateFrom, dateUntil, probability);
        this.defaultTime = defaultTime;
    }

    public Function id(long id) {
        super.id(id);
        return this;
    }

    public Function name(String name) {
        super.name(name);
        return this;
    }

    public Function dateFrom(LocalDate dateFrom) {
        super.dateFrom(dateFrom);
        return this;
    }

    public Function dateUntil(LocalDate dateUntil) {
        super.dateUntil(dateUntil);
        return this;
    }

    public Function probability(float probability) {
        super.probability(probability);
        return this;
    }

    public Function defaultTime(float defaultTime) {
        this.defaultTime = defaultTime;
        return this;
    }

    @Override
    public String toString() {
        return "Function{" + super.toString() +
                ", defaultTime=" + defaultTime +
                '}';
    }
}
