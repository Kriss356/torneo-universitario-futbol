package com.mycompany.torneodeportivo.modelo;

/**
 * Estados posibles de un torneo.
 *
 * @author Christian Hernandez Garcia
 * @version 1.0
 */
public enum EstadoTorneo {
    /** Se están inscribiendo equipos; aún no inicia. */
    EN_PREPARACION,
    /** Ya se están jugando las jornadas. */
    EN_CURSO,
    /** Detenido temporalmente. */
    PAUSADO,
    /** Terminó. */
    FINALIZADO
}
