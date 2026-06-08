package com.is1.proyecto;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.is1.proyecto.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class UserApp {

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return s;
        }
    }

    public static void register(ObjectMapper objectMapper) {

        get("/user/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine());

        get("/user/new", (req, res) -> {
            return new ModelAndView(new HashMap<>(), "user_form.mustache");
        }, new MustacheTemplateEngine());

        post("/user/new", (req, res) -> {
            String name = req.queryParams("name");
            String password = req.queryParams("password");

            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.redirect("/user/create?error=" + enc("Nombre y contraseña son requeridos."));
                return "";
            }

            try {
                User ac = new User();
                ac.set("name", name);
                ac.set("password", BCrypt.hashpw(password, BCrypt.gensalt()));
                ac.saveIt();
                res.redirect("/login?message=" + enc("Cuenta creada exitosamente. Ya podés iniciar sesión."));
                return "";
            } catch (Exception e) {
                res.redirect("/user/create?error=" + enc("Error interno al crear la cuenta. Intente de nuevo."));
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
