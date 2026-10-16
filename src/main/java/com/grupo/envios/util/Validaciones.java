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

    public static boolean vacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    /** Texto obligatorio: devuelve el valor sin espacios sobrantes. */
    public static String requerido(String valor, String campo, int maxCaracteres) {
        if (vacio(valor)) {
            throw new NegocioException("El campo \"" + campo + "\" es obligatorio.");
        }
        return limitar(valor.trim(), campo, maxCaracteres);
    }

    /** Texto opcional: devuelve null si está vacío. */
    public static String opcional(String valor, String campo, int maxCaracteres) {
        if (vacio(valor)) {
            return null;
        }
        return limitar(valor.trim(), campo, maxCaracteres);
    }

    private static String limitar(String valor, String campo, int maxCaracteres) {
        if (valor.length() > maxCaracteres) {
            throw new NegocioException("El campo \"" + campo + "\" no puede superar " + maxCaracteres + " caracteres.");
        }
        return valor;
    }

    /** Número decimal obligatorio (acepta coma o punto como separador). */
    public static BigDecimal decimal(String texto, String campo) {
        if (vacio(texto)) {
            throw new NegocioException("El campo \"" + campo + "\" es obligatorio.");
        }
        String limpio = texto.trim();
        if (!DECIMAL.matcher(limpio).matches()) {
            throw new NegocioException("El campo \"" + campo + "\" debe ser un número válido.");
        }
        return new BigDecimal(limpio.replace(',', '.'));
    }

    /** Número entero obligatorio. */
    public static int entero(String texto, String campo) {
        if (vacio(texto)) {
            throw new NegocioException("El campo \"" + campo + "\" es obligatorio.");
        }
        String limpio = texto.trim();
        if (!ENTERO.matcher(limpio).matches() || limpio.length() > 9) {
            throw new NegocioException("El campo \"" + campo + "\" debe ser un número entero válido.");
        }
        return Integer.parseInt(limpio);
    }

    /** Valida un DNI (8 dígitos). El parámetro "etiqueta" es el inicio del mensaje, ej. "El DNI". */
    public static void validarDni(String dni, String etiqueta) {
        if (vacio(dni)) {
            throw new NegocioException(etiqueta + " es obligatorio.");
        }
        if (!dni.trim().matches("\\d{8}")) {
            throw new NegocioException(etiqueta + " debe tener 8 dígitos numéricos.");
        }
    }

    /** Valida el número según el tipo de documento: DNI (8 dígitos), RUC (11) o CE (9 a 12 caracteres). */
    public static void validarDocumento(String tipo, String numero) {
        if (tipo == null) {
            throw new NegocioException("Selecciona el tipo de documento.");
        }
        if (vacio(numero)) {
            throw new NegocioException("Ingresa el número de documento.");
        }
        String n = numero.trim();
        switch (tipo) {
            case "DNI":
                if (!n.matches("\\d{8}")) {
                    throw new NegocioException("El DNI debe tener 8 dígitos numéricos.");
                }
                break;
            case "RUC":
                if (!n.matches("\\d{11}")) {
                    throw new NegocioException("El RUC debe tener 11 dígitos numéricos.");
                }
                break;
            case "CE":
                if (!n.matches("[A-Za-z0-9]{9,12}")) {
                    throw new NegocioException("El carné de extranjería debe tener entre 9 y 12 caracteres (letras y números).");
                }
                break;
            default:
                throw new NegocioException("Tipo de documento no válido.");
        }
    }

    /** El teléfono es opcional: si viene vacío no se valida. */
    public static void validarTelefono(String telefono) {
        if (vacio(telefono)) {
            return;
        }
        if (!TELEFONO.matcher(telefono.trim()).matches()) {
            throw new NegocioException("El teléfono no es válido (solo números, espacios, guiones o +; entre 6 y 15 caracteres).");
        }
    }

    /** El correo es opcional: si viene vacío no se valida. */
    public static void validarEmail(String email) {
        if (vacio(email)) {
            return;
        }
        if (!EMAIL.matcher(email.trim()).matches()) {
            throw new NegocioException("El correo electrónico no es válido.");
        }
    }
}
