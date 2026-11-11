package com.mycompany.torneodeportivo.mysql.modelos;

/**
 * Permisos (acciones autorizables) de la aplicación. Cada constante coincide con la columna
 * {@code clave} de la tabla {@code permiso}; la tabla {@code rol_permiso} indica qué rol tiene cada uno.
 *
 * @author Christian Hernandez Garcia
 */
public enum EPermisos {
    CATEGORIAS_VER("Consultar categorías"),
    CATEGORIAS_CREAR("Registrar categorías"),
    CATEGORIAS_EDITAR("Actualizar categorías"),
    CATEGORIAS_ELIMINAR("Eliminar categorías"),
    EQUIPOS_VER("Consultar equipos"),
    EQUIPOS_CREAR("Registrar equipos"),
    EQUIPOS_EDITAR("Actualizar equipos"),
    EQUIPOS_ELIMINAR("Eliminar equipos"),
    JUGADORES_VER("Consultar jugadores"),
    JUGADORES_CREAR("Registrar jugadores"),
    JUGADORES_EDITAR("Actualizar jugadores"),
    JUGADORES_ELIMINAR("Eliminar jugadores"),
    PARTIDOS_VER("Consultar partidos"),
    PARTIDOS_CREAR("Programar partidos"),
    PARTIDOS_EDITAR("Actualizar partidos"),
    PARTIDOS_ELIMINAR("Eliminar partidos"),
    RESULTADOS_REGISTRAR("Registrar resultados de partidos"),
    POSICIONES_VER("Consultar la tabla de posiciones"),
    POSICIONES_RECALCULAR("Recalcular la tabla de posiciones"),
    ADMINISTRADORES_VER("Consultar cuentas de usuario"),
    ADMINISTRADORES_CREAR("Registrar cuentas de usuario"),
    ADMINISTRADORES_EDITAR("Actualizar cuentas de usuario"),
    ADMINISTRADORES_ELIMINAR("Eliminar cuentas de usuario");

    private final String descripcion;

    EPermisos(String descripcion) {
        this.descripcion = descripcion;
    }

    /** @return texto para mostrar al usuario */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Arma el permiso de un módulo y una acción, por ejemplo ("CATEGORIAS", "CREAR") = CATEGORIAS_CREAR.
     *
     * @param modulo prefijo del módulo
     * @param accion VER, CREAR, EDITAR o ELIMINAR
     * @return el permiso correspondiente
     */
    public static EPermisos de(String modulo, String accion) {
        return valueOf(modulo + "_" + accion);
    }
}
