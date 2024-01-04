package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ProjectState {
    NOVY("nový"),
    BEZI("běží"),
    DOKONCENY("dokončený");

    private String value;
}
