-- data.sql
INSERT INTO roles (id_role, type_role) VALUES (1, 'USER') ON CONFLICT (type_role) DO NOTHING;
INSERT INTO roles (id_role, type_role) VALUES (2, 'ADMIN') ON CONFLICT (type_role) DO NOTHING;