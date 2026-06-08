package com.is1.proyecto.config;

import org.javalite.activejdbc.Base;

// Una sola instancia de config para toda la app
public final class DBConfigSingleton {

    private static DBConfigSingleton instance;

    private final String dbUrl;
    private final String user;
    private final String pass;
    private final String driver;

    private DBConfigSingleton() {
        this.driver = "org.sqlite.JDBC";
        // Permite sobreescribir la URL por parámetro al correr (-Ddb.url=...)
        this.dbUrl = System.getProperty("db.url", "jdbc:sqlite:./db/dev.db");
        this.user = "";
        this.pass = "";
    }

    public static synchronized DBConfigSingleton getInstance() {
        if (instance == null) {
            instance = new DBConfigSingleton();
        }
        return instance;
    }

    public void openConnection() {
        Base.open(this.driver, this.dbUrl, this.user, this.pass);
    }

    public void closeConnection() {
        Base.close();
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public String getDriver() {
        return driver;
    }
}
