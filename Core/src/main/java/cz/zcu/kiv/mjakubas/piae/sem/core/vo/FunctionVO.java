package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class FunctionVO {

    private long id;
    private String name;
    private String shortcut;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateFrom;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateUntil;
    private float probability;
    private float defaultTime;
    private Long functionManagerId;
    private String functionManagerName;
    private Long functionWorkplace;
    private String description;

    public FunctionVO id(long id) {
        this.id = id;
        return this;
    }

    public FunctionVO name(String name) {
        this.name = name;
        return this;
    }

    public FunctionVO shortcut(String shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    public FunctionVO dateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    public FunctionVO dateUntil(Date dateUntil) {
        this.dateUntil = dateUntil;
        return this;
    }

    public FunctionVO probability(float probability) {
        this.probability = probability;
        return this;
    }

    public FunctionVO defaultTime(float defaultTime) {
        this.defaultTime = defaultTime;
        return this;
    }

    public FunctionVO functionManagerId(Long functionManagerId) {
        this.functionManagerId = functionManagerId;
        return this;
    }

    public FunctionVO functionManagerName(String functionManagerName) {
        this.functionManagerName = functionManagerName;
        return this;
    }

    public FunctionVO functionWorkplace(Long functionWorkplace) {
        this.functionWorkplace = functionWorkplace;
        return this;
    }

    public FunctionVO description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "FunctionVO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", shortcut='" + shortcut + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateUntil=" + dateUntil +
                ", probability=" + probability +
                ", defaultTime=" + defaultTime +
                ", functionManagerId=" + functionManagerId +
                ", functionManagerName=" + functionManagerName +
                ", functionWorkplace=" + functionWorkplace +
                ", description=" + description +
                '}';
    }
}
