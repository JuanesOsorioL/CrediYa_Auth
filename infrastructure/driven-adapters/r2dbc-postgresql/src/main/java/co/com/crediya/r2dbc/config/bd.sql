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
('rol-001', 'Admin', 'Registro de usuarios'),
('rol-002', 'Adviser', 'Registro de usuarios'),
('rol-003', 'Customer', 'solicitud de prestamo y Solo para el mismo');


INSERT INTO users (
    id_usuario, first_name, last_name, birth_date, document_id, phone, email, base_salary, password, rol_id
)
VALUES (
           'user-001', 'Juan', 'Pérez', '1985-05-15', '1234567890', '555-0101', 'juan.perez@ejemplo.com', 50000.00, 'pass123', 'rol-001');

INSERT INTO users (
    id_usuario, first_name, last_name, birth_date, document_id, phone, email, base_salary, password, rol_id
)
VALUES (
           'user-002', 'Ana', 'González', '1990-08-25', '0987654321', '555-0202', 'ana.gonzalez@ejemplo.com', 40000.00, 'pass456', 'rol-002');

INSERT INTO users (
    id_usuario, first_name, last_name, birth_date, document_id, phone, email, base_salary, password, rol_id
)
VALUES (
           'user-003', 'Carlos', 'Sánchez', '1995-03-10', '1122334455', '555-0303', 'carlos.sanchez@ejemplo.com', 30000.00, 'pass789', 'rol-003');
