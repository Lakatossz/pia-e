package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Function extends Activity {

    private float defaultTime;
    private Employee functionManager;
    private Workplace functionWorkplace;
    private List<Employee> employees = new ArrayList<>();

    @Override
    public Function id(long id) {
        super.id(id);
        return this;
    }

    @Override
    public Function name(String name) {
        super.name(name);
        return this;
    }

    @Override
    public Function dateFrom(LocalDate dateFrom) {
        super.dateFrom(dateFrom);
        return this;
    }

    @Override
    public Function dateUntil(LocalDate dateUntil) {
        super.dateUntil(dateUntil);
        return this;
    }

    @Override
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

    public Function employees(List<Employee> employees) {
        this.employees = employees;
        return this;
    }

    @Override
    public String toString() {
        return "Function{" + super.toString() +
                ", functionManager=" + functionManager +
                ", functionWorkplace=" + functionWorkplace +
                ", defaultTime=" + defaultTime +
                ", employees=" + employees +
                '}';
    }
}
