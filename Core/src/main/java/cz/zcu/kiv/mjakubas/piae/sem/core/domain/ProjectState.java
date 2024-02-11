package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.Getter;

@Getter
public enum ProjectState {
    NOVY("nový"),
    BEZI("běží"),
    DOKONCENY("dokončený");

    private final String value;

    ProjectState(String value) {
        this.value = value;
    }

    public static ProjectState getByValue(String value) {
        ProjectState returnValue;
        switch (value) {
            case "nový":
                returnValue = ProjectState.NOVY;
                break;
            case "běží":
                returnValue = ProjectState.BEZI;
                break;
            default:
                returnValue = ProjectState.DOKONCENY;
                break;
        }

        return returnValue;
    }
}
