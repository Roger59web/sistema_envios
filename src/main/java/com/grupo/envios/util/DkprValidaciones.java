package com.grupo.envios.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class DkprValidaciones {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+() -]{6,15}$");
    private static final Pattern DECIMAL = Pattern.compile("^-?\\d+([.,]\\d+)?$");
    private static final Pattern ENTERO = Pattern.compile("^-?\\d+$");

    private DkprValidaciones() {
    }

    public static boolean dkprVacio(String dkprTexto) {
        return dkprTexto == null || dkprTexto.trim().isEmpty();
    }

    public static String dkprRequerido(String dkprValor, String dkprCampo, int dkprMaxCaracteres) {
        if (dkprVacio(dkprValor)) {
            throw new DkprNegocioException("El campo \"" + dkprCampo + "\" es obligatorio.");
        }
        return dkprLimitar(dkprValor.trim(), dkprCampo, dkprMaxCaracteres);
    }

    public static String dkprOpcional(String dkprValor, String dkprCampo, int dkprMaxCaracteres) {
        if (dkprVacio(dkprValor)) {
            return null;
        }
        return dkprLimitar(dkprValor.trim(), dkprCampo, dkprMaxCaracteres);
    }

    private static String dkprLimitar(String dkprValor, String dkprCampo, int dkprMaxCaracteres) {
        if (dkprValor.length() > dkprMaxCaracteres) {
            throw new DkprNegocioException("El campo \"" + dkprCampo + "\" no puede superar " + dkprMaxCaracteres + " caracteres.");
        }
        return dkprValor;
    }

    public static BigDecimal dkprDecimal(String dkprTexto, String dkprCampo) {
        if (dkprVacio(dkprTexto)) {
            throw new DkprNegocioException("El campo \"" + dkprCampo + "\" es obligatorio.");
        }
        String dkprLimpio = dkprTexto.trim();
        if (!DECIMAL.matcher(dkprLimpio).matches()) {
            throw new DkprNegocioException("El campo \"" + dkprCampo + "\" debe ser un número válido.");
        }
        return new BigDecimal(dkprLimpio.replace(',', '.'));
    }

    public static int dkprEntero(String dkprTexto, String dkprCampo) {
        if (dkprVacio(dkprTexto)) {
            throw new DkprNegocioException("El campo \"" + dkprCampo + "\" es obligatorio.");
        }
        String dkprLimpio = dkprTexto.trim();
        if (!ENTERO.matcher(dkprLimpio).matches() || dkprLimpio.length() > 9) {
            throw new DkprNegocioException("El campo \"" + dkprCampo + "\" debe ser un número entero válido.");
        }
        return Integer.parseInt(dkprLimpio);
    }

    public static void dkprValidarDni(String dkprDni, String dkprEtiqueta) {
        if (dkprVacio(dkprDni)) {
            throw new DkprNegocioException(dkprEtiqueta + " es obligatorio.");
        }
        if (!dkprDni.trim().matches("\\d{8}")) {
            throw new DkprNegocioException(dkprEtiqueta + " debe tener 8 dígitos numéricos.");
        }
    }

    public static void dkprValidarDocumento(String dkprTipo, String dkprNumero) {
        if (dkprTipo == null) {
            throw new DkprNegocioException("Selecciona el tipo de documento.");
        }
        if (dkprVacio(dkprNumero)) {
            throw new DkprNegocioException("Ingresa el número de documento.");
        }
        String dkprN = dkprNumero.trim();
        switch (dkprTipo) {
            case "DNI":
                if (!dkprN.matches("\\d{8}")) {
                    throw new DkprNegocioException("El DNI debe tener 8 dígitos numéricos.");
                }
                break;
            case "RUC":
                if (!dkprN.matches("\\d{11}")) {
                    throw new DkprNegocioException("El RUC debe tener 11 dígitos numéricos.");
                }
                break;
            case "CE":
                if (!dkprN.matches("[A-Za-z0-9]{9,12}")) {
                    throw new DkprNegocioException("El carné de extranjería debe tener entre 9 y 12 caracteres (letras y números).");
                }
                break;
            default:
                throw new DkprNegocioException("Tipo de documento no válido.");
        }
    }

    public static void dkprValidarTelefono(String dkprTelefono) {
        if (dkprVacio(dkprTelefono)) {
            return;
        }
        if (!TELEFONO.matcher(dkprTelefono.trim()).matches()) {
            throw new DkprNegocioException("El teléfono no es válido (solo números, espacios, guiones o +; entre 6 y 15 caracteres).");
        }
    }

    public static void dkprValidarEmail(String dkprEmail) {
        if (dkprVacio(dkprEmail)) {
            return;
        }
        if (!EMAIL.matcher(dkprEmail.trim()).matches()) {
            throw new DkprNegocioException("El correo electrónico no es válido.");
        }
    }
}
