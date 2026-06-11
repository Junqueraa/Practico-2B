package com.is1.proyecto;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.is1.proyecto.models.User;
import com.is1.proyecto.models.Alumno;
import com.is1.proyecto.models.Profesor;
import org.mindrot.jbcrypt.BCrypt;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserApp {

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return s;
        }
    }

    private static Map<String, Object> buildFormModel(String successMsg, String errorMsg) {
        Map<String, Object> model = new HashMap<>();
        if (successMsg != null && !successMsg.isEmpty()) model.put("successMessage", successMsg);
        if (errorMsg != null && !errorMsg.isEmpty()) model.put("errorMessage", errorMsg);

        List<Alumno> alumnos = Alumno.findAll();
        List<Map<String, Object>> alumnosMap = new ArrayList<>();
        for (Alumno a : alumnos) alumnosMap.add(a.toMap());
        model.put("alumnos", alumnosMap);

        List<Profesor> profesores = Profesor.findAll();
        List<Map<String, Object>> profesoresMap = new ArrayList<>();
        for (Profesor p : profesores) profesoresMap.add(p.toMap());
        model.put("profesores", profesoresMap);

        return model;
    }

    public static void register(ObjectMapper objectMapper) {

        get("/user/create", (req, res) -> {
            Boolean loggedIn = req.session().attribute("loggedIn");
            String role = req.session().attribute("role");
            if (loggedIn == null || !loggedIn || !"admin".equals(role)) {
                res.redirect("/login?error=" + enc("Solo el administrador puede crear usuarios."));
                return null;
            }
            Map<String, Object> model = buildFormModel(req.queryParams("message"), req.queryParams("error"));
            model.put("adminForm", true);
            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine());

        get("/user/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String errorMsg = req.queryParams("error");
            if (errorMsg != null && !errorMsg.isEmpty()) model.put("errorMessage", errorMsg);
            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine());

        post("/user/new", (req, res) -> {
            String name = req.queryParams("name");
            String password = req.queryParams("password");
            String roleParam = req.queryParams("role");
            String alumnoIdParam = req.queryParams("alumno_id");
            String profesorIdParam = req.queryParams("profesor_id");

            boolean esAdmin = "admin".equals(req.session().attribute("role"));
            String urlError   = esAdmin ? "/user/create?error="   : "/user/new?error=";
            String urlSuccess = esAdmin ? "/user/create?message=" : "/login?message=";

            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.redirect(urlError + enc("Nombre y contraseña son requeridos."));
                return "";
            }

            String role = (roleParam != null && !roleParam.isEmpty()) ? roleParam : "admin";

            try {
                User ac = new User();
                ac.set("name", name);
                ac.set("password", BCrypt.hashpw(password, BCrypt.gensalt()));
                ac.set("role", role);

                if ("alumno".equals(role) && alumnoIdParam != null && !alumnoIdParam.isEmpty()) {
                    ac.set("alumno_id", Integer.parseInt(alumnoIdParam));
                } else if ("profesor".equals(role) && profesorIdParam != null && !profesorIdParam.isEmpty()) {
                    ac.set("profesor_id", Integer.parseInt(profesorIdParam));
                }

                ac.saveIt();
                res.redirect(urlSuccess + enc("Usuario '" + name + "' creado correctamente."));
                return "";
            } catch (Exception e) {
                String msg = e.getMessage() != null && e.getMessage().contains("UNIQUE")
                    ? "Ya existe un usuario con ese nombre. Elegí otro."
                    : "Error interno al crear la cuenta. Intentá de nuevo.";
                res.redirect(urlError + enc(msg));
                return "";
            }
        });

        post("/add_users", (req, res) -> {
            res.type("application/json");
            String name = req.queryParams("name");
            String password = req.queryParams("password");

            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400);
                Map<String, Object> errorRes = new HashMap<>();
                errorRes.put("error", "Nombre y contraseña son requeridos.");
                return objectMapper.writeValueAsString(errorRes);
            }

            try {
                User newUser = new User();
                newUser.set("name", name);
                newUser.set("password", password);
                newUser.set("role", "admin");
                newUser.saveIt();
                res.status(201);
                Map<String, Object> successRes = new HashMap<>();
                successRes.put("message", "Usuario '" + name + "' registrado con éxito.");
                successRes.put("id", newUser.getId());
                return objectMapper.writeValueAsString(successRes);
            } catch (Exception e) {
                res.status(500);
                Map<String, Object> errorRes = new HashMap<>();
                errorRes.put("error", "Error interno al registrar usuario: " + e.getMessage());
                return objectMapper.writeValueAsString(errorRes);
            }
        });
    }
}
