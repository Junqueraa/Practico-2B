package com.is1.proyecto;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import com.is1.proyecto.models.Profesor;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfesorApp {

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return s;
        }
    }

    public static void register() {

        get("/profesores", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            Boolean loggedIn = req.session().attribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                res.redirect("/login?error=" + enc("Debes iniciar sesión primero."));
                return null;
            }

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            List<Profesor> listaProfesores = Profesor.findAll();
            List<Map<String, Object>> profesoresMap = new ArrayList<>();
            for (Profesor p : listaProfesores) {
                profesoresMap.add(p.toMap());
            }
            model.put("profesores", profesoresMap);

            return new ModelAndView(model, "profesores.mustache");
        }, new MustacheTemplateEngine());

        post("/profesores", (req, res) -> {
            String dni = req.queryParams("dni");
            String nombre = req.queryParams("nombre_y_apellido");
            String telefono = req.queryParams("telefono");
            String correo = req.queryParams("correo");

            if (dni == null || dni.isEmpty() || nombre == null || nombre.isEmpty()) {
                res.redirect("/profesores?error=" + enc("El DNI y el Nombre son obligatorios."));
                return null;
            }

            try {
                Profesor prof = new Profesor();
                prof.set("dni", dni);
                prof.set("nombre_y_apellido", nombre);
                prof.set("telefono", telefono);
                prof.set("correo", correo);
                prof.saveIt();
                res.redirect("/profesores?message=" + enc("Profesor " + nombre + " registrado correctamente."));
            } catch (Exception e) {
                res.redirect("/profesores?error=" + enc("Error interno al guardar. Revisa la consola."));
            }
            return null;
        });
    }
}
