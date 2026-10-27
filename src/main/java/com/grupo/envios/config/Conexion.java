package com.grupo.envios.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Conexion {

    private Conexion() {
    }

    public static Connection obtener() throws SQLException {
        DatosConexion dkprDatos = ConfiguracionConexion.actual();
        return DriverManager.getConnection(dkprDatos.url(), dkprDatos.usuario(), dkprDatos.clave());
    }
}
