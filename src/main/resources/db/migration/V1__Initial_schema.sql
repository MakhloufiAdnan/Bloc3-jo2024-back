-- Ce script est exécuté une seule fois par Testcontainers au démarrage du conteneur PostgreSQL.
-- Il garantit que les types ENUM PostgreSQL et toutes les tables sont créés de manière stable,
-- reflétant le modèle de données des entités JPA.

-- =================================================================================================
-- 1. DÉFINITION DES TYPES ENUMÉRÉS
-- =================================================================================================

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'type_tel') THEN
        CREATE TYPE type_tel AS ENUM ('MOBILE', 'FIXE');
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'type_role') THEN
        CREATE TYPE type_role AS ENUM ('ADMIN', 'USER', 'SCANNER');
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'type_offre') THEN
        CREATE TYPE type_offre AS ENUM ('SOLO', 'DUO', 'FAMILIALE');
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'type_token') THEN
        CREATE TYPE type_token AS ENUM ('CONNEXION', 'RESET_PASSWORD', 'VALIDATION_EMAIL');
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'statut_transaction') THEN
        CREATE TYPE statut_transaction AS ENUM ('REUSSI', 'ECHEC', 'EN_ATTENTE');
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'statut_panier') THEN
        CREATE TYPE statut_panier AS ENUM ('EN_ATTENTE', 'COMMANDE', 'SAUVEGARDE');
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'statut_offre') THEN
        CREATE TYPE statut_offre AS ENUM ('DISPONIBLE', 'EPUISE', 'ANNULE', 'EXPIRE');
    END IF;
END $$;

DO $$ BEGIN
    -- Utilisation de 'pg_catalog.pg_type' pour une compatibilité plus large avec d'anciennes versions de PostgreSQL
    IF NOT EXISTS (SELECT 1 FROM pg_catalog.pg_type WHERE typname = 'nom_methode_paiement') THEN
        CREATE TYPE nom_methode_paiement AS ENUM ('CARTE_BANCAIRE', 'PAYPAL', 'STRIPE');
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'statut_paiement') THEN
        CREATE TYPE statut_paiement AS ENUM ('EN_ATTENTE', 'ACCEPTE', 'REFUSE');
    END IF;
END $$;


-- =================================================================================================
-- 2. DÉFINITION DES TABLES
-- =================================================================================================

-- Table : roles
CREATE TABLE IF NOT EXISTS roles (
                                     id_role BIGSERIAL PRIMARY KEY,
                                     type_role type_role NOT NULL UNIQUE
);

-- Table : pays
CREATE TABLE IF NOT EXISTS pays (
                                    id_pays BIGSERIAL PRIMARY KEY,
                                    nom_pays VARCHAR(100) NOT NULL UNIQUE
);

-- Table : adresses (Dépend de : pays)
CREATE TABLE IF NOT EXISTS adresses (
                                        id_adresse BIGSERIAL PRIMARY KEY,
                                        numero_rue INT NOT NULL,
                                        nom_rue VARCHAR(250) NOT NULL,
                                        ville VARCHAR(100) NOT NULL,
                                        code_postal VARCHAR(10) NOT NULL,
                                        id_pays BIGINT NOT NULL,
                                        FOREIGN KEY (id_pays) REFERENCES pays(id_pays)
);

-- Table : epreuves (Indépendante)
CREATE TABLE IF NOT EXISTS epreuves (
                                        id_epreuve BIGSERIAL PRIMARY KEY,
                                        nom_epreuve VARCHAR(100) NOT NULL UNIQUE,
                                        is_featured BOOLEAN DEFAULT FALSE NOT NULL
);

-- Table : utilisateurs (Dépend de : roles, adresses)
CREATE TABLE IF NOT EXISTS utilisateurs (
                                            id_utilisateur_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                            email VARCHAR(255) NOT NULL UNIQUE,
                                            nom VARCHAR(255) NOT NULL,
                                            prenom VARCHAR(255) NOT NULL,
                                            date_naissance DATE NOT NULL,
                                            date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
                                            cle_utilisateur VARCHAR(255) UNIQUE,
                                            is_verified BOOLEAN NOT NULL DEFAULT FALSE,
                                            id_role BIGINT NOT NULL,
                                            id_adresse BIGINT NOT NULL,
                                            FOREIGN KEY (id_role) REFERENCES roles(id_role),
                                            FOREIGN KEY (id_adresse) REFERENCES adresses(id_adresse)
);
CREATE INDEX IF NOT EXISTS idx_utilisateurs_email ON utilisateurs (email);
CREATE INDEX IF NOT EXISTS idx_utilisateurs_adresse ON utilisateurs (id_adresse);

-- Table : authentifications (Dépend de : utilisateurs)
CREATE TABLE IF NOT EXISTS authentifications (
                                                 id_token_uuid UUID PRIMARY KEY,
                                                 mot_passe_hache VARCHAR(255) NOT NULL,
                                                 id_utilisateur_uuid UUID NOT NULL UNIQUE,
                                                 FOREIGN KEY (id_utilisateur_uuid) REFERENCES utilisateurs(id_utilisateur_uuid)
);

-- Table : auth_tokens_temporaire (Dépend de : utilisateurs)
CREATE TABLE IF NOT EXISTS auth_tokens_temporaire (
                                                      id_token_temp_uuid UUID PRIMARY KEY,
                                                      token_identifier VARCHAR(36) NOT NULL UNIQUE,
                                                      token_hache TEXT NOT NULL UNIQUE,
                                                      type_token type_token NOT NULL,
                                                      date_expiration TIMESTAMP NOT NULL,
                                                      is_used BOOLEAN DEFAULT FALSE NOT NULL,
                                                      id_utilisateur_uuid UUID NOT NULL,
                                                      FOREIGN KEY (id_utilisateur_uuid) REFERENCES utilisateurs(id_utilisateur_uuid)
);
CREATE INDEX IF NOT EXISTS idx_authtokentemp_token_identifier ON auth_tokens_temporaire (token_identifier);
CREATE INDEX IF NOT EXISTS idx_authtokentemp_token_hache ON auth_tokens_temporaire (token_hache);


-- Table : disciplines (Dépend de : adresses)
CREATE TABLE IF NOT EXISTS disciplines (
                                           id_discipline BIGSERIAL PRIMARY KEY,
                                           nom_discipline VARCHAR(100) NOT NULL UNIQUE,
                                           date_discipline TIMESTAMP NOT NULL,
                                           nb_place_dispo INT NOT NULL CHECK (nb_place_dispo >= 0),
                                           is_featured BOOLEAN DEFAULT FALSE NOT NULL,
                                           version BIGINT,
                                           id_adresse BIGINT NOT NULL,
                                           FOREIGN KEY (id_adresse) REFERENCES adresses(id_adresse)
);
CREATE INDEX IF NOT EXISTS idx_discipline_nom ON disciplines (nom_discipline);
CREATE INDEX IF NOT EXISTS idx_discipline_date ON disciplines (date_discipline);


-- Table : offres (Dépend de : disciplines)
CREATE TABLE IF NOT EXISTS offres (
                                      id_offre BIGSERIAL PRIMARY KEY,
                                      type_offre type_offre NOT NULL,
                                      quantite INT NOT NULL,
                                      prix DECIMAL(10, 2) NOT NULL,
                                      capacite INT NOT NULL,
                                      date_expiration TIMESTAMP,
                                      statut_offre statut_offre NOT NULL,
                                      version BIGINT,
                                      id_discipline BIGINT NOT NULL,
                                      featured BOOLEAN DEFAULT FALSE NOT NULL,
                                      FOREIGN KEY (id_discipline) REFERENCES disciplines(id_discipline)
);
CREATE INDEX IF NOT EXISTS idx_offre_statut ON offres (statut_offre);
CREATE INDEX IF NOT EXISTS idx_offre_date_expiration ON offres (date_expiration);
CREATE INDEX IF NOT EXISTS idx_offre_discipline ON offres (id_discipline);


-- Table : methodes_paiement (Indépendante)
CREATE TABLE IF NOT EXISTS methodes_paiement (
                                                 id_methode BIGSERIAL PRIMARY KEY,
                                                 nom_methode_paiement nom_methode_paiement NOT NULL UNIQUE
);

-- Table : paniers (Dépend de : utilisateurs)
CREATE TABLE IF NOT EXISTS paniers (
                                       id_panier BIGSERIAL PRIMARY KEY,
                                       montant_total DECIMAL(38,2) NOT NULL,
                                       statut_panier statut_panier NOT NULL,
                                       date_ajout TIMESTAMP NOT NULL,
                                       id_utilisateur_uuid UUID NOT NULL,
                                       version BIGINT,
                                       FOREIGN KEY (id_utilisateur_uuid) REFERENCES utilisateurs(id_utilisateur_uuid)
);
CREATE INDEX IF NOT EXISTS idx_paniers_statut ON paniers (statut_panier);


-- Table d'association : contenu_panier (Dépend de : offres, paniers)
-- Cette table est une entité JPA avec une clé composite comme PK.
CREATE TABLE IF NOT EXISTS contenu_panier (
                                              id_panier BIGINT NOT NULL,
                                              id_offre BIGINT NOT NULL,
                                              quantite_commandee INT NOT NULL,
                                              PRIMARY KEY (id_panier, id_offre),
                                              FOREIGN KEY (id_panier) REFERENCES paniers(id_panier),
                                              FOREIGN KEY (id_offre) REFERENCES offres(id_offre)
);

-- Table : paiements (Dépend de : methodes_paiement, utilisateurs, paniers)
CREATE TABLE IF NOT EXISTS paiements (
                                         id_paiement BIGSERIAL PRIMARY KEY,
                                         statut_paiement statut_paiement NOT NULL,
                                         date_paiement TIMESTAMP NOT NULL,
                                         montant DECIMAL(38,2) NOT NULL,
                                         id_methode_paiement BIGINT NOT NULL,
                                         id_utilisateur UUID NOT NULL,
                                         id_panier BIGINT NOT NULL UNIQUE,
                                         FOREIGN KEY (id_methode_paiement) REFERENCES methodes_paiement(id_methode),
                                         FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs(id_utilisateur_uuid),
                                         FOREIGN KEY (id_panier) REFERENCES paniers(id_panier)
);

-- Table : transactions (Dépend de : paiements)
CREATE TABLE IF NOT EXISTS transactions (
                                            id_transaction BIGSERIAL PRIMARY KEY,
                                            montant DECIMAL(38,2) NOT NULL CHECK (montant >= 0),
                                            date_transaction TIMESTAMP NOT NULL DEFAULT NOW(),
                                            statut_transaction statut_transaction NOT NULL,
                                            date_validation TIMESTAMP,
                                            details_transaction TEXT,
                                            is_test BOOLEAN NOT NULL DEFAULT FALSE,
                                            id_payement BIGINT NOT NULL UNIQUE,
                                            FOREIGN KEY (id_payement) REFERENCES paiements(id_paiement)
);

-- Table : commandes (Dépend de : paiements)
CREATE TABLE IF NOT EXISTS commandes (
                                         id_commande BIGSERIAL PRIMARY KEY,
                                         num_commande VARCHAR(20) NOT NULL UNIQUE,
                                         envoye_mail BOOLEAN NOT NULL,
                                         id_payement BIGINT NOT NULL UNIQUE,
                                         FOREIGN KEY (id_payement) REFERENCES paiements(id_paiement)
);

-- Table : telephones (Dépend de: utilisateurs)
CREATE TABLE IF NOT EXISTS telephones (
                                          id_telephone BIGSERIAL PRIMARY KEY,
                                          type_tel type_tel NOT NULL,
                                          numero_telephone VARCHAR(20) NOT NULL UNIQUE,
                                          id_utilisateur_uuid UUID NOT NULL,
                                          FOREIGN KEY (id_utilisateur_uuid) REFERENCES utilisateurs(id_utilisateur_uuid)
);

-- Table : billets (Dépend de : utilisateurs)
CREATE TABLE IF NOT EXISTS billets (
                                       id_billet BIGSERIAL PRIMARY KEY,
                                       cle_finale_billet TEXT NOT NULL UNIQUE,
                                       qr_code_image BYTEA,
                                       id_utilisateur_uuid UUID NOT NULL,
                                       is_scanned boolean DEFAULT FALSE NOT NULL,
                                       scanned_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                       purchase_date TIMESTAMP NOT NULL DEFAULT NOW(),
                                       FOREIGN KEY (id_utilisateur_uuid) REFERENCES utilisateurs(id_utilisateur_uuid)
);

-- Table de jointure: billet_offre (Dépend de: billets, offres)
CREATE TABLE IF NOT EXISTS billet_offre (
                                            id_billet BIGINT NOT NULL,
                                            id_offre BIGINT NOT NULL,
                                            PRIMARY KEY (id_billet, id_offre),
                                            FOREIGN KEY (id_billet) REFERENCES billets(id_billet),
                                            FOREIGN KEY (id_offre) REFERENCES offres(id_offre)
);

-- Table : athletes (Dépend de : pays)
CREATE TABLE IF NOT EXISTS athletes (
                                        id_athlete BIGSERIAL PRIMARY KEY,
                                        nom VARCHAR(100) NOT NULL,
                                        prenom VARCHAR(100) NOT NULL,
                                        id_pays BIGINT NOT NULL,
                                        date_naissance DATE,
                                        genre VARCHAR(10),
                                        FOREIGN KEY (id_pays) REFERENCES pays(id_pays)
);

-- Table de jointure : comporter (Dépend de : disciplines, epreuves)
CREATE TABLE IF NOT EXISTS comporter (
                                         id_discipline BIGINT NOT NULL,
                                         id_epreuve BIGINT NOT NULL,
                                         jr_de_medaille BOOLEAN,
                                         PRIMARY KEY (id_discipline, id_epreuve),
                                         FOREIGN KEY (id_discipline) REFERENCES disciplines(id_discipline),
                                         FOREIGN KEY (id_epreuve) REFERENCES epreuves(id_epreuve)
);
CREATE INDEX IF NOT EXISTS idx_comporter_jr_de_medaille ON comporter (jr_de_medaille);

-- Table de jointure : pratiquer (Dépend de : athletes, epreuves)
CREATE TABLE IF NOT EXISTS pratiquer (
                                         id_athlete BIGINT NOT NULL,
                                         id_epreuve BIGINT NOT NULL,
                                         PRIMARY KEY (id_athlete, id_epreuve),
                                         FOREIGN KEY (id_athlete) REFERENCES athletes(id_athlete),
                                         FOREIGN KEY (id_epreuve) REFERENCES epreuves(id_epreuve)
);

-- Table : oauths (Dépend de : utilisateurs)
CREATE TABLE IF NOT EXISTS oauths (
                                      id_oauth UUID PRIMARY KEY,
                                      id_utilisateur_uuid UUID NOT NULL UNIQUE,
                                      google_id VARCHAR(255) UNIQUE,
                                      facebook_id VARCHAR(255) UNIQUE,
                                      FOREIGN KEY (id_utilisateur_uuid) REFERENCES utilisateurs(id_utilisateur_uuid)
);