create table if not exists rol (
    rol_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(100)
    );


create table if not exists users (
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


INSERT INTO users (
    id_usuario, first_name, last_name, birth_date, document_id, phone, email, base_salary, password, rol_id
) VALUES
      ('user-004','Sofía','Ramírez','1988-02-14','2000000004','555-0404','sofia.ramirez@ejemplo.com', 1200000.00,'pass004','rol-003'),
      ('user-005','Luis','Torres','1987-07-09','2000000005','555-0405','luis.torres@ejemplo.com',     2500000.00,'pass005','rol-003'),
      ('user-006','María','López','1992-11-30','2000000006','555-0406','maria.lopez@ejemplo.com',     3500000.00,'pass006','rol-003'),
      ('user-007','Pedro','Castillo','1984-03-21','2000000007','555-0407','pedro.castillo@ejemplo.com',1800000.00,'pass007','rol-003'),
      ('user-008','Laura','Mendoza','1991-09-12','2000000008','555-0408','laura.mendoza@ejemplo.com',  4200000.00,'pass008','rol-003'),
      ('user-009','Diego','Álvarez','1990-01-19','2000000009','555-0409','diego.alvarez@ejemplo.com',  5200000.00,'pass009','rol-003'),
      ('user-010','Valentina','Ríos','1993-05-07','2000000010','555-0410','valentina.rios@ejemplo.com',3100000.00,'pass010','rol-003'),
      ('user-011','Andrés','García','1986-08-18','2000000011','555-0411','andres.garcia@ejemplo.com',  9800000.00,'pass011','rol-003'),
      ('user-012','Camila','Herrera','1994-12-03','2000000012','555-0412','camila.herrera@ejemplo.com',15000000.00,'pass012','rol-003'),
      ('user-013','Jorge','Martínez','1983-04-11','2000000013','555-0413','jorge.martinez@ejemplo.com',7200000.00,'pass013','rol-003'),
      ('user-014','Daniela','Cruz','1996-06-26','2000000014','555-0414','daniela.cruz@ejemplo.com',    2600000.00,'pass014','rol-003'),
      ('user-015','Sebastián','Ortega','1989-10-15','2000000015','555-0415','sebastian.ortega@ejemplo.com',8400000.00,'pass015','rol-003'),
      ('user-016','Natalia','Vargas','1992-02-28','2000000016','555-0416','natalia.vargas@ejemplo.com',10500000.00,'pass016','rol-003'),
      ('user-017','Fernando','Morales','1981-03-08','2000000017','555-0417','fernando.morales@ejemplo.com',5600000.00,'pass017','rol-003'),
      ('user-018','Patricia','Navarro','1987-12-22','2000000018','555-0418','patricia.navarro@ejemplo.com',1300000.00,'pass018','rol-003'),
      ('user-019','Ricardo','Pineda','1995-01-05','2000000019','555-0419','ricardo.pineda@ejemplo.com', 6900000.00,'pass019','rol-003'),
      ('user-020','Mónica','Salazar','1993-09-01','2000000020','555-0420','monica.salazar@ejemplo.com', 2100000.00,'pass020','rol-003'),
      ('user-021','Julián','Gómez','1988-06-13','2000000021','555-0421','julian.gomez@ejemplo.com',     750000.00,'pass021','rol-003'),
      ('user-022','Carolina','Vega','1991-07-24','2000000022','555-0422','carolina.vega@ejemplo.com',   4300000.00,'pass022','rol-003'),
      ('user-023','Esteban','Ruiz','1985-11-02','2000000023','555-0423','esteban.ruiz@ejemplo.com',     8700000.00,'pass023','rol-003');

