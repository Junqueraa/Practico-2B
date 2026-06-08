package com.is1.proyecto;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import com.is1.proyecto.models.Alumno;
import com.is1.proyecto.models.Beca;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlumnoApp {

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return s;
        }
    }

    public static void register() {

        get("/alumnos", (req, res) -> {
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

            List<Alumno> listaAlumnos = Alumno.findAll();
            List<Map<String, Object>> alumnosMap = new ArrayList<>();
            for (Alumno a : listaAlumnos) {
                Map<String, Object> alumnoData = new HashMap<>(a.toMap());
                Beca beca = Beca.findFirst("alumno_id = ?", a.getId());
                alumnoData.put("tiene_beca", beca != null);
                alumnoData.put("trabaja", beca != null && Boolean.TRUE.equals(beca.get("trabaja")));
                alumnosMap.add(alumnoData);
            }
            model.put("alumnos", alumnosMap);

            return new ModelAndView(model, "alumnos.mustache");
        }, new MustacheTemplateEngine());

        post("/alumnos", (req, res) -> {
            String dni = req.queryParams("dni");
            String nombre = req.queryParams("nombre_y_apellido");
            String telefono = req.queryParams("telefono");
            String correo = req.queryParams("correo");
            String anioIngreso = req.queryParams("anio_ingreso_u");
            String tieneBeca = req.queryParams("tiene_beca");
            String trabaja = req.queryParams("trabaja");

            if (dni == null || dni.isEmpty() || nombre == null || nombre.isEmpty()) {
                res.redirect("/alumnos?error=" + enc("El DNI y el Nombre son obligatorios."));
                return null;
            }

            try {
                Alumno alumno = new Alumno();
                alumno.set("dni", dni);
                alumno.set("nombre_y_apellido", nombre);
                alumno.set("telefono", telefono);
                alumno.set("correo", correo);
                if (anioIngreso != null && !anioIngreso.isEmpty()) {
                    alumno.set("anio_ingreso_u", Integer.parseInt(anioIngreso));
                }
                alumno.saveIt();

                if ("on".equals(tieneBeca)) {
                    Beca beca = new Beca();
                    beca.set("alumno_id", alumno.getId());
                    beca.set("trabaja", "on".equals(trabaja));
                    beca.saveIt();
                }

                res.redirect("/alumnos?message=" + enc("Alumno " + nombre + " registrado correctamente."));
            } catch (Exception e) {
                res.redirect("/alumnos?error=" + enc("Error interno al guardar: " + e.getMessage()));
            }
            return null;
        });
    }
}
