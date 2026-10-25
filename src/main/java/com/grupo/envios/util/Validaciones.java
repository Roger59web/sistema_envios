package com.grupo.envios.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validaciones de datos de entrada. Los métodos lanzan {@link NegocioException}
 * con un mensaje listo para mostrar al usuario.
 */
public final class Validaciones {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+() -]{6,15}$");
    private static final Pattern DECIMAL = Pattern.compile("^-?\\d+([.,]\\d+)?$");
    private static final Pattern ENTERO = Pattern.compile("^-?\\d+$");

    private Validaciones() {
    }

    public static boolean vacio(String dkprTexto) {
        return dkprTexto == null || dkprTexto.trim().isEmpty();
    }

    /** Texto obligatorio: devuelve el valor sin espacios sobrantes. */
    public static String requerido(String dkprValor, String dkprCampo, int dkprMaxCaracteres) {
        if (vacio(dkprValor)) {
            throw new NegocioException("El campo \"" + dkprCampo + "\" es obligatorio.");
        }
        return limitar(dkprValor.trim(), dkprCampo, dkprMaxCaracteres);
    }

    /** Texto opcional: devuelve null si está vacío. */
    public static String opcional(String dkprValor, String dkprCampo, int dkprMaxCaracteres) {
        if (vacio(dkprValor)) {
            return null;
        }
        return limitar(dkprValor.trim(), dkprCampo, dkprMaxCaracteres);
    }

    private static String limitar(String dkprValor, String dkprCampo, int dkprMaxCaracteres) {
        if (dkprValor.length() > dkprMaxCaracteres) {
            throw new NegocioException("El campo \"" + dkprCampo + "\" no puede superar " + dkprMaxCaracteres + " caracteres.");
        }
        return dkprValor;
    }

    /** Número decimal obligatorio (acepta coma o punto como separador). */
    public static BigDecimal decimal(String dkprTexto, String dkprCampo) {
        if (vacio(dkprTexto)) {
            throw new NegocioException("El campo \"" + dkprCampo + "\" es obligatorio.");
        }
        String dkprLimpio = dkprTexto.trim();
        if (!DECIMAL.matcher(dkprLimpio).matches()) {
            throw new NegocioException("El campo \"" + dkprCampo + "\" debe ser un número válido.");
        }
        return new BigDecimal(dkprLimpio.replace(',', '.'));
    }

    /** Número entero obligatorio. */
    public static int entero(String dkprTexto, String dkprCampo) {
        if (vacio(dkprTexto)) {
            throw new NegocioException("El campo \"" + dkprCampo + "\" es obligatorio.");
        }
        String dkprLimpio = dkprTexto.trim();
        if (!ENTERO.matcher(dkprLimpio).matches() || dkprLimpio.length() > 9) {
            throw new NegocioException("El campo \"" + dkprCampo + "\" debe ser un número entero válido.");
        }
        return Integer.parseInt(dkprLimpio);
    }

    /** Valida un DNI (8 dígitos). El parámetro "etiqueta" es el inicio del mensaje, ej. "El DNI". */
    public static void validarDni(String dkprDni, String dkprEtiqueta) {
        if (vacio(dkprDni)) {
            throw new NegocioException(dkprEtiqueta + " es obligatorio.");
        }
        if (!dkprDni.trim().matches("\\d{8}")) {
            throw new NegocioException(dkprEtiqueta + " debe tener 8 dígitos numéricos.");
        }
    }

    /** Valida el número según el tipo de documento: DNI (8 dígitos), RUC (11) o CE (9 a 12 caracteres). */
    public static void validarDocumento(String dkprTipo, String dkprNumero) {
        if (dkprTipo == null) {
            throw new NegocioException("Selecciona el tipo de documento.");
        }
        if (vacio(dkprNumero)) {
            throw new NegocioException("Ingresa el número de documento.");
        }
        String dkprN = dkprNumero.trim();
        switch (dkprTipo) {
            case "DNI":
                if (!dkprN.matches("\\d{8}")) {
                    throw new NegocioException("El DNI debe tener 8 dígitos numéricos.");
                }
                break;
            case "RUC":
                if (!dkprN.matches("\\d{11}")) {
                    throw new NegocioException("El RUC debe tener 11 dígitos numéricos.");
                }
                break;
            case "CE":
                if (!dkprN.matches("[A-Za-z0-9]{9,12}")) {
                    throw new NegocioException("El carné de extranjería debe tener entre 9 y 12 caracteres (letras y números).");
                }
                break;
            default:
                throw new NegocioException("Tipo de documento no válido.");
        }
    }

    /** El teléfono es opcional: si viene vacío no se valida. */
    public static void validarTelefono(String dkprTelefono) {
        if (vacio(dkprTelefono)) {
            return;
        }
        if (!TELEFONO.matcher(dkprTelefono.trim()).matches()) {
            throw new NegocioException("El teléfono no es válido (solo números, espacios, guiones o +; entre 6 y 15 caracteres).");
        }
    }

    /** El correo es opcional: si viene vacío no se valida. */
    public static void validarEmail(String dkprEmail) {
        if (vacio(dkprEmail)) {
            return;
        }
        if (!EMAIL.matcher(dkprEmail.trim()).matches()) {
            throw new NegocioException("El correo electrónico no es válido.");
        }
    }
}
