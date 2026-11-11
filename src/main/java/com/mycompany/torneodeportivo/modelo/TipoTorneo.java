package com.mycompany.torneodeportivo.modelo;

/**
 * Tipo de torneo dentro del año: el de la primera mitad o el de la segunda.
 *
 * @author Christian Hernandez Garcia
 * @version 1.0
 */
public enum TipoTorneo {
    /** Torneo de la segunda mitad del año (agosto a diciembre). */
    APERTURA("Apertura"),
    /** Torneo de la primera mitad del año (enero a mayo). */
    CLAUSURA("Clausura");

    /** Texto para mostrar en pantalla. */
    private final String texto;

    TipoTorneo(String texto) {
        this.texto = texto;
    }

    /**
     * Obtiene el texto para mostrar en pantalla.
     *
     * @return texto con mayúscula inicial (por ejemplo, "Apertura")
     */
    public String getTexto() {
        return texto;
    }
}
