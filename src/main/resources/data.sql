INSERT INTO roles (type_role) VALUES ('USER') ON CONFLICT (type_role) DO NOTHING;
INSERT INTO roles (type_role) VALUES ('ADMIN') ON CONFLICT (type_role) DO NOTHING;