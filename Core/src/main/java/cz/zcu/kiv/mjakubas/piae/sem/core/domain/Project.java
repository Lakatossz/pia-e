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
public class Project extends Activity {

    private Employee projectManager;
    private Workplace projectWorkplace;
    private String description;
    private Integer budget;
    private Integer participation;
    private Integer totalTime;
    private List<Employee> employees = new ArrayList<>();

    @Override
    public Project id(long id) {
        super.id(id);
        return this;
    }

    @Override
    public Project name(String name) {
        super.name(name);
        return this;
    }

    @Override
    public Project dateFrom(LocalDate dateFrom) {
        super.dateFrom(dateFrom);
        return this;
    }

    @Override
    public Project dateUntil(LocalDate dateUntil) {
        super.dateUntil(dateUntil);
        return this;
    }

    @Override
    public Project probability(float probability) {
        super.probability(probability);
        return this;
    }

    public Project projectManager(Employee projectManager) {
        this.projectManager = projectManager;
        return this;
    }

    public Project projectWorkplace(Workplace projectWorkplace) {
        this.projectWorkplace = projectWorkplace;
        return this;
    }

    public Project description(String description) {
        this.description = description;
        return this;
    }

    public Project budget(Integer budget) {
        this.budget = budget;
        return this;
    }

    public Project participation(Integer participation) {
        this.participation = participation;
        return this;
    }

    public Project totalTime(Integer totalTime) {
        this.totalTime = totalTime;
        return this;
    }

    public Project employees(List<Employee> employees) {
        this.employees = employees;
        return this;
    }

    @Override
    public String toString() {
        return "Project{" + super.toString() +
                ", projectManager=" + projectManager +
                ", projectWorkplace=" + projectWorkplace +
                ", description='" + description +
                ", budget=" + budget +
                ", participation=" + participation +
                ", totalTime=" + totalTime + '\'' +
                ", employees=" + employees +
                '}';
    }
}
