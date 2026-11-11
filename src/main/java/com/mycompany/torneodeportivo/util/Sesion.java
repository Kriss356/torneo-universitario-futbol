package com.mycompany.torneodeportivo.util;

import com.mycompany.torneodeportivo.modelo.Administrador;
import com.mycompany.torneodeportivo.modelo.AdministradorEquipos;
import com.mycompany.torneodeportivo.modelo.AdministradorPartidos;
import com.mycompany.torneodeportivo.modelo.AdministradorTorneo;
import com.mycompany.torneodeportivo.mysql.modelos.EPermisos;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Sesión activa: guarda al administrador que inició sesión, el id de su registro en la tabla
 * {@code sesion} y los permisos de su rol (leídos de la BD con PermisoDAO).
 *
 * @author Christian Hernandez Garcia
 */
public final class Sesion {

    private static Administrador actual;
    private static int idSesion;
    private static Set<EPermisos> permisos = EnumSet.noneOf(EPermisos.class);

    private Sesion() {
    }

    /** Abre la sesión después de validar las credenciales. */
    public static void iniciar(Administrador administrador, int idSesionBD, Set<EPermisos> permisosRol) {
        actual = administrador;
        idSesion = idSesionBD;
        permisos = permisosRol.isEmpty() ? EnumSet.noneOf(EPermisos.class) : EnumSet.copyOf(permisosRol);
    }

    /** Limpia todos los datos de la sesión. */
    public static void cerrar() {
        if (actual != null) actual.cerrarSesion();
        actual = null;
        idSesion = 0;
        permisos = EnumSet.noneOf(EPermisos.class);
    }

    public static boolean activa() { return actual != null; }
    public static Administrador getActual() { return actual; }
    public static int getIdSesion() { return idSesion; }
    public static Set<EPermisos> getPermisos() { return Collections.unmodifiableSet(permisos); }

    /** Se usa cuando el administrador edita sus propios datos (nombre, correo...). */
    public static void setActual(Administrador a) { actual = a; }

    /** @return true si el rol del usuario que inició sesión tiene el permiso */
    public static boolean puede(EPermisos permiso) {
        return actual != null && permisos.contains(permiso);
    }

    /** @return true si el administrador es el mismo que inició sesión (compara por id) */
    public static boolean esUsuarioActual(Administrador a) {
        return actual != null && actual.equals(a);
    }

    public static boolean esEquipos() { return actual instanceof AdministradorEquipos; }
    public static boolean esPartidos() { return actual instanceof AdministradorPartidos; }
    public static boolean esTorneo() { return actual instanceof AdministradorTorneo; }

    public static String nombreRol() {
        return actual == null ? "" : actual.getRol();
    }
}
