package com.grupo.envios.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ConfiguracionConexion {

    private static final Path ARCHIVO_DESARROLLO = Path.of("db.properties");

    private static DatosConexion dkprActual;

    private ConfiguracionConexion() {
    }

    public static Path archivoUsuario() {
        String dkprAppData = System.getenv("APPDATA");
        Path dkprBase = dkprAppData != null ? Path.of(dkprAppData) : Path.of(System.getProperty("user.home"));
        return dkprBase.resolve("SistemaEnvios").resolve("db.properties");
    }

    public static Path archivoEnUso() {
        return Files.exists(ARCHIVO_DESARROLLO) ? ARCHIVO_DESARROLLO : archivoUsuario();
    }

    public static synchronized boolean existe() {
        return dkprActual != null || Files.exists(archivoEnUso());
    }

    public static synchronized DatosConexion actual() {
        if (dkprActual == null) {
            dkprActual = leer(archivoEnUso());
        }
        return dkprActual;
    }

    public static synchronized DatosConexion leerSiExiste() {
        return existe() ? actual() : null;
    }

    public static synchronized void usar(DatosConexion dkprDatos) {
        dkprActual = dkprDatos;
    }

    public static synchronized void guardar(DatosConexion dkprDatos) throws IOException {
        Path dkprArchivo = archivoEnUso();
        if (dkprArchivo.getParent() != null) {
            Files.createDirectories(dkprArchivo.getParent());
        }
        Properties dkprProps = new Properties();
        dkprProps.setProperty("db.url", dkprDatos.url());
        dkprProps.setProperty("db.user", dkprDatos.usuario());
        dkprProps.setProperty("db.password", dkprDatos.clave());
        try (OutputStream dkprOut = Files.newOutputStream(dkprArchivo)) {
            dkprProps.store(dkprOut, null);
        }
        dkprActual = dkprDatos;
    }

    private static DatosConexion leer(Path dkprArchivo) {
        Properties dkprProps = new Properties();
        try (InputStream dkprIn = Files.newInputStream(dkprArchivo)) {
            dkprProps.load(dkprIn);
        } catch (IOException dkprE) {
            throw new IllegalStateException("No se encontró la configuración de conexión en " + dkprArchivo, dkprE);
        }
        return DatosConexion.desdeUrl(
                dkprProps.getProperty("db.url"),
                dkprProps.getProperty("db.user", "postgres"),
                dkprProps.getProperty("db.password", ""));
    }
}
