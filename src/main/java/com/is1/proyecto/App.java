package com.is1.proyecto;

// Importaciones necesarias para la aplicación Spark
import com.fasterxml.jackson.databind.ObjectMapper;
import static spark.Spark.*;

// Importaciones para ActiveJDBC
import com.is1.proyecto.models.Teacher;
import org.javalite.activejdbc.Base;
import org.mindrot.jbcrypt.BCrypt;

// Importaciones de Spark
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

// Importaciones java
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder; 
import java.nio.charset.StandardCharsets; 

// Importaciones de clases
import com.is1.proyecto.config.DBConfigSingleton;
import com.is1.proyecto.models.User;


/**
 * Clase principal de la aplicación Spark.
 * Configura las rutas, filtros y el inicio del servidor web.
 */
public class App {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * inicia la aplicación. Aquí se configuran todas las rutas y filtros de Spark.
     */
    public static void main(String[] args) {
        port(8080);

        // Obtengo del singleton la configuración de la base de datos.
        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        // --- gestiona la conexión a la base de datos
        before((req, res) -> {
            if (req.pathInfo().contains(".")) {
                return;
            }
            try {
                // Abre una conexión a la base de datos utilizando las credenciales del singleton.
                Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
                System.out.println("DEBUG OPEN: " + req.url());
            } catch (Exception e) {
                System.err.println("Error al abrir conexión con ActiveJDBC: " + e.getMessage());
                // Permite que la conexión siga si ya está abierta por el mismo hilo (lo cual causa el error reportado)
                if (!e.getMessage().contains("existing connection is still on current thread")) {
                    halt(500, "{\"error\": \"Error interno del servidor: Fallo al conectar a la base de datos.\"}");
                }
            }
        });

        // cierra la conexión a la base de datos
        after((req, res) -> {
            
            if (req.pathInfo().contains(".")) {
                return;
            }
            try {
                // Cierra la conexión a la base de datos para liberar recursos.
                Base.close();
            } catch (Exception e) {
                System.err.println("Error al cerrar conexión con ActiveJDBC: " + e.getMessage());
            }
        });


        //Rutas GET para renderizar formularios y páginas HTML

        // GET: Muestra el formulario de creación de cuenta.
        get("/user/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                // Los mensajes ya llegan codificados por el POST, solo se pasan al modelo.
                model.put("successMessage", successMessage);
            }

            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine());

        // GET: Ruta para mostrar el dashboard (panel de control) del usuario.
        get("/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            String currentUsername = req.session().attribute("currentUserUsername");
            Boolean loggedIn = req.session().attribute("loggedIn");

            if (currentUsername == null || loggedIn == null || !loggedIn) {
                System.out.println("DEBUG: Acceso no autorizado a /dashboard. Redirigiendo a /login.");
                String encodedError = URLEncoder.encode("Debes iniciar sesión para acceder a esta página.", StandardCharsets.UTF_8.toString());
                res.redirect("/?error=" + encodedError);
                return null;
            }

            model.put("username", currentUsername);

            // 3. Renderiza la plantilla del dashboard con el nombre de usuario.
            return new ModelAndView(model, "dashboard.mustache");
        }, new MustacheTemplateEngine());

        // GET: Ruta para MOSTRAR el formulario de carga de docente
        get("/teacher/new", (req, res) -> {

            Map<String, Object> model = new HashMap<>();
            String successMessage = req.queryParams("message");
            String errorMessage = req.queryParams("error");

            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "teacher_from.mustache");

        }, new MustacheTemplateEngine());


        // POST: Procesa el formulario de carga de un nuevo docente
        post("/teacher/new", (req, res) -> {

            //Obtiene los datos del formulario HTML
            String name = req.queryParams("teacher_name");
            String lastName = req.queryParams("teacher_lastname");
            String dniStr = req.queryParams("teacher_dni");
            String phoneStr = req.queryParams("teacher_phone");
            String address = req.queryParams("teacher_address"); 

            //chequeo dd campos vacios o erroneos
            if (name == null || name.isEmpty() ||
                    lastName == null || lastName.isEmpty() ||
                    dniStr == null || dniStr.isEmpty()) {

                String encodedError = URLEncoder.encode("Los campos Nombre, Apellido y DNI son obligatorios.", StandardCharsets.UTF_8.toString());
                res.redirect("/teacher/new?error=" + encodedError);
                return "";
            }

            try {
                Long dni = Long.parseLong(dniStr); 
                Long phone = (phoneStr != null && !phoneStr.isEmpty()) ? Long.parseLong(phoneStr) : null; 

                //corrobora si el dni que estan por cargar existe
                Teacher existingTeacher = Teacher.findFirst("dni = ?", dni);
                if (existingTeacher != null) {
                    String encodedError = URLEncoder.encode("El DNI ingresado ya existe en el sistema.", StandardCharsets.UTF_8.toString());
                    res.redirect("/teacher/new?error=" + encodedError);
                    return "";
                }

                //Creación y Guardado
                Teacher teacher = new Teacher();
                teacher.set("name", name);
                teacher.set("lastName", lastName);
                teacher.set("dni", dni);
                teacher.set("phone", phone);
                teacher.set("address", address); 
                teacher.saveIt();
                res.status(201);
                String successMsg = "Profesor " + name + " " + lastName + " registrado correctamente.";
                String encodedSuccess = URLEncoder.encode(successMsg, StandardCharsets.UTF_8.toString());
                res.redirect("/teacher/new?message=" + encodedSuccess);
                return "";

            } catch (NumberFormatException nfe) {
                String encodedError = URLEncoder.encode("El DNI y el Teléfono deben ser números válidos.", StandardCharsets.UTF_8.toString());
                res.redirect("/teacher/new?error=" + encodedError);
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                String encodedError = URLEncoder.encode("Error interno al guardar el profesor.", StandardCharsets.UTF_8.toString());
                res.redirect("/teacher/new?error=" + encodedError);
                return "";
            }
        });


        // GET: Ruta para cerrar la sesión del usuario.
        get("/logout", (req, res) -> {
            req.session().invalidate();
            System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login.");
            res.redirect("/");
            return null;
        });

        // GET: Muestra el formulario de inicio de sesión (login).
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            return new ModelAndView(model, "login.mustache");
        }, new MustacheTemplateEngine());

        // GET: Ruta de alias para el formulario de creación de cuenta.
        get("/user/new", (req, res) -> {
            return new ModelAndView(new HashMap<>(), "user_form.mustache");
        }, new MustacheTemplateEngine());


        //Rutas POST para manejar envíos de formularios y APIs ---

        // POST: Maneja el envío del formulario de creación de nueva cuenta.
        post("/user/new", (req, res) -> {
            String name = req.queryParams("name");
            String password = req.queryParams("password");

            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400);
                String encodedError = URLEncoder.encode("Nombre y contraseña son requeridos.", StandardCharsets.UTF_8.toString());
                res.redirect("/user/create?error=" + encodedError);
                return "";
            }

            try {
                User ac = new User();
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                ac.set("name", name);
                ac.set("password", hashedPassword);
                ac.saveIt();

                res.status(201);
                String successMsg = "Cuenta creada exitosamente para " + name + "!";
                String encodedSuccess = URLEncoder.encode(successMsg, StandardCharsets.UTF_8.toString());
                res.redirect("/user/create?message=" + encodedSuccess);
                return "";

            } catch (Exception e) {
                System.err.println("Error al registrar la cuenta: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                String encodedError = URLEncoder.encode("Error interno al crear la cuenta. Intente de nuevo.", StandardCharsets.UTF_8.toString());
                res.redirect("/user/create?error=" + encodedError);
                return "";
            }
        });


        // POST: Maneja el envío del formulario de inicio de sesión.
        post("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            String username = req.queryParams("username");
            String plainTextPassword = req.queryParams("password");

            if (username == null || username.isEmpty() || plainTextPassword == null || plainTextPassword.isEmpty()) {
                res.status(400);
                model.put("errorMessage", "El nombre de usuario y la contraseña son requeridos.");
                return new ModelAndView(model, "login.mustache");
            }

            User ac = User.findFirst("name = ?", username);

            if (ac == null) {
                res.status(401);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                return new ModelAndView(model, "login.mustache");
            }

            String storedHashedPassword = ac.getString("password");

            if (BCrypt.checkpw(plainTextPassword, storedHashedPassword)) {
                res.status(200);

                //Gestión de Sesión
                req.session(true).attribute("currentUserUsername", username);
                req.session().attribute("userId", ac.getId());
                req.session().attribute("loggedIn", true);

                System.out.println("DEBUG: Login exitoso para la cuenta: " + username);
                System.out.println("DEBUG: ID de Sesión: " + req.session().id());


                model.put("username", username);
                return new ModelAndView(model, "dashboard.mustache");
            } else {
                res.status(401);
                System.out.println("DEBUG: Intento de login fallido para: " + username);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                return new ModelAndView(model, "login.mustache");
            }
        }, new MustacheTemplateEngine());


        // POST: Endpoint para añadir usuarios (API que devuelve JSON, no HTML).
        post("/add_users", (req, res) -> {
            res.type("application/json");

            String name = req.queryParams("name");
            String password = req.queryParams("password");

            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400);
                return objectMapper.writeValueAsString(Map.of("error", "Nombre y contraseña son requeridos."));
            }

            try {
                User newUser = new User();
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); 

                newUser.set("name", name);
                newUser.set("password", hashedPassword);
                newUser.saveIt();

                res.status(201);
                return objectMapper.writeValueAsString(Map.of("message", "Usuario '" + name + "' registrado con éxito.", "id", newUser.getId()));

            } catch (Exception e) {
                System.err.println("Error al registrar usuario: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return objectMapper.writeValueAsString(Map.of("error", "Error interno al registrar usuario: " + e.getMessage()));
            }
        });

        // El enlace funciona pero la funcionalidad aun no se implemento (para evitar el error 404): ej al seleccionar el boton perfil
        get("/profile", (req, res) -> {
            res.type("text/html");
            return "<h1>Página de Perfil (Falta Implementar)</h1><a href='/dashboard'>Volver</a>";
        });
        
        get("/settings", (req, res) -> {
            res.type("text/html");
            return "<h1>Página de Configuración (Falta Implementar)</h1><a href='/dashboard'>Volver</a>";
        });


    } 
} 