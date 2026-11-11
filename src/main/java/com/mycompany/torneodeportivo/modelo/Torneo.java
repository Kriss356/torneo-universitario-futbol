package com.mycompany.torneodeportivo.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Torneo al que pertenecen los partidos. Indica si es Apertura o Clausura y el periodo
 * (fecha de inicio y de fin) en el que se juega. Cada partido se juega dentro de un torneo
 * y en una jornada de ese torneo.
 *
 * @author Christian Hernandez Garcia
 * @version 1.1
 */
public class Torneo {

    /** Formato de fecha para mostrar en pantalla. */
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** El identificador único (llave primaria). */
    private int id;

    /** El nombre del torneo. */
    private String nombreTorneo;

    /** El tipo de torneo (Apertura o Clausura). */
    private TipoTorneo tipo;

    /** La fecha en que inicia el torneo. */
    private LocalDate fechaInicio;

    /** La fecha en que termina el torneo. */
    private LocalDate fechaFin;

    /** El estado del torneo. */
    private EstadoTorneo estado = EstadoTorneo.EN_PREPARACION;

    /**
     * Constructor vacío. Crea un objeto con los valores por defecto,
     * pensado para frameworks e instanciación sin datos.
     */
    public Torneo() {
        // Estado inicial por defecto; se completa después con los setters.
    }

    /**
     * Constructor con los datos mínimos indispensables para que el objeto
     * nazca en un estado válido. Todos los valores pasan por las validaciones
     * de sus setters.
     *
     * @param id identificador único
     * @param nombreTorneo nombre del torneo
     * @param tipo Apertura o Clausura
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @throws IllegalArgumentException si algún dato no cumple las validaciones
     */
    public Torneo(int id, String nombreTorneo, TipoTorneo tipo, LocalDate fechaInicio, LocalDate fechaFin) {
        setId(id);
        setNombreTorneo(nombreTorneo);
        setTipo(tipo);
        setPeriodo(fechaInicio, fechaFin);
    }

    /**
     * Obtiene el identificador único (llave primaria).
     *
     * @return el identificador único (llave primaria)
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador único (llave primaria).
     *
     * @param valor el identificador único (llave primaria)
     * @throws IllegalArgumentException si el valor es negativo
     */
    public final void setId(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("El identificador no puede ser negativo.");
        }
        this.id = valor;
    }

    /**
     * Obtiene el nombre del torneo.
     *
     * @return el nombre del torneo
     */
    public String getNombreTorneo() {
        return nombreTorneo;
    }

    /**
     * Establece el nombre del torneo.
     *
     * @param valor el nombre del torneo
     * @throws IllegalArgumentException si está vacío o tiene más de 60 caracteres
     */
    public final void setNombreTorneo(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo \"Nombre del torneo\" es obligatorio.");
        }
        if (valor.trim().length() > 60) {
            throw new IllegalArgumentException("El nombre del torneo admite máximo 60 caracteres.");
        }
        this.nombreTorneo = valor.trim();
    }

    /**
     * Obtiene el tipo de torneo (Apertura o Clausura).
     *
     * @return el tipo de torneo
     */
    public TipoTorneo getTipo() {
        return tipo;
    }

    /**
     * Establece el tipo de torneo (Apertura o Clausura).
     *
     * @param valor el tipo de torneo
     * @throws IllegalArgumentException si el valor es nulo
     */
    public final void setTipo(TipoTorneo valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo \"Tipo de torneo\" es obligatorio.");
        }
        this.tipo = valor;
    }

    /**
     * Obtiene la fecha en que inicia el torneo.
     *
     * @return la fecha de inicio
     */
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    /**
     * Obtiene la fecha en que termina el torneo.
     *
     * @return la fecha de fin
     */
    public LocalDate getFechaFin() {
        return fechaFin;
    }

    /**
     * Establece el periodo del torneo. Se reciben juntas las dos fechas para poder
     * comprobar que la de inicio no sea posterior a la de fin.
     *
     * @param inicio fecha de inicio
     * @param fin fecha de fin
     * @throws IllegalArgumentException si alguna es nula o el inicio es posterior al fin
     */
    public final void setPeriodo(LocalDate inicio, LocalDate fin) {
        if (inicio == null) {
            throw new IllegalArgumentException("El campo \"Fecha de inicio\" es obligatorio.");
        }
        if (fin == null) {
            throw new IllegalArgumentException("El campo \"Fecha de fin\" es obligatorio.");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        this.fechaInicio = inicio;
        this.fechaFin = fin;
    }

    /**
     * Obtiene el estado del torneo.
     *
     * @return el estado del torneo
     */
    public EstadoTorneo getEstado() {
        return estado;
    }

    /**
     * Establece el estado del torneo.
     *
     * @param valor el estado del torneo
     * @throws IllegalArgumentException si el valor es nulo
     */
    public final void setEstado(EstadoTorneo valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo \"Estado\" es obligatorio.");
        }
        this.estado = valor;
    }

    /**
     * Indica si una fecha cae dentro del periodo del torneo (incluye inicio y fin).
     *
     * @param fecha fecha a revisar
     * @return {@code true} si está entre la fecha de inicio y la de fin
     */
    public boolean incluyeFecha(LocalDate fecha) {
        return fecha != null && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }

    /**
     * Obtiene el periodo en texto.
     *
     * @return "dd/MM/yyyy - dd/MM/yyyy"
     */
    public String getPeriodo() {
        return fechaInicio.format(FORMATO) + " - " + fechaFin.format(FORMATO);
    }

    /**
     * Obtiene el nombre corto para mostrar en pantalla.
     *
     * @return por ejemplo "Apertura 2026 - Torneo Universitario de Fútbol"
     */
    public String getNombreCompleto() {
        return tipo.getTexto() + " " + fechaInicio.getYear() + " - " + nombreTorneo;
    }

    /**
     * Compara dos objetos Torneo únicamente por su identificador.
     *
     * @param obj objeto a comparar
     * @return {@code true} si son del mismo tipo y tienen el mismo id
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return id == ((Torneo) obj).id;
    }

    /**
     * Calcula el código hash a partir del identificador.
     *
     * @return código hash basado en el id
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    /**
     * Devuelve el estado completo del objeto.
     *
     * @return cadena con todos los atributos
     */
    @Override
    public String toString() {
        return "Torneo[id=" + id + ", nombreTorneo=" + nombreTorneo + ", tipo=" + tipo
                + ", fechaInicio=" + fechaInicio + ", fechaFin=" + fechaFin + ", estado=" + estado + "]";
    }

}
