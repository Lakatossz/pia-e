package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Value object for representing {@link Project}.
 *
 * @see Project
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class ProjectVO {

    private String name;
    private String shortcut;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateFrom;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateUntil;
    private float probability;
    private Long projectManagerId;
    private String projectManagerName;
    private Long workplaceId;
    private String description;
    private Integer budget;
    private Integer budgetParticipation;
    private Integer totalTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date introduced;
    private String agency;
    private String grantTitle;
    private String state;

    public ProjectVO name(String name) {
        this.name = name;
        return this;
    }

    public ProjectVO shortcut(String shortcut) {
        this.shortcut = shortcut;
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

    public ProjectVO dateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    public ProjectVO dateUntil(Date dateUntil) {
        this.dateUntil = dateUntil;
        return this;
    }

    public ProjectVO probability(float probability) {
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
        return this;
    }

    public ProjectVO totalTime(Integer totalTime) {
        this.totalTime = totalTime;
        return this;
    }

    public ProjectVO introduced(Date introduced) {
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

    public ProjectVO state(String state) {
        this.state = state;
        return this;
    }

    @Override
    public String toString() {
        return "ProjectVO{" +
                "name='" + name + '\'' +
                ", shortcut='" + shortcut + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateUntil=" + dateUntil +
                ", projectManagerId='" + projectManagerId + '\'' +
                ", projectManagerName='" + projectManagerName + '\'' +
                ", workplaceId=" + workplaceId +
                ", budget=" + budget +
                ", budgetParticipation=" + budgetParticipation +
                ", agency=" + agency +
                ", grantTitle=" + grantTitle +
                ", state=" + state +
                ", introduced=" + introduced +
                ", description='" + description + '\'' +
                '}';
    }
}
