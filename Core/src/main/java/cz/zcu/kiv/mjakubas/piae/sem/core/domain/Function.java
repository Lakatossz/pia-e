package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Function extends Activity {

    private float defaultTime;
    private Employee functionManager;
    private Workplace functionWorkplace;

    public Function() {

    }

    public Function(long id, String name, LocalDate dateFrom, Employee functionManager, Workplace functionWorkplace,
                    LocalDate dateUntil, float probability, float defaultTime) {
        super(id, name, dateFrom, dateUntil, probability);
        this.functionManager = functionManager;
        this.functionWorkplace = functionWorkplace;
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

    public Function functionWorkplace(Workplace functionWorkplace) {
        this.functionWorkplace = functionWorkplace;
        return this;
    }

    public Function functionManager(Employee functionManager) {
        this.functionManager = functionManager;
        return this;
    }

    public Function defaultTime(float defaultTime) {
        this.defaultTime = defaultTime;
        return this;
    }

    @Override
    public String toString() {
        return "Function{" + super.toString() +
                ", functionManager=" + functionManager +
                ", functionWorkplace=" + functionWorkplace +
                ", defaultTime=" + defaultTime +
                '}';
    }
}
