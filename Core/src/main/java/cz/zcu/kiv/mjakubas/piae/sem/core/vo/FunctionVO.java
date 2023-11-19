package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FunctionVO {

    private long id;
    private String name;
    private LocalDate dateFrom;
    private LocalDate dateUntil;
    private float probability;
    private float defaultTime;
    private Long functionManager;
    private Long functionWorkplace;

    public FunctionVO() {

    }

    public FunctionVO(long id, String name, LocalDate dateFrom,
                    LocalDate dateUntil, float probability, float defaultTime,
                      Long functionManager, Long functionWorkplace) {
        this.id = id;
        this.name = name;
        this.dateFrom = dateFrom;
        this.dateUntil = dateUntil;
        this.probability = probability;
        this.defaultTime = defaultTime;
        this.functionManager = functionManager;
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

    public FunctionVO functionManager(Long functionManager) {
        this.functionManager = functionManager;
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
                ", dateFrom=" + dateFrom +
                ", dateUntil=" + dateUntil +
                ", probability=" + probability +
                ", defaultTime=" + defaultTime +
                ", functionManager=" + functionManager +
                ", functionWorkplace=" + functionWorkplace +
                '}';
    }
}
