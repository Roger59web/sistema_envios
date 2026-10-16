package com.grupo.envios.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Entrega conexiones a PostgreSQL leyendo los datos de db.properties
 * (archivo en la raíz del proyecto, no se sube a GitHub).
 */
public final class Conexion {

    private static final Properties props = new Properties();

    private Conexion() {
    }

    private static synchronized void cargarConfiguracion() {
        if (!props.isEmpty()) {
            return;
        }
        try (InputStream in = new FileInputStream("db.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se encontró db.properties en la raíz del proyecto. "
                            + "Copia db.properties.example como db.properties y ajusta tus datos.", e);
        }
    }

    public static Connection obtener() throws SQLException {
        cargarConfiguracion();
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password"));
    }
}
