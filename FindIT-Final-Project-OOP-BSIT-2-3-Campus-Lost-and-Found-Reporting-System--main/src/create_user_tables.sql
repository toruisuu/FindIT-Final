CREATE TABLE IF NOT EXISTS users (
                                     user_id        SERIAL PRIMARY KEY,
                                     id_number      VARCHAR(20)  NOT NULL UNIQUE,
    full_name      VARCHAR(100) NOT NULL,
    password       VARCHAR(255) NOT NULL,
    role           VARCHAR(10)  NOT NULL DEFAULT 'Student' CHECK (role IN ('Student', 'Admin')),
    contact_number VARCHAR(100) NOT NULL
    );
 