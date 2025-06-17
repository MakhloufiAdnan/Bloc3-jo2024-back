-- Insertion des rôles par défaut pour l'application.

INSERT INTO roles (type_role) VALUES ('USER') ON CONFLICT (type_role) DO NOTHING;
INSERT INTO roles (type_role) VALUES ('ADMIN') ON CONFLICT (type_role) DO NOTHING;
INSERT INTO roles (type_role) VALUES ('SCANNER') ON CONFLICT (type_role) DO NOTHING;