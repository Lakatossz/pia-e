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
        return switch (value) {
            case "Z" -> TermState.Z;
            case "L" -> TermState.L;
            default -> TermState.N;
        };
    }
}
