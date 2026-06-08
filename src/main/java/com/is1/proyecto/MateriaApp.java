package com.is1.proyecto;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import com.is1.proyecto.models.Materia;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MateriaApp {

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return s;
        }
    }

    public static void register() {

        get("/materias", (req, res) -> {
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

            List<Materia> listaMaterias = Materia.findAll();
            List<Map<String, Object>> materiasMap = new ArrayList<>();
            for (Materia m : listaMaterias) {
                materiasMap.add(m.toMap());
            }
            model.put("materias", materiasMap);

            return new ModelAndView(model, "materias.mustache");
        }, new MustacheTemplateEngine());

        post("/materias", (req, res) -> {
            String codigo = req.queryParams("cod_materia");
            String nombre = req.queryParams("nombre_materia");

            if (codigo == null || codigo.isEmpty() || nombre == null || nombre.isEmpty()) {
                res.redirect("/materias?error=" + enc("Todos los campos son obligatorios."));
                return null;
            }

            try {
                Materia mat = new Materia();
                mat.set("cod_materia", codigo);
                mat.set("nombre_materia", nombre);
                mat.saveIt();
                res.redirect("/materias?message=" + enc("Materia '" + nombre + "' registrada correctamente."));
            } catch (Exception e) {
                res.redirect("/materias?error=" + enc("Error interno al guardar: " + e.getMessage()));
            }
            return null;
        });
    }
}
