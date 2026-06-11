CREATE TABLE IF NOT EXISTS alumnos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dni INTEGER,
    nombre_y_apellido VARCHAR(255),
    telefono VARCHAR(50),
    correo VARCHAR(255),
    anio_ingreso_u INTEGER,
    trabaja INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS profesores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dni INTEGER,
    nombre_y_apellido VARCHAR(255),
    telefono VARCHAR(50),
    correo VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'admin',
    alumno_id INTEGER,
    profesor_id INTEGER,
    FOREIGN KEY(alumno_id) REFERENCES alumnos(id),
    FOREIGN KEY(profesor_id) REFERENCES profesores(id)
);

CREATE TABLE IF NOT EXISTS materias (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cod_materia INTEGER UNIQUE,
    nombre_materia VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS rendimientos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    alumno_id INTEGER,
    materia_id INTEGER,
    nota REAL,
    estado_nota VARCHAR(50),
    asistencia INTEGER,
    FOREIGN KEY(alumno_id) REFERENCES alumnos(id),
    FOREIGN KEY(materia_id) REFERENCES materias(id)
);

CREATE TABLE IF NOT EXISTS cargos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    profesor_id INTEGER,
    materia_id INTEGER,
    cargo VARCHAR(50),
    periodo VARCHAR(50),
    FOREIGN KEY(profesor_id) REFERENCES profesores(id),
    FOREIGN KEY(materia_id) REFERENCES materias(id)
);

CREATE TABLE IF NOT EXISTS becas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    alumno_id INTEGER,
    trabaja BOOLEAN,
    FOREIGN KEY(alumno_id) REFERENCES alumnos(id)
);

CREATE TABLE IF NOT EXISTS inscripciones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    alumno_id INTEGER,
    materia_id INTEGER,
    FOREIGN KEY(alumno_id) REFERENCES alumnos(id),
    FOREIGN KEY(materia_id) REFERENCES materias(id)
);
