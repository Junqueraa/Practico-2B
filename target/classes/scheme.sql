-- 1. LIMPIEZA INICIAL (En orden inverso a las dependencias)
DROP TABLE IF EXISTS cargos;
DROP TABLE IF EXISTS becas;
DROP TABLE IF EXISTS rendimientos;
DROP TABLE IF EXISTS materias;
DROP TABLE IF EXISTS alumnos;
DROP TABLE IF EXISTS profesores;
DROP TABLE IF EXISTS users;

-- 2. TABLA DE USUARIOS (Login)
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL
);

-- 3. TABLAS PRINCIPALES (Personas y Materias)
CREATE TABLE alumnos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dni INTEGER,
    nombre_y_apellido VARCHAR(255),
    telefono VARCHAR(50),
    correo VARCHAR(255),
    anio_ingreso_u INTEGER
);

CREATE TABLE profesores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dni INTEGER,
    nombre_y_apellido VARCHAR(255),
    telefono VARCHAR(50),
    correo VARCHAR(255)
);

CREATE TABLE materias (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cod_materia INTEGER UNIQUE,
    nombre_materia VARCHAR(255)
);

-- 4. TABLAS ASOCIATIVAS Y DEPENDIENTES
CREATE TABLE rendimientos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    alumno_id INTEGER,
    materia_id INTEGER,
    nota REAL, 
    estado_nota VARCHAR(50), -- Aprobado, Desaprobado, Promocion
    asistencia INTEGER,
    FOREIGN KEY(alumno_id) REFERENCES alumnos(id),
    FOREIGN KEY(materia_id) REFERENCES materias(id)
);

CREATE TABLE cargos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    profesor_id INTEGER,
    materia_id INTEGER,
    cargo VARCHAR(50),       -- ResponsableCatedra, Ayudante, JefePracticos
    periodo VARCHAR(50),     -- UnSemestre, DosSemestres, TresSemestres
    FOREIGN KEY(profesor_id) REFERENCES profesores(id),
    FOREIGN KEY(materia_id) REFERENCES materias(id)
);

CREATE TABLE becas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    alumno_id INTEGER,
    trabaja BOOLEAN,
    FOREIGN KEY(alumno_id) REFERENCES alumnos(id)
);