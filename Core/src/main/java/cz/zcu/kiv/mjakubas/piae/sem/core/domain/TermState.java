package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.Getter;

@Getter
public enum TermState {
    Z("Z"),
    L("L"),
    N("N");

    private final String value;

    TermState(String value) {
        this.value = value;
    }

    public static TermState getByValue(String value) {
        TermState returnValue;

        switch (value) {
            case "Z":
                returnValue = TermState.Z;
                break;
            case "L":
                returnValue = TermState.L;
                break;
            default:
                returnValue = TermState.N;
                break;
        }

        return returnValue;
    }
}
