select * from users
drop table users

CREATE TABLE users (
    id_usuario VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    birth_date DATE,
    document_id VARCHAR(20) UNIQUE NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(150) UNIQUE NOT NULL,
    base_salary NUMERIC(15,2) NOT NULL,
    password VARCHAR(100)NOT NULL,
    rol_id VARCHAR(36) NOT NULL,
    FOREIGN KEY (rol_id) REFERENCES rol(rol_id)
);

CREATE TABLE rol (
    rol_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(100)
);

INSERT INTO rol (rol_id, name, description) VALUES
    ('rol-001', 'Admin', 'tiene todos los privilegios'),
('rol-002', 'adviser', 'algunos'),
('rol-003', 'customer ', 'algunos');
