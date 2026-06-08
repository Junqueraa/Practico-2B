package com.is1.proyecto;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import com.is1.proyecto.models.Alumno;
import com.is1.proyecto.models.Beca;
import com.is1.proyecto.models.Materia;
import com.is1.proyecto.models.Rendimiento;
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

                List<Rendimiento> rends = Rendimiento.where("alumno_id = ?", a.getId());
                if (!rends.isEmpty()) {
                    double avgNota = rends.stream()
                        .mapToDouble(r -> r.get("nota") != null ? ((Number) r.get("nota")).doubleValue() : 0)
                        .average().orElse(0);
                    double avgAsist = rends.stream()
                        .mapToDouble(r -> r.get("asistencia") != null ? ((Number) r.get("asistencia")).doubleValue() : 0)
                        .average().orElse(0);
                    alumnoData.put("promedio_nota", String.format("%.1f", avgNota));
                    alumnoData.put("promedio_asistencia", String.format("%.0f%%", avgAsist));
                } else {
                    alumnoData.put("promedio_nota", "-");
                    alumnoData.put("promedio_asistencia", "-");
                }

                alumnosMap.add(alumnoData);
            }
            model.put("alumnos", alumnosMap);

            List<Materia> listaMaterias = Materia.findAll();
            List<Map<String, Object>> materiasMap = new ArrayList<>();
            for (Materia m : listaMaterias) {
                materiasMap.add(m.toMap());
            }
            model.put("materias", materiasMap);

            return new ModelAndView(model, "alumnos.mustache");
        }, new MustacheTemplateEngine());

        post("/alumnos", (req, res) -> {
            String dni = req.queryParams("dni");
            String nombre = req.queryParams("nombre_y_apellido");
            String telefono = req.queryParams("telefono");
            String correo = req.queryParams("correo");
            String anioIngreso = req.queryParams("anio_ingreso_u");

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
                res.redirect("/alumnos?message=" + enc("Alumno " + nombre + " registrado correctamente."));
            } catch (Exception e) {
                res.redirect("/alumnos?error=" + enc("Error interno al guardar: " + e.getMessage()));
            }
            return null;
        });

        post("/rendimientos", (req, res) -> {
            String alumnoId = req.queryParams("alumno_id");
            String materiaId = req.queryParams("materia_id");
            String nota = req.queryParams("nota");
            String asistencia = req.queryParams("asistencia");

            if (alumnoId == null || materiaId == null || nota == null || asistencia == null ||
                alumnoId.isEmpty() || materiaId.isEmpty() || nota.isEmpty() || asistencia.isEmpty()) {
                res.redirect("/alumnos?error=" + enc("Todos los campos del rendimiento son obligatorios."));
                return null;
            }

            try {
                Rendimiento rend = new Rendimiento();
                rend.set("alumno_id", Integer.parseInt(alumnoId));
                rend.set("materia_id", Integer.parseInt(materiaId));
                rend.set("nota", Double.parseDouble(nota));
                rend.set("asistencia", Integer.parseInt(asistencia));
                rend.saveIt();
                res.redirect("/alumnos?message=" + enc("Rendimiento registrado correctamente."));
            } catch (Exception e) {
                res.redirect("/alumnos?error=" + enc("Error al guardar rendimiento: " + e.getMessage()));
            }
            return null;
        });

        // Asigna beca al alumno si cumple las 3 condiciones
        post("/alumnos/:id/beca", (req, res) -> {
            int alumnoId = Integer.parseInt(req.params("id"));
            String trabaja = req.queryParams("trabaja");

            if (!"on".equals(trabaja)) {
                res.redirect("/alumnos?error=" + enc("El alumno debe trabajar para obtener la beca."));
                return null;
            }

            List<Rendimiento> rends = Rendimiento.where("alumno_id = ?", alumnoId);
            if (rends.isEmpty()) {
                res.redirect("/alumnos?error=" + enc("El alumno no tiene rendimientos registrados."));
                return null;
            }

            double avgNota = rends.stream()
                .mapToDouble(r -> r.get("nota") != null ? ((Number) r.get("nota")).doubleValue() : 0)
                .average().orElse(0);
            double avgAsist = rends.stream()
                .mapToDouble(r -> r.get("asistencia") != null ? ((Number) r.get("asistencia")).doubleValue() : 0)
                .average().orElse(100);

            if (avgNota <= 8) {
                res.redirect("/alumnos?error=" + enc("El promedio de notas debe ser mayor a 8. Promedio actual: " + String.format("%.1f", avgNota)));
                return null;
            }

            if (avgAsist >= 30) {
                res.redirect("/alumnos?error=" + enc("La asistencia promedio debe ser menor al 30%. Promedio actual: " + String.format("%.0f%%", avgAsist)));
                return null;
            }

            Beca beca = new Beca();
            beca.set("alumno_id", alumnoId);
            beca.set("trabaja", true);
            beca.saveIt();

            res.redirect("/alumnos?message=" + enc("Beca asignada correctamente."));
            return null;
        });
    }
}
