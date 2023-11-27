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
    private Long workplaceId;
    private String description;
    private Integer budget;
    private Integer participation;
    private Integer totalTime;

    public ProjectVO() {

    }

    public ProjectVO(String name, Long projectManagerId, Long workplaceId, float probability,
                     LocalDate dateFrom, LocalDate dateUntil, String description,
                     Integer budget, Integer participation, Integer totalTime) {
        this.name = name;
        this.dateFrom = dateFrom;
        this.dateUntil = dateUntil;
        this.probability = probability;
        this.projectManagerId = projectManagerId;
        this.workplaceId = workplaceId;
        this.description = description;
        this.budget = budget;
        this.participation = participation;
        this.totalTime = totalTime;
    }

    public ProjectVO name(String name) {
        this.name = name;
        return this;
    }

    public ProjectVO projectManagerId(Long projectManagerId) {
        this.projectManagerId = projectManagerId;
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

    public ProjectVO participation(Integer participation) {
        this.participation = participation;
        return this;
    }

    public ProjectVO totalTime(Integer totalTime) {
        this.totalTime = totalTime;
        return this;
    }

    @Override
    public String toString() {
        return "ProjectVO{" +
                "name='" + name + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateUntil=" + dateUntil +
                ", projectManagerId='" + projectManagerId + '\'' +
                ", workplaceId=" + workplaceId +
                ", description='" + description + '\'' +
                '}';
    }
}
