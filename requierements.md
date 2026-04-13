# **Proyecto Integrador: Especificación, Gestión y Planificación**

## **1. Requerimientos**

- **Problema que se quiere resolver:** Gestión de docentes de una Universidad.
- **Usuarios del Sistema:** Los usuarios van a ser los administradores de la Universidad encargados de gestionar la información de los docentes.
- **Funcionalidades Principales:** Las funcionalidades principales son login, registro de usuarios y dashboard.
- **Tamaño del Equipo:** A la hora de realizar el programa en Ingeniería de Software I éramos un grupo de 3 integrantes, ahora somos 2.
- **Tecnologías Elegidas y Justificación:** Java con Spark como servidor web, SQLite como base de datos, ActiveJDBC como ORM, Mustache para las vistas y Maven para la gestión de dependencias.
- **Plazo Estimado:** Usamos la metodolog2)c)
	
	"""Como análisis podemos ver que el equipo tiende a identificar riesgos   
	relacionados con la dinámica y experiencia propia del grupo, mientras que la 
	IA, si bien detecta la mayoría de los riesgos, se enfoca más en los aspectos
	 técnicos y estructurales del proyecto."""
	
	
3)
Diagrama de Arquitectura:

graph TD
    Cliente["Cliente / Navegador Web"] -->|"Peticiones HTTP (GET/POST)"| Servidor["Servidor Web - Spark Java"]
    
    Servidor -->|"Renderiza Vistas"| Vistas["Mustache - Interfaz de Usuario"]
    Servidor -->|"Consultas / Operaciones"| ORM["ActiveJDBC - ORM"]
    
    ORM -->|"Lectura / Escritura de Datos"| BD[("Base de Datos - SQLite")]
    
    Vistas -->|"HTML / CSS"| Cliente


Diagrama de clases:

classDiagram
    class PERSONA {
        -Dni: int
        -Nya: char
        -Tel: int
        -Correo: char
    }
    class Alumno {
        -Año_Ingreso_U: int
        -Id_Alumno: int
    }
    class Profesor {
        -Id_Profesor: int
    }
    class Seguimiento {
        -Asistencia: int
        -Trabajos: boolean
        -Promedio: int
    }
    class Nota {
        <<Association Class>>
        -Nota: int
        -Estado_Nota: TipoEstadoNota
    }
    class MATERIA {
        -Nombre_Mat: char
        -Cod_Mat: int
    }
    class PLAN {
        -Codigo_Plan: int
        -Nombre_Plan: char
        -Año_Res: int
    }
    class CARRERA {
        -Nombre_Carrera: char
        -Codigo_Carrera: int
        -Facultad: char
    }
    class Cargo {
        <<Association Class>>
        -Cargo: TipoCargo
        -Periodo: TipoPeriodo
    }
    class Condicion {
        <<Association Class>>
        -Condicion: TipoCondicion
    }
    class TipoEstadoNota {
        <<enumeration>>
        Aprobado
        Desaprobado
        Promocion
    }
    class TipoCondicion {
        <<enumeration>>
        Aprobado
        Regular
        Libre
    }
    class TipoCargo {
        <<enumeration>>
        ResponsableCatedra
        Ayudante
        JefePracticos
    }
    class TipoPeriodo {
        <<enumeration>>
        UnSemestre
        DosSemestres
        TresSemestres
    }

    %% Relaciones
    PERSONA <|-- Alumno
    PERSONA <|-- Profesor
    Alumno "1" -- "1" Seguimiento
    Alumno "1..*" -- "1..*" MATERIA : rinde (Nota)
    Profesor "1..*" -- "1..*" MATERIA : dicta (Cargo)
    MATERIA "0..*" -- "0..*" MATERIA : Correlatividad (Condicion)
    PLAN "1" -- "1..*" MATERIA
    CARRERA "1" -- "1" PLAN : Vigente
    CARRERA "1" -- "0..*" PLAN : Otros
    Alumno "1" -- "1" Profesor : Ayudante
    Alumno "1" -- "1" Profesor : Estudia

4. Crear un backlog en github projects.ía ágil SCRUM con sprints de 2 a 4 semanas.
- **Problemas encontrados:** Al no tener experiencia previa con HTML, hubo dificultades para la realización del front de la página, además de no entender cómo funcionaban los GET y POST en la aplicación.
- **Forma de Organización del Equipo:** El equipo se juntaba a trabajar en conjunto y cada integrante aportaba ideas que luego se integraban al proyecto.

## **2. Auditoría**

| **Tipo de Riesgo** | **Descripción** | **Probabilidad** | **Impacto** | **Identificado por** |
|--------------|-----------|------------|--------|---------------|
|Humano|Falta de roles definido|Alto|Medio|Ambos|
|Humano|Equipo de solo dos personas|Media|Muy alto|IA|
|Técnico|Curva de arendizadeje en nuevas tecnologías|Alta|Alto|Ambos|
|Técnico|Elección de SQLite para un sistema universitario|Media|Alto|IA|
|Organizacional|Funcionalidades poco especificadas|Alta|Medio|IA|
|Planificación|Incumplimiento fecha entrega por grupo reducido|Alta|Alta|Equipo|

Como análisis podemos ver que el equipo tiende a identificar riesgos relacionados con la dinámica y experiencia propia del grupo, mientras que la IA, si bien detecta la mayoría de los riesgos, se enfoca más en los aspectos técnicos y estructurales del proyecto.
	
	
## **3) Diagrama de Arquitectura**

graph TD
    Cliente["Cliente / Navegador Web"] -->|"Peticiones HTTP (GET/POST)"| Servidor["Servidor Web - Spark Java"]
    
    Servidor -->|"Renderiza Vistas"| Vistas["Mustache - Interfaz de Usuario"]
    Servidor -->|"Consultas / Operaciones"| ORM["ActiveJDBC - ORM"]
    
    ORM -->|"Lectura / Escritura de Datos"| BD[("Base de Datos - SQLite")]
    
    Vistas -->|"HTML / CSS"| Cliente

**Diagrama de clases:**

classDiagram
    class PERSONA {
        -Dni: int
        -Nya: char
        -Tel: int
        -Correo: char
    }
    class Alumno {
        -Año_Ingreso_U: int
        -Id_Alumno: int
    }
    class Profesor {
        -Id_Profesor: int
    }
    class Seguimiento {
        -Asistencia: int
        -Trabajos: boolean
        -Promedio: int
    }
    class Nota {
        <<Association Class>>
        -Nota: int
        -Estado_Nota: TipoEstadoNota
    }
    class MATERIA {
        -Nombre_Mat: char
        -Cod_Mat: int
    }
    class PLAN {
        -Codigo_Plan: int
        -Nombre_Plan: char
        -Año_Res: int
    }
    class CARRERA {
        -Nombre_Carrera: char
        -Codigo_Carrera: int
        -Facultad: char
    }
    class Cargo {
        <<Association Class>>
        -Cargo: TipoCargo
        -Periodo: TipoPeriodo
    }
    class Condicion {
        <<Association Class>>
        -Condicion: TipoCondicion
    }
    class TipoEstadoNota {
        <<enumeration>>
        Aprobado
        Desaprobado
        Promocion
    }
    class TipoCondicion {
        <<enumeration>>
        Aprobado
        Regular
        Libre
    }
    class TipoCargo {
        <<enumeration>>
        ResponsableCatedra
        Ayudante
        JefePracticos
    }
    class TipoPeriodo {
        <<enumeration>>
        UnSemestre
        DosSemestres
        TresSemestres
    }

    %% Relaciones
    PERSONA <|-- Alumno
    PERSONA <|-- Profesor
    Alumno "1" -- "1" Seguimiento
    Alumno "1..*" -- "1..*" MATERIA : rinde (Nota)
    Profesor "1..*" -- "1..*" MATERIA : dicta (Cargo)
    MATERIA "0..*" -- "0..*" MATERIA : Correlatividad (Condicion)
    PLAN "1" -- "1..*" MATERIA
    CARRERA "1" -- "1" PLAN : Vigente
    CARRERA "1" -- "0..*" PLAN : Otros
    Alumno "1" -- "1" Profesor : Ayudante
    Alumno "1" -- "1" Profesor : Estudia

## **4. Crear un backlog en github projects**
