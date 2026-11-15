package com.grupo.envios.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class DkprConfiguracionConexion {

    private static final Path ARCHIVO_DESARROLLO = Path.of("db.properties");

    private static DkprDatosConexion dkprActual;

    private DkprConfiguracionConexion() {
    }

    public static Path dkprArchivoUsuario() {
        String dkprAppData = System.getenv("APPDATA");
        Path dkprBase = dkprAppData != null ? Path.of(dkprAppData) : Path.of(System.getProperty("user.home"));
        return dkprBase.resolve("SistemaEnvios").resolve("db.properties");
    }

    public static Path dkprArchivoEnUso() {
        return Files.exists(ARCHIVO_DESARROLLO) ? ARCHIVO_DESARROLLO : dkprArchivoUsuario();
    }

    public static synchronized boolean dkprExiste() {
        return dkprActual != null || Files.exists(dkprArchivoEnUso());
    }

    public static synchronized DkprDatosConexion dkprActual() {
        if (dkprActual == null) {
            dkprActual = dkprLeer(dkprArchivoEnUso());
        }
        return dkprActual;
    }

    public static synchronized DkprDatosConexion dkprLeerSiExiste() {
        return dkprExiste() ? dkprActual() : null;
    }

    public static synchronized void dkprUsar(DkprDatosConexion dkprDatos) {
        dkprActual = dkprDatos;
    }

    public static synchronized void dkprGuardar(DkprDatosConexion dkprDatos) throws IOException {
        Path dkprArchivo = dkprArchivoEnUso();
        if (dkprArchivo.getParent() != null) {
            Files.createDirectories(dkprArchivo.getParent());
        }
        Properties dkprProps = new Properties();
        dkprProps.setProperty("db.url", dkprDatos.dkprUrl());
        dkprProps.setProperty("db.user", dkprDatos.dkprUsuario());
        dkprProps.setProperty("db.password", dkprDatos.dkprClave());
        try (OutputStream dkprOut = Files.newOutputStream(dkprArchivo)) {
            dkprProps.store(dkprOut, null);
        }
        dkprActual = dkprDatos;
    }

    private static DkprDatosConexion dkprLeer(Path dkprArchivo) {
        Properties dkprProps = new Properties();
        try (InputStream dkprIn = Files.newInputStream(dkprArchivo)) {
            dkprProps.load(dkprIn);
        } catch (IOException dkprE) {
            throw new IllegalStateException("No se encontró la configuración de conexión en " + dkprArchivo, dkprE);
        }
        return DkprDatosConexion.dkprDesdeUrl(
                dkprProps.getProperty("db.url"),
                dkprProps.getProperty("db.user", "postgres"),
                dkprProps.getProperty("db.password", ""));
    }
}
