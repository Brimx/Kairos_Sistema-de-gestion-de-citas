package co.edu.upc.citasmedicas.service;

import co.edu.upc.citasmedicas.model.Usuario;

/**
 * Mantiene el usuario autenticado durante la ejecucion actual de la aplicacion.
 */
public final class Session {

    private static Usuario usuarioActual;

    private Session() {
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static void cerrar() {
        usuarioActual = null;
    }
}
