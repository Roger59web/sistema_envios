package com.grupo.envios.config;

import com.grupo.envios.util.DkprNegocioException;

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

public final class DkprPostgresPortatil {

    public static final int PUERTO = 5440;

    private static final String BASE = "sistema_envios";
    private static final String USUARIO = "postgres";
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private static boolean dkprEncendidoPorMi;

    private DkprPostgresPortatil() {
    }

    public static boolean dkprDisponible() {
        return dkprCarpetaPgsql() != null;
    }

    public static synchronized void dkprIniciar() throws Exception {
        Path dkprPgsql = dkprCarpetaPgsql();
        Path dkprDatos = dkprCarpetaDatos();
        if (!Files.exists(dkprDatos.resolve("PG_VERSION"))) {
            dkprCrearCluster(dkprPgsql, dkprDatos);
        }
        if (!dkprEstaEncendido(dkprPgsql, dkprDatos)) {
            if (dkprPuertoOcupado()) {
                throw new DkprNegocioException("El puerto " + PUERTO + " está ocupado por otro programa.\n"
                        + "Ciérralo e intenta abrir el sistema de nuevo.");
            }
            int dkprCodigo = dkprEjecutar(List.of(dkprPgCtl(dkprPgsql), "start", "-D", dkprDatos.toString(),
                    "-l", dkprCarpetaRaiz().resolve("postgres.log").toString(), "-w", "-t", "60"), 90);
            if (dkprCodigo != 0) {
                throw new IOException("No se pudo encender la base de datos. Revisa "
                        + dkprCarpetaRaiz().resolve("postgres.log"));
            }
        }
        dkprEncendidoPorMi = true;
        DkprDatosConexion dkprConexion = new DkprDatosConexion("localhost", PUERTO, BASE, USUARIO, dkprLeerClave());
        DkprInicializadorBase.dkprPreparar(dkprConexion);
        DkprConfiguracionConexion.dkprUsar(dkprConexion);
    }

    public static synchronized void dkprDetener() {
        if (!dkprEncendidoPorMi) {
            return;
        }
        try {
            dkprEjecutar(List.of(dkprPgCtl(dkprCarpetaPgsql()), "stop", "-D", dkprCarpetaDatos().toString(), "-m", "fast", "-w"), 60);
        } catch (Exception dkprE) {
            dkprE.printStackTrace();
        }
        dkprEncendidoPorMi = false;
    }

    private static void dkprCrearCluster(Path dkprPgsql, Path dkprDatos) throws IOException, InterruptedException {
        Files.createDirectories(dkprCarpetaRaiz());
        String dkprClave = dkprGenerarClave();
        Files.writeString(dkprArchivoClave(), dkprClave, StandardCharsets.UTF_8);
        Path dkprArchivoClave = Files.createTempFile("pg", ".txt");
        try {
            Files.writeString(dkprArchivoClave, dkprClave, StandardCharsets.UTF_8);
            int dkprCodigo = dkprEjecutar(List.of(dkprPgsql.resolve("bin").resolve("initdb.exe").toString(),
                    "-D", dkprDatos.toString(), "-U", USUARIO, "-E", "UTF8",
                    "-A", "scram-sha-256", "--pwfile=" + dkprArchivoClave), 300);
            if (dkprCodigo != 0) {
                throw new IOException("No se pudo crear la carpeta de datos " + dkprDatos
                        + ". Revisa " + dkprCarpetaRaiz().resolve("pg_ctl.log"));
            }
        } finally {
            Files.deleteIfExists(dkprArchivoClave);
        }
        Files.writeString(dkprDatos.resolve("postgresql.conf"),
                System.lineSeparator() + "listen_addresses = 'localhost'" + System.lineSeparator()
                        + "port = " + PUERTO + System.lineSeparator(),
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    private static boolean dkprEstaEncendido(Path dkprPgsql, Path dkprDatos) throws IOException, InterruptedException {
        return dkprEjecutar(List.of(dkprPgCtl(dkprPgsql), "status", "-D", dkprDatos.toString()), 30) == 0;
    }

    private static boolean dkprPuertoOcupado() {
        try (Socket dkprSocket = new Socket()) {
            dkprSocket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), PUERTO), 500);
            return true;
        } catch (IOException dkprE) {
            return !dkprPuertoLibre();
        }
    }

    private static boolean dkprPuertoLibre() {
        try (ServerSocket dkprServidor = new ServerSocket(PUERTO, 1, InetAddress.getLoopbackAddress())) {
            return true;
        } catch (IOException dkprE) {
            return false;
        }
    }

    private static int dkprEjecutar(List<String> dkprComando, long dkprSegundos) throws IOException, InterruptedException {
        Files.createDirectories(dkprCarpetaRaiz());
        File dkprLog = dkprCarpetaRaiz().resolve("pg_ctl.log").toFile();
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

    private static String dkprLeerClave() throws IOException {
        if (!Files.exists(dkprArchivoClave())) {
            throw new IOException("Falta el archivo " + dkprArchivoClave() + ". Sin él no se puede abrir la base.");
        }
        return Files.readString(dkprArchivoClave(), StandardCharsets.UTF_8).trim();
    }

    private static String dkprGenerarClave() {
        SecureRandom dkprAzar = new SecureRandom();
        StringBuilder dkprClave = new StringBuilder();
        for (int dkprI = 0; dkprI < 24; dkprI++) {
            dkprClave.append(LETRAS.charAt(dkprAzar.nextInt(LETRAS.length())));
        }
        return dkprClave.toString();
    }

    private static String dkprPgCtl(Path dkprPgsql) {
        return dkprPgsql.resolve("bin").resolve("pg_ctl.exe").toString();
    }

    private static Path dkprCarpetaPgsql() {
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

    private static Path dkprCarpetaRaiz() {
        String dkprProgramData = System.getenv("ProgramData");
        return Path.of(dkprProgramData != null ? dkprProgramData : "C:\\ProgramData", "SistemaEnvios");
    }

    private static Path dkprCarpetaDatos() {
        return dkprCarpetaRaiz().resolve("datos");
    }

    private static Path dkprArchivoClave() {
        return dkprCarpetaRaiz().resolve("clave.txt");
    }
}
