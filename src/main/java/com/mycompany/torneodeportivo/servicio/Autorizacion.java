package com.mycompany.torneodeportivo.servicio;

import com.mycompany.torneodeportivo.mysql.daos.PermisoDAO;
import com.mycompany.torneodeportivo.mysql.modelos.EPermisos;
import com.mycompany.torneodeportivo.util.Sesion;

/**
 * Autoriza las acciones. Antes de guardar, actualizar, eliminar o registrar, se valida:
 * <ol>
 *   <li>que exista una sesión activa,</li>
 *   <li>que el rol tenga el permiso (lo cargado al iniciar sesión) y</li>
 *   <li>que en la base de datos la cuenta siga ACTIVA y su rol conserve el permiso (PermisoDAO).</li>
 * </ol>
 *
 * @author Christian Hernandez Garcia
 */
public final class Autorizacion {

    private static final PermisoDAO PERMISO_DAO = new PermisoDAO();

    private Autorizacion() {
    }

    /**
     * Detiene la acción si el usuario no está autorizado.
     *
     * @param permiso permiso necesario
     * @throws IllegalArgumentException con el motivo, para mostrarlo en un Alert
     */
    public static void exigir(EPermisos permiso) {
        if (!Sesion.activa()) {
            throw new IllegalArgumentException("No hay una sesión activa. Inicia sesión de nuevo.");
        }
        if (!Sesion.puede(permiso) || !PERMISO_DAO.tienePermiso(Sesion.getActual().getId(), permiso)) {
            throw new IllegalArgumentException("Acción no autorizada.\n\nTu perfil (" + Sesion.nombreRol()
                    + ") no tiene el permiso: " + permiso.getDescripcion() + ".");
        }
    }
}
