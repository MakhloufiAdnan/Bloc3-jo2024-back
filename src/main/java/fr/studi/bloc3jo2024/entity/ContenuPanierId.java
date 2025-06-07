package fr.studi.bloc3jo2024.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContenuPanierId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long panier;
    private Long offre;
}