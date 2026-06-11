package com.is1.proyecto;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import com.is1.proyecto.models.Alumno;
import com.is1.proyecto.models.Beca;
import com.is1.proyecto.models.Materia;
import com.is1.proyecto.models.Rendimiento;
import com.is1.proyecto.models.Inscripcion;
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

    private static boolean isAdmin(spark.Request req) {
        Boolean loggedIn = req.session().attribute("loggedIn");
        String role = req.session().attribute("role");
        return loggedIn != null && loggedIn && "admin".equals(role);
    }

    public static void register() {

        // ── Panel del alumno ──────────────────────────────────────────────────
        get("/alumno/panel", (req, res) -> {
            Boolean loggedIn = req.session().attribute("loggedIn");
            String role = req.session().attribute("role");
            if (loggedIn == null || !loggedIn || !"alumno".equals(role)) {
                res.redirect("/login?error=" + enc("Acceso no autorizado."));
                return null;
            }

            Integer alumnoId = req.session().attribute("alumnoId");
            Alumno alumno = Alumno.findById(alumnoId);
            if (alumno == null) {
                res.redirect("/login?error=" + enc("Alumno no encontrado. Contacta al administrador."));
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("nombre", alumno.get("nombre_y_apellido"));

            Object trabajaVal = alumno.get("trabaja");
            boolean trabaja = trabajaVal != null && ((Number) trabajaVal).intValue() == 1;
            model.put("trabaja", trabaja);

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) model.put("successMessage", successMessage);
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) model.put("errorMessage", errorMessage);

            // Materias en las que ya está inscripto
            List<Inscripcion> inscripciones = Inscripcion.where("alumno_id = ?", alumnoId);
            List<Map<String, Object>> inscritasMap = new ArrayList<>();
            List<Integer> inscritasIds = new ArrayList<>();
            for (Inscripcion i : inscripciones) {
                int mid = ((Number) i.get("materia_id")).intValue();
                inscritasIds.add(mid);
                Materia m = Materia.findById(mid);
                if (m != null) inscritasMap.add(m.toMap());
            }
            model.put("materias_inscritas", inscritasMap);
            model.put("tiene_inscripciones", !inscritasMap.isEmpty());

            // Materias disponibles para inscribirse
            List<Materia> todasMaterias = Materia.findAll();
            List<Map<String, Object>> disponiblesMap = new ArrayList<>();
            for (Materia m : todasMaterias) {
                int mid = ((Number) m.getId()).intValue();
                if (!inscritasIds.contains(mid)) disponiblesMap.add(m.toMap());
            }
            model.put("materias_disponibles", disponiblesMap);
            model.put("hay_disponibles", !disponiblesMap.isEmpty());

            return new ModelAndView(model, "alumno_panel.mustache");
        }, new MustacheTemplateEngine());

        // Inscribirse a una materia
        post("/alumno/inscribir", (req, res) -> {
            Boolean loggedIn = req.session().attribute("loggedIn");
            String role = req.session().attribute("role");
            if (loggedIn == null || !loggedIn || !"alumno".equals(role)) {
                res.redirect("/login?error=" + enc("Acceso no autorizado."));
                return null;
            }

            Integer alumnoId = req.session().attribute("alumnoId");
            String materiaIdStr = req.queryParams("materia_id");

            if (materiaIdStr == null || materiaIdStr.isEmpty()) {
                res.redirect("/alumno/panel?error=" + enc("Seleccioná una materia."));
                return null;
            }

            int materiaId = Integer.parseInt(materiaIdStr);
            Inscripcion existing = Inscripcion.findFirst("alumno_id = ? AND materia_id = ?", alumnoId, materiaId);
            if (existing != null) {
                res.redirect("/alumno/panel?error=" + enc("Ya estás inscripto en esa materia."));
                return null;
            }

            Inscripcion ins = new Inscripcion();
            ins.set("alumno_id", alumnoId);
            ins.set("materia_id", materiaId);
            ins.saveIt();

            res.redirect("/alumno/panel?message=" + enc("Inscripción realizada correctamente."));
            return null;
        });

        // Actualizar estado de trabajo
        post("/alumno/trabaja", (req, res) -> {
            Boolean loggedIn = req.session().attribute("loggedIn");
            String role = req.session().attribute("role");
            if (loggedIn == null || !loggedIn || !"alumno".equals(role)) {
                res.redirect("/login?error=" + enc("Acceso no autorizado."));
                return null;
            }

            Integer alumnoId = req.session().attribute("alumnoId");
            String trabajaParam = req.queryParams("trabaja");
            int trabajaVal = "on".equals(trabajaParam) ? 1 : 0;

            Alumno alumno = Alumno.findById(alumnoId);
            alumno.set("trabaja", trabajaVal);
            alumno.saveIt();

            res.redirect("/alumno/panel?message=" + enc("Estado de trabajo actualizado."));
            return null;
        });

        // ── Rutas de administración (solo admin) ──────────────────────────────
        get("/alumnos", (req, res) -> {
            if (!isAdmin(req)) {
                res.redirect("/login?error=" + enc("Acceso no autorizado."));
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) model.put("successMessage", successMessage);
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) model.put("errorMessage", errorMessage);

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
            for (Materia m : listaMaterias) materiasMap.add(m.toMap());
            model.put("materias", materiasMap);

            return new ModelAndView(model, "alumnos.mustache");
        }, new MustacheTemplateEngine());

        post("/alumnos", (req, res) -> {
            if (!isAdmin(req)) {
                res.redirect("/login?error=" + enc("Acceso no autorizado."));
                return null;
            }

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
            if (!isAdmin(req)) {
                res.redirect("/login?error=" + enc("Acceso no autorizado."));
                return null;
            }

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

        post("/alumnos/:id/beca", (req, res) -> {
            if (!isAdmin(req)) {
                res.redirect("/login?error=" + enc("Acceso no autorizado."));
                return null;
            }

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
