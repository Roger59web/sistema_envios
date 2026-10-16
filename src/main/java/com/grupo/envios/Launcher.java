package com.grupo.envios;

/**
 * Clase de arranque para ejecutar desde IntelliJ (clic derecho > Run 'Launcher.main()').
 * Es necesaria porque una clase que extiende Application no puede ser la clase principal
 * cuando JavaFX viene como dependencia de Maven y el proyecto no usa módulos.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
