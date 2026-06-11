package com.is1.proyecto;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import com.is1.proyecto.models.User;
import com.is1.proyecto.models.Alumno;
import com.is1.proyecto.models.Profesor;
import org.mindrot.jbcrypt.BCrypt;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class LoginApp {

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return s;
        }
    }

    public static void register() {

        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) model.put("errorMessage", errorMessage);
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) model.put("successMessage", successMessage);
            return new ModelAndView(model, "login.mustache");
        }, new MustacheTemplateEngine());

        get("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) model.put("errorMessage", errorMessage);
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) model.put("successMessage", successMessage);
            return new ModelAndView(model, "login.mustache");
        }, new MustacheTemplateEngine());

        post("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String username = req.queryParams("username");
            String plainTextPassword = req.queryParams("password");

            if (username == null || username.isEmpty() || plainTextPassword == null || plainTextPassword.isEmpty()) {
                model.put("errorMessage", "El nombre de usuario y la contraseña son requeridos.");
                return new ModelAndView(model, "login.mustache");
            }

            User ac = User.findFirst("name = ?", username);
            if (ac == null) {
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                return new ModelAndView(model, "login.mustache");
            }

            if (!BCrypt.checkpw(plainTextPassword, ac.getString("password"))) {
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                return new ModelAndView(model, "login.mustache");
            }

            String role = ac.getString("role");
            if (role == null) role = "admin";

            req.session(true).attribute("currentUserUsername", username);
            req.session().attribute("userId", ac.getId());
            req.session().attribute("loggedIn", true);
            req.session().attribute("role", role);

            if ("alumno".equals(role)) {
                Object alumnoId = ac.get("alumno_id");
                if (alumnoId == null) {
                    model.put("errorMessage", "Tu cuenta de alumno no está vinculada a ningún registro. Contacta al administrador.");
                    return new ModelAndView(model, "login.mustache");
                }
                req.session().attribute("alumnoId", ((Number) alumnoId).intValue());
                res.redirect("/alumno/panel");
            } else if ("profesor".equals(role)) {
                Object profesorId = ac.get("profesor_id");
                if (profesorId == null) {
                    model.put("errorMessage", "Tu cuenta de profesor no está vinculada a ningún registro. Contacta al administrador.");
                    return new ModelAndView(model, "login.mustache");
                }
                req.session().attribute("profesorId", ((Number) profesorId).intValue());
                res.redirect("/profesor/panel");
            } else {
                res.redirect("/dashboard");
            }
            return null;
        }, new MustacheTemplateEngine());

        get("/perfil", (req, res) -> {
            String currentUsername = req.session().attribute("currentUserUsername");
            Boolean loggedIn = req.session().attribute("loggedIn");
            if (currentUsername == null || loggedIn == null || !loggedIn) {
                res.redirect("/login?error=" + enc("Debes iniciar sesión primero."));
                return null;
            }

            String role = req.session().attribute("role");
            if (role == null) role = "admin";

            Map<String, Object> model = new HashMap<>();
            model.put("username", currentUsername);
            model.put("role", role);

            // Etiqueta legible del rol
            if ("alumno".equals(role)) model.put("rolLabel", "Alumno");
            else if ("profesor".equals(role)) model.put("rolLabel", "Profesor");
            else model.put("rolLabel", "Administrador");

            // Datos del registro vinculado
            if ("alumno".equals(role)) {
                Integer alumnoId = req.session().attribute("alumnoId");
                if (alumnoId != null) {
                    Alumno alumno = Alumno.findById(alumnoId);
                    if (alumno != null) {
                        model.put("nombre", alumno.get("nombre_y_apellido"));
                        model.put("dni", alumno.get("dni"));
                        model.put("telefono", alumno.get("telefono"));
                        model.put("correo", alumno.get("correo"));
                        model.put("anio_ingreso", alumno.get("anio_ingreso_u"));
                        Object trabVal = alumno.get("trabaja");
                        model.put("trabaja_str", trabVal != null && ((Number) trabVal).intValue() == 1 ? "Sí" : "No");
                        model.put("esAlumno", true);
                    }
                }
            } else if ("profesor".equals(role)) {
                Integer profesorId = req.session().attribute("profesorId");
                if (profesorId != null) {
                    Profesor profesor = Profesor.findById(profesorId);
                    if (profesor != null) {
                        model.put("nombre", profesor.get("nombre_y_apellido"));
                        model.put("dni", profesor.get("dni"));
                        model.put("telefono", profesor.get("telefono"));
                        model.put("correo", profesor.get("correo"));
                        model.put("esProfesor", true);
                    }
                }
            }

            // Mensajes de feedback del cambio de contraseña
            String successMsg = req.queryParams("message");
            if (successMsg != null && !successMsg.isEmpty()) model.put("successMessage", successMsg);
            String errorMsg = req.queryParams("error");
            if (errorMsg != null && !errorMsg.isEmpty()) model.put("errorMessage", errorMsg);

            // Volver según rol
            if ("alumno".equals(role)) model.put("backUrl", "/alumno/panel");
            else if ("profesor".equals(role)) model.put("backUrl", "/profesor/panel");
            else model.put("backUrl", "/dashboard");

            return new ModelAndView(model, "perfil.mustache");
        }, new MustacheTemplateEngine());

        post("/perfil/password", (req, res) -> {
            String currentUsername = req.session().attribute("currentUserUsername");
            Boolean loggedIn = req.session().attribute("loggedIn");
            if (currentUsername == null || loggedIn == null || !loggedIn) {
                res.redirect("/login?error=" + enc("Debes iniciar sesión primero."));
                return null;
            }

            String currentPass = req.queryParams("current_password");
            String newPass = req.queryParams("new_password");
            String confirmPass = req.queryParams("confirm_password");

            if (currentPass == null || newPass == null || confirmPass == null ||
                currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                res.redirect("/perfil?error=" + enc("Todos los campos son obligatorios."));
                return null;
            }

            if (!newPass.equals(confirmPass)) {
                res.redirect("/perfil?error=" + enc("La nueva contraseña y la confirmación no coinciden."));
                return null;
            }

            if (newPass.length() < 6) {
                res.redirect("/perfil?error=" + enc("La nueva contraseña debe tener al menos 6 caracteres."));
                return null;
            }

            User user = User.findFirst("name = ?", currentUsername);
            if (user == null || !BCrypt.checkpw(currentPass, user.getString("password"))) {
                res.redirect("/perfil?error=" + enc("La contraseña actual es incorrecta."));
                return null;
            }

            user.set("password", BCrypt.hashpw(newPass, BCrypt.gensalt()));
            user.saveIt();
            res.redirect("/perfil?message=" + enc("Contraseña actualizada correctamente."));
            return null;
        });

        get("/logout", (req, res) -> {
            req.session().invalidate();
            res.redirect("/");
            return null;
        });

        get("/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String currentUsername = req.session().attribute("currentUserUsername");
            Boolean loggedIn = req.session().attribute("loggedIn");
            String role = req.session().attribute("role");

            if (currentUsername == null || loggedIn == null || !loggedIn) {
                res.redirect("/login?error=" + enc("Debes iniciar sesión para acceder a esta página."));
                return null;
            }

            if ("alumno".equals(role)) {
                res.redirect("/alumno/panel");
                return null;
            }
            if ("profesor".equals(role)) {
                res.redirect("/profesor/panel");
                return null;
            }

            model.put("username", currentUsername);
            return new ModelAndView(model, "dashboard.mustache");
        }, new MustacheTemplateEngine());
    }
}
