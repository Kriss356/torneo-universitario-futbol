package com.mycompany.torneodeportivo.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Partido entre dos equipos. Puede tener un {@link Resultado} una vez que se registra el marcador.
 *
 * @author Christian Hernandez Garcia
 * @version 1.0
 */
public class Partido {

    /** El identificador único (llave primaria). */
    private int id;

    /** El torneo al que pertenece el partido. */
    private Torneo torneo;

    /** La jornada (fecha del calendario del torneo) en la que se juega. */
    private int jornada = 1;

    /** El equipo local. */
    private Equipo equipoLocal;

    /** El equipo visitante. */
    private Equipo equipoVisitante;

    /** La fecha del partido. */
    private LocalDate fecha;

    /** La hora del partido. */
    private LocalTime hora;

    /** El lugar donde se juega. */
    private String lugar = "Por definir";

    /** El estado del partido. */
    private EstadoPartido estado = EstadoPartido.PROGRAMADO;

    /** El resultado del partido ({@code null} mientras no se registre). */
    private Resultado resultado;

    /**
     * Constructor vacío. Crea un objeto con los valores por defecto,
     * pensado para frameworks e instanciación sin datos.
     */
    public Partido() {
        // Estado inicial por defecto; se completa después con los setters.
    }

    /**
     * Constructor con los datos mínimos indispensables para que el objeto
     * nazca en un estado válido. Todos los valores pasan por las validaciones
     * de sus setters.
     *
     * @param id identificador único
     * @param equipoLocal equipo local
     * @param equipoVisitante equipo visitante
     * @param fecha fecha del partido
     * @param hora hora del partido
     * @throws IllegalArgumentException si algún dato no cumple las validaciones
     */
    public Partido(int id, Equipo equipoLocal, Equipo equipoVisitante, LocalDate fecha, LocalTime hora) {
        setId(id);
        setEquipoLocal(equipoLocal);
        setEquipoVisitante(equipoVisitante);
        setFecha(fecha);
        setHora(hora);
        if (equipoLocal.equals(equipoVisitante)) {
            throw new IllegalArgumentException("El equipo local y el visitante deben ser distintos.");
        }
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
     * Obtiene el torneo al que pertenece el partido.
     *
     * @return el torneo
     */
    public Torneo getTorneo() {
        return torneo;
    }

    /**
     * Establece el torneo al que pertenece el partido.
     *
     * @param valor el torneo
     * @throws IllegalArgumentException si el valor es nulo
     */
    public final void setTorneo(Torneo valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo \"Torneo\" es obligatorio.");
        }
        this.torneo = valor;
    }

    /**
     * Obtiene la jornada en la que se juega el partido.
     *
     * @return la jornada
     */
    public int getJornada() {
        return jornada;
    }

    /**
     * Establece la jornada en la que se juega el partido.
     *
     * @param valor la jornada (de 1 a 38)
     * @throws IllegalArgumentException si está fuera del rango 1 a 38
     */
    public final void setJornada(int valor) {
        if (valor < 1 || valor > 38) {
            throw new IllegalArgumentException("La jornada debe estar entre 1 y 38.");
        }
        this.jornada = valor;
    }

    /**
     * Obtiene el nombre del torneo para mostrarlo en la tabla.
     *
     * @return nombre completo del torneo o "—" si no tiene
     */
    public String getNombreTorneo() {
        return torneo == null ? "—" : torneo.getNombreCompleto();
    }

    /**
     * Obtiene el equipo local.
     *
     * @return el equipo local
     */
    public Equipo getEquipoLocal() {
        return equipoLocal;
    }

    /**
     * Establece el equipo local.
     *
     * @param valor el equipo local
     * @throws IllegalArgumentException si el valor es nulo
     */
    public final void setEquipoLocal(Equipo valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo \"Equipo local\" es obligatorio.");
        }
        this.equipoLocal = valor;
    }

    /**
     * Obtiene el equipo visitante.
     *
     * @return el equipo visitante
     */
    public Equipo getEquipoVisitante() {
        return equipoVisitante;
    }

    /**
     * Establece el equipo visitante.
     *
     * @param valor el equipo visitante
     * @throws IllegalArgumentException si el valor es nulo
     */
    public final void setEquipoVisitante(Equipo valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo \"Equipo visitante\" es obligatorio.");
        }
        this.equipoVisitante = valor;
    }

    /**
     * Obtiene la fecha del partido.
     *
     * @return la fecha del partido
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha del partido.
     *
     * @param valor la fecha del partido
     * @throws IllegalArgumentException si el valor es nulo
     */
    public final void setFecha(LocalDate valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo \"Fecha\" es obligatorio.");
        }
        this.fecha = valor;
    }

    /**
     * Obtiene la hora del partido.
     *
     * @return la hora del partido
     */
    public LocalTime getHora() {
        return hora;
    }

    /**
     * Establece la hora del partido.
     *
     * @param valor la hora del partido
     * @throws IllegalArgumentException si el valor es nulo
     */
    public final void setHora(LocalTime valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo \"Hora\" es obligatorio.");
        }
        this.hora = valor;
    }

    /**
     * Obtiene el lugar donde se juega.
     *
     * @return el lugar donde se juega
     */
    public String getLugar() {
        return lugar;
    }

    /**
     * Establece el lugar donde se juega.
     *
     * @param valor el lugar donde se juega
     */
    public final void setLugar(String valor) {
        this.lugar = valor == null || valor.trim().isEmpty() ? "Por definir" : valor.trim();
    }

    /**
     * Obtiene el estado del partido.
     *
     * @return el estado del partido
     */
    public EstadoPartido getEstado() {
        return estado;
    }

    /**
     * Establece el estado del partido.
     *
     * @param valor el estado del partido
     * @throws IllegalArgumentException si el valor es nulo
     */
    public final void setEstado(EstadoPartido valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo \"Estado\" es obligatorio.");
        }
        this.estado = valor;
    }

    /**
     * Obtiene el resultado del partido ({@code null} mientras no se registre).
     *
     * @return el resultado del partido ({@code null} mientras no se registre)
     */
    public Resultado getResultado() {
        return resultado;
    }

    /**
     * Establece el resultado del partido ({@code null} mientras no se registre).
     *
     * @param valor el resultado del partido ({@code null} mientras no se registre)
     */
    public final void setResultado(Resultado valor) {
        this.resultado = valor;
    }

    /**
     * Obtiene el nombre del equipo local.
     *
     * @return nombre del equipo local
     */
    public String getNombreLocal() {
        return equipoLocal.getNombreEquipo();
    }

    /**
     * Obtiene el nombre del equipo visitante.
     *
     * @return nombre del equipo visitante
     */
    public String getNombreVisitante() {
        return equipoVisitante.getNombreEquipo();
    }

    /**
     * Obtiene el marcador en texto.
     *
     * @return marcador "local - visitante" o "—" si no hay resultado
     */
    public String getMarcador() {
        return resultado == null ? "—" : resultado.getGolesLocal() + " - " + resultado.getGolesVisitante();
    }

    /**
     * Compara dos objetos Partido únicamente por su identificador.
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
        return id == ((Partido) obj).id;
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
        return "Partido[id=" + id + ", torneo=" + getNombreTorneo() + ", jornada=" + jornada
                + ", equipoLocal=" + getNombreLocal() + ", equipoVisitante=" + getNombreVisitante()
                + ", fecha=" + fecha + ", hora=" + hora + ", lugar=" + lugar + ", estado=" + estado
                + ", resultado=" + getMarcador() + "]";
    }

}
