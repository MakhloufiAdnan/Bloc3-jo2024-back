-- database/schema/create_enums.sql

DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'type_tel') THEN
            CREATE TYPE type_tel AS ENUM ('MOBILE', 'FIXE');
        END IF;
    END$$;

DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'type_role') THEN
            CREATE TYPE type_role AS ENUM ('ADMIN', 'USER');
        END IF;
    END$$;

DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'type_offre') THEN
            CREATE TYPE type_offre AS ENUM ('SOLO', 'DUO', 'FAMILIALE');
        END IF;
    END$$;

DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'type_auth_token_temp') THEN
            CREATE TYPE type_auth_token_temp AS ENUM ('CONNEXION', 'RESET_PASSWORD', 'VALIDATION_EMAIL');
        END IF;
    END$$;

DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'statut_transaction') THEN
            CREATE TYPE statut_transaction AS ENUM ('REUSSI', 'ECHEC', 'EN_ATTENTE');
        END IF;
    END$$;

DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'statut_panier') THEN
            CREATE TYPE statut_panier AS ENUM ('EN_ATTENTE', 'PAYE', 'SAUVEGARDE');
        END IF;
    END$$;

DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'statut_offre') THEN
            CREATE TYPE statut_offre AS ENUM ('DISPONIBLE', 'EPUISE', 'ANNULE', 'EXPIRE');
        END IF;
    END$$;

DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'methode_payement_enum') THEN
            CREATE TYPE methode_payement_enum AS ENUM ('CARTE_BANCAIRE', 'PAYPAL', 'STRIPE');
        END IF;
    END$$;