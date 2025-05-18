package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Authentification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthentificationRepository extends JpaRepository<Authentification, UUID> {

}