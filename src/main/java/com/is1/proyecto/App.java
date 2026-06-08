package com.is1.proyecto;

import com.fasterxml.jackson.databind.ObjectMapper;
import static spark.Spark.*;

import org.javalite.activejdbc.Base;
import com.is1.proyecto.config.DBConfigSingleton;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class App {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        port(8080);

        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        // Ejecuta el schema al inicio para crear las tablas si no existen
        try {
            Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
            String schemaSql = new String(Files.readAllBytes(Paths.get("src/main/resources/scheme.sql")));
            Base.exec(schemaSql);
        } catch (IOException e) {
            System.err.println("Error leyendo scheme.sql: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error ejecutando scheme.sql: " + e.getMessage());
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
