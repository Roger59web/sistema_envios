package com.grupo.envios.config;

import com.grupo.envios.util.NegocioException;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class PostgresPortatil {

    public static final int PUERTO = 5440;

    private static final String BASE = "sistema_envios";
    private static final String USUARIO = "postgres";
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private static boolean dkprEncendidoPorMi;

    private PostgresPortatil() {
    }

    public static boolean disponible() {
        return carpetaPgsql() != null;
    }

    public static synchronized void iniciar() throws Exception {
        Path dkprPgsql = carpetaPgsql();
        Path dkprDatos = carpetaDatos();
        if (!Files.exists(dkprDatos.resolve("PG_VERSION"))) {
            crearCluster(dkprPgsql, dkprDatos);
        }
        if (!estaEncendido(dkprPgsql, dkprDatos)) {
            if (puertoOcupado()) {
                throw new NegocioException("El puerto " + PUERTO + " está ocupado por otro programa.\n"
                        + "Ciérralo e intenta abrir el sistema de nuevo.");
            }
            int dkprCodigo = ejecutar(List.of(pgCtl(dkprPgsql), "start", "-D", dkprDatos.toString(),
                    "-l", carpetaRaiz().resolve("postgres.log").toString(), "-w", "-t", "60"), 90);
            if (dkprCodigo != 0) {
                throw new IOException("No se pudo encender la base de datos. Revisa "
                        + carpetaRaiz().resolve("postgres.log"));
            }
        }
        dkprEncendidoPorMi = true;
        DatosConexion dkprConexion = new DatosConexion("localhost", PUERTO, BASE, USUARIO, leerClave());
        InicializadorBase.preparar(dkprConexion);
        ConfiguracionConexion.usar(dkprConexion);
    }

    public static synchronized void detener() {
        if (!dkprEncendidoPorMi) {
            return;
        }
        try {
            ejecutar(List.of(pgCtl(carpetaPgsql()), "stop", "-D", carpetaDatos().toString(), "-m", "fast", "-w"), 60);
        } catch (Exception dkprE) {
            dkprE.printStackTrace();
        }
        dkprEncendidoPorMi = false;
    }

    private static void crearCluster(Path dkprPgsql, Path dkprDatos) throws IOException, InterruptedException {
        Files.createDirectories(carpetaRaiz());
        String dkprClave = generarClave();
        Files.writeString(archivoClave(), dkprClave, StandardCharsets.UTF_8);
        Path dkprArchivoClave = Files.createTempFile("pg", ".txt");
        try {
            Files.writeString(dkprArchivoClave, dkprClave, StandardCharsets.UTF_8);
            int dkprCodigo = ejecutar(List.of(dkprPgsql.resolve("bin").resolve("initdb.exe").toString(),
                    "-D", dkprDatos.toString(), "-U", USUARIO, "-E", "UTF8",
                    "-A", "scram-sha-256", "--pwfile=" + dkprArchivoClave), 300);
            if (dkprCodigo != 0) {
                throw new IOException("No se pudo crear la carpeta de datos " + dkprDatos
                        + ". Revisa " + carpetaRaiz().resolve("pg_ctl.log"));
            }
        } finally {
            Files.deleteIfExists(dkprArchivoClave);
        }
        Files.writeString(dkprDatos.resolve("postgresql.conf"),
                System.lineSeparator() + "listen_addresses = 'localhost'" + System.lineSeparator()
                        + "port = " + PUERTO + System.lineSeparator(),
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    private static boolean estaEncendido(Path dkprPgsql, Path dkprDatos) throws IOException, InterruptedException {
        return ejecutar(List.of(pgCtl(dkprPgsql), "status", "-D", dkprDatos.toString()), 30) == 0;
    }

    private static boolean puertoOcupado() {
        try (Socket dkprSocket = new Socket()) {
            dkprSocket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), PUERTO), 500);
            return true;
        } catch (IOException dkprE) {
            return !puertoLibre();
        }
    }

    private static boolean puertoLibre() {
        try (ServerSocket dkprServidor = new ServerSocket(PUERTO, 1, InetAddress.getLoopbackAddress())) {
            return true;
        } catch (IOException dkprE) {
            return false;
        }
    }

    private static int ejecutar(List<String> dkprComando, long dkprSegundos) throws IOException, InterruptedException {
        Files.createDirectories(carpetaRaiz());
        File dkprLog = carpetaRaiz().resolve("pg_ctl.log").toFile();
        Process dkprProceso = new ProcessBuilder(new ArrayList<>(dkprComando))
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.appendTo(dkprLog))
                .redirectInput(ProcessBuilder.Redirect.from(new File("NUL")))
                .start();
        if (!dkprProceso.waitFor(dkprSegundos, TimeUnit.SECONDS)) {
            dkprProceso.destroyForcibly();
            throw new IOException("La base de datos no respondió a tiempo. Revisa " + dkprLog);
        }
        return dkprProceso.exitValue();
    }

    private static String leerClave() throws IOException {
        if (!Files.exists(archivoClave())) {
            throw new IOException("Falta el archivo " + archivoClave() + ". Sin él no se puede abrir la base.");
        }
        return Files.readString(archivoClave(), StandardCharsets.UTF_8).trim();
    }

    private static String generarClave() {
        SecureRandom dkprAzar = new SecureRandom();
        StringBuilder dkprClave = new StringBuilder();
        for (int dkprI = 0; dkprI < 24; dkprI++) {
            dkprClave.append(LETRAS.charAt(dkprAzar.nextInt(LETRAS.length())));
        }
        return dkprClave.toString();
    }

    private static String pgCtl(Path dkprPgsql) {
        return dkprPgsql.resolve("bin").resolve("pg_ctl.exe").toString();
    }

    private static Path carpetaPgsql() {
        String dkprPrueba = System.getProperty("envios.pgsql");
        if (dkprPrueba != null) {
            return Files.exists(Path.of(dkprPrueba, "bin", "pg_ctl.exe")) ? Path.of(dkprPrueba) : null;
        }
        String dkprLanzador = System.getProperty("jpackage.app-path");
        if (dkprLanzador == null) {
            return null;
        }
        Path dkprRaiz = Path.of(dkprLanzador).getParent();
        for (Path dkprCandidata : List.of(dkprRaiz.resolve("pgsql"), dkprRaiz.resolve("app").resolve("pgsql"))) {
            if (Files.exists(dkprCandidata.resolve("bin").resolve("pg_ctl.exe"))) {
                return dkprCandidata;
            }
        }
        return null;
    }

    private static Path carpetaRaiz() {
        String dkprProgramData = System.getenv("ProgramData");
        return Path.of(dkprProgramData != null ? dkprProgramData : "C:\\ProgramData", "SistemaEnvios");
    }

    private static Path carpetaDatos() {
        return carpetaRaiz().resolve("datos");
    }

    private static Path archivoClave() {
        return carpetaRaiz().resolve("clave.txt");
    }
}
