package com.grupo.envios.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DkprConexion {

    private DkprConexion() {
    }

    public static Connection dkprObtener() throws SQLException {
        DkprDatosConexion dkprDatos = DkprConfiguracionConexion.dkprActual();
        return DriverManager.getConnection(dkprDatos.dkprUrl(), dkprDatos.dkprUsuario(), dkprDatos.dkprClave());
    }
}
