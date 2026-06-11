package com.is1.proyecto;

import com.fasterxml.jackson.databind.ObjectMapper;
import static spark.Spark.*;

import org.javalite.activejdbc.Base;
import com.is1.proyecto.config.DBConfigSingleton;
import com.is1.proyecto.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.io.InputStream;
import java.util.Scanner;

public class App {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Columnas que pueden no existir en bases de datos anteriores
    private static final String[] MIGRATIONS = {
        "ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'admin'",
        "ALTER TABLE users ADD COLUMN alumno_id INTEGER",
        "ALTER TABLE users ADD COLUMN profesor_id INTEGER",
        "ALTER TABLE alumnos ADD COLUMN trabaja INTEGER DEFAULT 0"
    };

    public static void main(String[] args) {
        port(8080);

        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        try {
            Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());

            // Crear tablas que no existan (seguro de correr siempre)
            try (InputStream schemaStream = App.class.getResourceAsStream("/scheme.sql");
                 Scanner scanner = new Scanner(schemaStream, "UTF-8")) {
                String schemaSql = scanner.useDelimiter("\\A").next();
                for (String statement : schemaSql.split(";")) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        Base.exec(trimmed);
                    }
                }
            }

            // Migraciones: agrega columnas nuevas si no existen. Ignora el error si ya existen.
            for (String migration : MIGRATIONS) {
                try {
                    Base.exec(migration);
                } catch (Exception ignored) {
                    // La columna ya existe en esta base de datos
                }
            }

            // Si no hay ningún usuario, crea el admin por defecto
            if (User.count() == 0) {
                User admin = new User();
                admin.set("name", "admin");
                admin.set("password", BCrypt.hashpw("admin123", BCrypt.gensalt()));
                admin.set("role", "admin");
                admin.saveIt();
                System.out.println("==============================================");
                System.out.println(" Admin por defecto creado:");
                System.out.println("   Usuario:    admin");
                System.out.println("   Contraseña: admin123");
                System.out.println(" Cambiá la contraseña después del primer login.");
                System.out.println("==============================================");
            }

        } catch (Exception e) {
            System.err.println("Error inicializando la base de datos: " + e.getMessage());
        } finally {
            Base.close();
        }

        // ActiveJDBC necesita una conexión por hilo, se abre y cierra en cada request
        before((req, res) -> {
            if (!Base.hasConnection()) {
                Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
            }
        });

        afterAfter((req, res) -> {
            if (Base.hasConnection()) {
                Base.close();
            }
        });

        LoginApp.register();
        UserApp.register(objectMapper);
        ProfesorApp.register();
        MateriaApp.register();
        AlumnoApp.register();
    }
}
