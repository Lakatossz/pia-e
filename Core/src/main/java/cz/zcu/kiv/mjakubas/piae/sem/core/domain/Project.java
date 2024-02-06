package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Project extends Activity {

    private Employee projectManager;
    private Workplace projectWorkplace;
    private Integer budget;
    private Integer budgetParticipation;
    private float totalTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date introduced;
    private String agency;
    private String grantTitle;
    private ProjectState state;
    private List<Employee> employees = new ArrayList<>();
    private List<Float> yearAllocation = new ArrayList<>(Collections.nCopies(12, (float) 0));
    private List<Allocation> projectAllocations = new ArrayList<>();

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
    public Project shortcut(String shortcut) {
        super.shortcut(shortcut);
        return this;
    }

    @Override
    public Project dateFrom(Date dateFrom) {
        super.dateFrom(dateFrom);
        return this;
    }

    @Override
    public Project dateUntil(Date dateUntil) {
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

    @Override
    public Project description(String description) {
        super.description(description);
        return this;
    }

    public Project budget(Integer budget) {
        this.budget = budget;
        return this;
    }

    public Project budgetParticipation(Integer budgetParticipation) {
        this.budgetParticipation = budgetParticipation;
        return this;
    }

    public Project participation(Integer participation) {
        return this;
    }

    public Project totalTime(float totalTime) {
        this.totalTime = totalTime;
        return this;
    }

    public Project introduced(Date introduced) {
        this.introduced = introduced;
        return this;
    }

    public Project agency(String agency) {
        this.agency = agency;
        return this;
    }

    public Project grantTitle(String grantTitle) {
        this.grantTitle = grantTitle;
        return this;
    }

    public Project state(ProjectState state) {
        this.state = state;
        return this;
    }

    public Project employees(List<Employee> employees) {
        this.employees = employees;
        return this;
    }

    public Project yearAllocation(List<Float> yearAllocation) {
        this.yearAllocation.addAll(yearAllocation);
        return this;
    }

    public Project projectAllocations(List<Allocation> projectAllocations) {
        this.projectAllocations.addAll(projectAllocations);
        return this;
    }


    @Override
    public String toString() {
        return "Project{" + super.toString() +
                ", projectManager=" + projectManager +
                ", projectWorkplace=" + projectWorkplace +
                ", budget=" + budget +
                ", budgetParticipation=" + budgetParticipation +
                ", totalTime=" + totalTime + '\'' +
                ", introduced=" + introduced +
                ", agency=" + agency +
                ", grantTitle=" + grantTitle +
                ", state=" + state +
                ", employees=" + employees +
                ", yearAllocation=" + yearAllocation +
                ", projectAllocations=" + projectAllocations +
                '}';
    }
}
