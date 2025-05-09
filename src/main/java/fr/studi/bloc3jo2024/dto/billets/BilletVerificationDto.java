package fr.studi.bloc3jo2024.dto.billets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BilletVerificationDto {
    private Long idBillet;
    private String cleFinaleBillet;
    private UUID idUtilisateur;
    private String nomUtilisateur;
    private List<String> offres;
    private Date dateAchat;
}