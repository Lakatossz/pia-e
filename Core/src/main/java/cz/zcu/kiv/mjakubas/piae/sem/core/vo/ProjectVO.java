package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Value object for representing {@link Project}.
 *
 * @see Project
 */

@Setter
@Getter
public class ProjectVO {

    private String name;
    private LocalDate dateFrom;
    private LocalDate dateUntil;
    private float probability;
    private Long projectManagerId;
    private String projectManagerName;
    private Long workplaceId;
    private String description;
    private Integer budget;
    private Integer budgetParticipation;
    private Integer participation;
    private Integer totalTime;
    private LocalDate introduced;
    private String agency;
    private String grantTitle;

    public ProjectVO() {

    }

    public ProjectVO name(String name) {
        this.name = name;
        return this;
    }

    public ProjectVO projectManagerId(Long projectManagerId) {
        this.projectManagerId = projectManagerId;
        return this;
    }

    public ProjectVO projectManagerName(String projectManagerName) {
        this.projectManagerName = projectManagerName;
        return this;
    }

    public ProjectVO workplaceId(Long workplaceId) {
        this.workplaceId = workplaceId;
        return this;
    }

    public ProjectVO dateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    public ProjectVO dateUntil(LocalDate dateUntil) {
        this.dateUntil = dateUntil;
        return this;
    }

    public ProjectVO probability(long probability) {
        this.probability = probability;
        return this;
    }

    public ProjectVO description(String description) {
        this.description = description;
        return this;
    }

    public ProjectVO budget(Integer budget) {
        this.budget = budget;
        return this;
    }

    public ProjectVO budgetParticipation(Integer budgetParticipation) {
        this.budgetParticipation = budgetParticipation;
        return this;
    }

    public ProjectVO participation(Integer participation) {
        this.participation = participation;
        return this;
    }

    public ProjectVO totalTime(Integer totalTime) {
        this.totalTime = totalTime;
        return this;
    }

    public ProjectVO introduced(LocalDate introduced) {
        this.introduced = introduced;
        return this;
    }

    public ProjectVO agency(String agency) {
        this.agency = agency;
        return this;
    }

    public ProjectVO grantTitle(String grantTitle) {
        this.grantTitle = grantTitle;
        return this;
    }

    @Override
    public String toString() {
        return "ProjectVO{" +
                "name='" + name + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateUntil=" + dateUntil +
                ", projectManagerId='" + projectManagerId + '\'' +
                ", projectManagerName='" + projectManagerName + '\'' +
                ", workplaceId=" + workplaceId +
                ", budget=" + budget +
                ", budgetParticipation=" + budgetParticipation +
                ", agency=" + agency +
                ", grantTitle=" + grantTitle +
                ", introduced=" + introduced +
                ", description='" + description + '\'' +
                '}';
    }
}
