package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FunctionVO {

    private long id;
    private String name;
    private String shortcut;
    private LocalDate dateFrom;
    private LocalDate dateUntil;
    private float probability;
    private float defaultTime;
    private Long functionManagerId;
    private String functionManagerName;
    private Long functionWorkplace;

    public FunctionVO() {

    }

    public FunctionVO(long id, String name, String shortcut, LocalDate dateFrom,
                    LocalDate dateUntil, float probability, float defaultTime,
                      Long functionManagerId, String functionManagerName, Long functionWorkplace) {
        this.id = id;
        this.name = name;
        this.shortcut = shortcut;
        this.dateFrom = dateFrom;
        this.dateUntil = dateUntil;
        this.probability = probability;
        this.defaultTime = defaultTime;
        this.functionManagerId = functionManagerId;
        this.functionManagerName = functionManagerName;
        this.functionWorkplace = functionWorkplace;
    }

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

    public FunctionVO dateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    public FunctionVO dateUntil(LocalDate dateUntil) {
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
                '}';
    }
}
