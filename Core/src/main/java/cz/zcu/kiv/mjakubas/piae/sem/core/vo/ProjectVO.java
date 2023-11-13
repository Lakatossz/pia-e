package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private String projectManagerOrionLogin;
    private Long workplaceId;
    private LocalDate dateFrom;
    private LocalDate dateUntil;
    private String Description;

    public ProjectVO() {

    }

    public ProjectVO(String name, String projectManagerOrionLogin, Long workplaceId, LocalDate dateFrom, LocalDate dateUntil, String description) {
        this.name = name;
        this.projectManagerOrionLogin = projectManagerOrionLogin;
        this.workplaceId = workplaceId;
        this.dateFrom = dateFrom;
        this.dateUntil = dateUntil;
        Description = description;
    }

    public ProjectVO name(String name) {
        this.name = name;
        return this;
    }

    public ProjectVO projectManagerOrionLogin(String projectManagerOrionLogin) {
        this.projectManagerOrionLogin = projectManagerOrionLogin;
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

    public ProjectVO description(String description) {
        Description = description;
        return this;
    }

    @Override
    public String toString() {
        return "ProjectVO{" +
                "name='" + name + '\'' +
                ", projectManagerOrionLogin='" + projectManagerOrionLogin + '\'' +
                ", workplaceId=" + workplaceId +
                ", dateFrom=" + dateFrom +
                ", dateUntil=" + dateUntil +
                ", Description='" + Description + '\'' +
                '}';
    }
}
