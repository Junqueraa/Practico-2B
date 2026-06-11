package com.is1.proyecto;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import com.is1.proyecto.models.Profesor;
import com.is1.proyecto.models.Materia;
import com.is1.proyecto.models.Cargo;
import com.is1.proyecto.models.Alumno;
import com.is1.proyecto.models.Inscripcion;
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

    private static boolean isAdmin(spark.Request req) {
        Boolean loggedIn = req.session().attribute("loggedIn");
        String role = req.session().attribute("role");
        return loggedIn != null && loggedIn && "admin".equals(role);
    }

    public static void register() {

        // ── Panel del profesor ────────────────────────────────────────────────
        get("/profesor/panel", (req, res) -> {
            Boolean loggedIn = req.session().attribute("loggedIn");
            String role = req.session().attribute("role");
            if (loggedIn == null || !loggedIn || !"profesor".equals(role)) {
                res.redirect("/login?error=" + enc("Acceso no autorizado."));
                return null;
            }

            Integer profesorId = req.session().attribute("profesorId");
            Profesor profesor = Profesor.findById(profesorId);
            if (profesor == null) {
                res.redirect("/login?error=" + enc("Profesor no encontrado. Contacta al administrador."));
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("nombre", profesor.get("nombre_y_apellido"));

            Cargo cargo = Cargo.findFirst("profesor_id = ?", profesorId);
            if (cargo != null) {
                Materia materia = Materia.findById(cargo.get("materia_id"));
                if (materia != null) {
                    model.put("materia_nombre", materia.get("nombre_materia"));
                    model.put("tiene_materia", true);

                    List<Inscripcion> inscripciones = Inscripcion.where("materia_id = ?", materia.getId());
                    List<Map<String, Object>> alumnosMap = new ArrayList<>();
                    for (Inscripcion i : inscripciones) {
                        Alumno alumno = Alumno.findById(i.get("alumno_id"));
                        if (alumno != null) {
                            Map<String, Object> aMap = new HashMap<>(alumno.toMap());
                            Object trabVal = alumno.get("trabaja");
                            boolean trabaja = trabVal != null && ((Number) trabVal).intValue() == 1;
                            aMap.put("trabaja_bool", trabaja);
                            aMap.put("trabaja_str", trabaja ? "Sí" : "No");
                            alumnosMap.add(aMap);
                        }
                    }
                    model.put("alumnos_inscritos", alumnosMap);
                    model.put("tiene_alumnos", !alumnosMap.isEmpty());
                } else {
                    model.put("tiene_materia", false);
                }
            } else {
                model.put("tiene_materia", false);
            }

            return new ModelAndView(model, "profesor_panel.mustache");
        }, new MustacheTemplateEngine());

        // ── Rutas de administración (solo admin) ──────────────────────────────
        get("/profesores", (req, res) -> {
            if (!isAdmin(req)) {
                res.redirect("/login?error=" + enc("Acceso no autorizado."));
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) model.put("successMessage", successMessage);
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) model.put("errorMessage", errorMessage);

            List<Materia> listaMaterias = Materia.findAll();
            List<Map<String, Object>> materiasMap = new ArrayList<>();
            for (Materia m : listaMaterias) materiasMap.add(m.toMap());
            model.put("materias", materiasMap);

            List<Profesor> listaProfesores = Profesor.findAll();
            List<Map<String, Object>> profesoresMap = new ArrayList<>();
            for (Profesor p : listaProfesores) {
                Map<String, Object> pMap = new HashMap<>(p.toMap());
                Cargo cargo = Cargo.findFirst("profesor_id = ?", p.getId());
                if (cargo != null) {
                    Materia mat = Materia.findById(cargo.get("materia_id"));
                    pMap.put("materia_asignada", mat != null ? mat.get("nombre_materia") : "-");
                    pMap.put("cargo_tipo", cargo.get("cargo"));
                } else {
                    pMap.put("materia_asignada", "-");
                    pMap.put("cargo_tipo", "-");
                }
                profesoresMap.add(pMap);
            }
            model.put("profesores", profesoresMap);

            return new ModelAndView(model, "profesores.mustache");
        }, new MustacheTemplateEngine());

        post("/profesores", (req, res) -> {
            if (!isAdmin(req)) {
                res.redirect("/login?error=" + enc("Acceso no autorizado."));
                return null;
            }

            String dni = req.queryParams("dni");
            String nombre = req.queryParams("nombre_y_apellido");
            String telefono = req.queryParams("telefono");
            String correo = req.queryParams("correo");
            String materiaId = req.queryParams("materia_id");
            String cargoTipo = req.queryParams("cargo");
            String periodo = req.queryParams("periodo");

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

                if (materiaId != null && !materiaId.isEmpty()) {
                    Cargo cargo = new Cargo();
                    cargo.set("profesor_id", prof.getId());
                    cargo.set("materia_id", Integer.parseInt(materiaId));
                    cargo.set("cargo", cargoTipo != null ? cargoTipo : "Ayudante");
                    cargo.set("periodo", periodo != null ? periodo : "UnSemestre");
                    cargo.saveIt();
                }

                res.redirect("/profesores?message=" + enc("Profesor " + nombre + " registrado correctamente."));
            } catch (Exception e) {
                res.redirect("/profesores?error=" + enc("Error interno al guardar. Revisa la consola."));
            }
            return null;
        });
    }
}
