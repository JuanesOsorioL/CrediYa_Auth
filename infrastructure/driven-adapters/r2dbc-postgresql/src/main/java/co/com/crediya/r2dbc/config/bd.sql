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
                       base_salary NUMERIC(15,2) NOT NULL
);