package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.Getter;

@Getter
public enum TermState {
    ZIMNI("Z"),
    LETNI("L"),
    ZADNY("N");

    private final String value;

    TermState(String value) {
        this.value = value;
    }

    public static TermState getByValue(String value) {
        return switch (value) {
            case "Z" -> TermState.ZIMNI;
            case "L" -> TermState.LETNI;
            default -> TermState.ZADNY;
        };
    }
}
