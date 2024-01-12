package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.Getter;

@Getter
public enum ProjectState {
    NOVY("nový"),
    BEZI("běží"),
    DOKONCENY("dokončený");

    private String value;

    ProjectState(String value) {
        this.value = value;
    }

    public static ProjectState getByValue(String value) {
        return switch (value) {
            case "nový" -> ProjectState.NOVY;
            case "běží" -> ProjectState.BEZI;
            default -> ProjectState.DOKONCENY;
        };
    }

    public void setValue(String value) {
        this.value = value;
    }
}
