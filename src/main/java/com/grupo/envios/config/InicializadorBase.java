package com.grupo.envios.config;

import com.grupo.envios.util.NegocioException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public final class InicializadorBase {

    private static final List<String> SCRIPTS = List.of("1_tablas.sql", "2_indices.sql", "3_registros.sql");
    private static final Pattern NOMBRE_BASE = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");
    private static final String BASE_NO_EXISTE = "3D000";

    private InicializadorBase() {
    }

    public static String probar(DatosConexion dkprDatos) throws SQLException {
        validarNombre(dkprDatos.base());
        try (Connection dkprCon = conectar(dkprDatos, dkprDatos.base())) {
            return contarTablas(dkprCon) == 0
                    ? "Conexión correcta. La base \"" + dkprDatos.base() + "\" está vacía: se crearán las tablas al guardar."
                    : "Conexión correcta.";
        } catch (SQLException dkprE) {
            if (!BASE_NO_EXISTE.equals(dkprE.getSQLState())) {
                throw dkprE;
            }
        }
        try (Connection dkprCon = conectar(dkprDatos, "postgres")) {
            return "Conexión correcta. La base \"" + dkprDatos.base() + "\" no existe: se creará al guardar.";
        }
    }

    public static void preparar(DatosConexion dkprDatos) throws SQLException, IOException {
        validarNombre(dkprDatos.base());
        try (Connection dkprCon = conectar(dkprDatos, "postgres")) {
            if (!existeBase(dkprCon, dkprDatos.base())) {
                try (Statement dkprSt = dkprCon.createStatement()) {
                    dkprSt.execute("CREATE DATABASE \"" + dkprDatos.base() + "\" ENCODING 'UTF8'");
                }
            }
        }
        try (Connection dkprCon = conectar(dkprDatos, dkprDatos.base())) {
            if (contarTablas(dkprCon) == 0) {
                ejecutarScripts(dkprCon);
            }
        }
    }

    private static Connection conectar(DatosConexion dkprDatos, String dkprBase) throws SQLException {
        Properties dkprProps = new Properties();
        dkprProps.setProperty("user", dkprDatos.usuario());
        dkprProps.setProperty("password", dkprDatos.clave());
        dkprProps.setProperty("connectTimeout", "5");
        return DriverManager.getConnection(dkprDatos.url(dkprBase), dkprProps);
    }

    private static boolean existeBase(Connection dkprCon, String dkprBase) throws SQLException {
        try (PreparedStatement dkprPs = dkprCon.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
            dkprPs.setString(1, dkprBase);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next();
            }
        }
    }

    private static int contarTablas(Connection dkprCon) throws SQLException {
        String dkprSql = "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'";
        try (Statement dkprSt = dkprCon.createStatement(); ResultSet dkprRs = dkprSt.executeQuery(dkprSql)) {
            dkprRs.next();
            return dkprRs.getInt(1);
        }
    }

    private static void ejecutarScripts(Connection dkprCon) throws SQLException, IOException {
        dkprCon.setAutoCommit(false);
        try (Statement dkprSt = dkprCon.createStatement()) {
            for (String dkprScript : SCRIPTS) {
                dkprSt.execute(leerScript(dkprScript));
            }
            dkprCon.commit();
        } catch (SQLException | IOException dkprE) {
            dkprCon.rollback();
            throw dkprE;
        } finally {
            dkprCon.setAutoCommit(true);
        }
    }

    private static String leerScript(String dkprNombre) throws IOException {
        try (InputStream dkprIn = InicializadorBase.class.getResourceAsStream("/database/" + dkprNombre)) {
            if (dkprIn == null) {
                throw new IOException("No se encontró el script " + dkprNombre + " dentro del programa.");
            }
            return new String(dkprIn.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void validarNombre(String dkprBase) {
        if (dkprBase == null || !NOMBRE_BASE.matcher(dkprBase).matches()) {
            throw new NegocioException("El nombre de la base solo puede tener letras, números y guion bajo, "
                    + "y no puede empezar con un número.");
        }
    }
}
