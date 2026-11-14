package com.mycompany.torneodeportivo.servicio;

import com.mycompany.torneodeportivo.modelo.*;
import com.mycompany.torneodeportivo.mysql.daos.AdministradorSistemaDAO;
import com.mycompany.torneodeportivo.mysql.daos.CategoriaDAO;
import com.mycompany.torneodeportivo.mysql.daos.EquipoDAO;
import com.mycompany.torneodeportivo.mysql.daos.JugadorDAO;
import com.mycompany.torneodeportivo.mysql.daos.PartidoDAO;
import com.mycompany.torneodeportivo.mysql.daos.PosicionDAO;
import com.mycompany.torneodeportivo.mysql.daos.TorneoDAO;
import com.mycompany.torneodeportivo.util.Cifrado;
import com.mycompany.torneodeportivo.util.ImagenUtil;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Capa de servicio: contiene las reglas de negocio y guarda todo en la base de datos
 * torneo_futbol (MariaDB) por medio de los DAOs. Las listas observables son una copia
 * en memoria de lo que hay en la base, para mostrarla en las tablas de JavaFX.
 *
 * Patrón de cada operación: 1) validar reglas, 2) guardar en la BD con el DAO,
 * 3) solo si la BD lo aceptó, actualizar la lista en memoria.
 * Todas las reglas lanzan IllegalArgumentException con un mensaje para el usuario.
 *
 * @author Christian Hernandez Garcia
 */
public class DatosServicio {

    private static final DatosServicio INSTANCIA = new DatosServicio();
    public static DatosServicio getInstancia() { return INSTANCIA; }

    private final ObservableList<Torneo> torneos = FXCollections.observableArrayList();
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private final ObservableList<Equipo> equipos = FXCollections.observableArrayList();
    private final ObservableList<Jugador> jugadores = FXCollections.observableArrayList();
    private final ObservableList<Partido> partidos = FXCollections.observableArrayList();
    private final ObservableList<Posicion> posiciones = FXCollections.observableArrayList();
    private final ObservableList<Administrador> administradores = FXCollections.observableArrayList();

    private final TorneoDAO torneoDAO = new TorneoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final EquipoDAO equipoDAO = new EquipoDAO();
    private final JugadorDAO jugadorDAO = new JugadorDAO();
    private final PartidoDAO partidoDAO = new PartidoDAO();
    private final PosicionDAO posicionDAO = new PosicionDAO();
    private final AdministradorSistemaDAO administradorDAO = new AdministradorSistemaDAO();

    private DatosServicio() {
    }

    // ---------- Acceso a las listas ----------
    public ObservableList<Torneo> getTorneos() { return torneos; }
    public ObservableList<Categoria> getCategorias() { return categorias; }
    public ObservableList<Equipo> getEquipos() { return equipos; }
    public ObservableList<Jugador> getJugadores() { return jugadores; }
    public ObservableList<Partido> getPartidos() { return partidos; }
    public ObservableList<Posicion> getPosiciones() { return posiciones; }
    public ObservableList<Administrador> getAdministradores() { return administradores; }

    /**
     * Lee toda la información desde la base de datos. Se llama al iniciar sesión.
     *
     * @throws IllegalArgumentException si no hay conexión o falta alguna tabla
     */
    public void cargarDesdeBD() {
        List<Administrador> admins = administradorDAO.listar();
        List<Torneo> tors = torneoDAO.listar();
        Map<Integer, Torneo> mapaTorneos = new HashMap<>();
        for (Torneo t : tors) mapaTorneos.put(t.getId(), t);
        List<Categoria> cats = categoriaDAO.listar();
        Map<Integer, Categoria> mapaCategorias = new HashMap<>();
        for (Categoria c : cats) mapaCategorias.put(c.getId(), c);

        List<Equipo> eqs = equipoDAO.listar(mapaCategorias);
        Map<Integer, Equipo> mapaEquipos = new HashMap<>();
        for (Equipo e : eqs) mapaEquipos.put(e.getId(), e);

        List<Jugador> jugs = jugadorDAO.listar(mapaEquipos);
        List<Partido> parts = partidoDAO.listar(mapaTorneos, mapaEquipos);
        posicionDAO.crearFaltantes();
        List<Posicion> pos = posicionDAO.listar(mapaEquipos);

        administradores.setAll(admins);
        torneos.setAll(tors);
        categorias.setAll(cats);
        equipos.setAll(eqs);
        jugadores.setAll(jugs);
        partidos.setAll(parts);
        posiciones.setAll(pos);
        recalcularPosiciones(); // la tabla siempre coincide con los resultados guardados
    }

    // ---------- Administradores (tabla administrador) ----------
    public Administrador agregarAdministrador(String nombre, int edad, String correo, String usuario,
                                              String contrasena, String rol, EstadoAdministrador estado) {
        validarAdministrador(null, correo, usuario);
        Administrador a = crearAdministrador(0, nombre, edad, correo, usuario, Cifrado.sha256(contrasena), rol, null);
        a.setEstado(estado);
        a.setId(administradorDAO.insertar(a));
        administradores.add(a);
        return a;
    }

    /**
     * Actualiza un administrador. Si contrasenaNueva viene vacía se conserva la actual.
     * Como el rol define la subclase, se crea un objeto nuevo y se reemplaza en la lista.
     */
    public Administrador actualizarAdministrador(Administrador actual, String nombre, int edad, String correo,
                                                 String usuario, String contrasenaNueva, String rol,
                                                 EstadoAdministrador estado) {
        validarAdministrador(actual, correo, usuario);
        asegurarAdministradorTorneo(actual,
                AdministradorSistemaDAO.ROL_TORNEO.equals(rol) && estado == EstadoAdministrador.ACTIVO);
        String clave = (contrasenaNueva == null || contrasenaNueva.isEmpty())
                ? actual.getContrasena() : Cifrado.sha256(contrasenaNueva);
        Administrador copia = crearAdministrador(actual.getId(), nombre, edad, correo, usuario, clave, rol, actual);
        copia.setEstado(estado);
        administradorDAO.actualizar(copia);
        administradores.set(administradores.indexOf(actual), copia);
        return copia;
    }

    public void eliminarAdministrador(Administrador a) {
        asegurarAdministradorTorneo(a, false);
        administradorDAO.eliminar(a.getId());
        administradores.remove(a);
    }

    /**
     * Como no hay formulario de registro, siempre debe quedar al menos una cuenta ACTIVA de
     * Administrador del torneo para poder iniciar sesión y crear cuentas.
     *
     * @param cuenta              cuenta que se va a modificar o eliminar
     * @param seguiraSiendoActivo true si después del cambio seguirá siendo Administrador del torneo ACTIVO
     */
    private void asegurarAdministradorTorneo(Administrador cuenta, boolean seguiraSiendoActivo) {
        if (seguiraSiendoActivo || !(cuenta instanceof AdministradorTorneo)
                || cuenta.getEstado() != EstadoAdministrador.ACTIVO) {
            return;
        }
        boolean hayOtro = administradores.stream().anyMatch(a -> !a.equals(cuenta)
                && a instanceof AdministradorTorneo && a.getEstado() == EstadoAdministrador.ACTIVO);
        if (!hayOtro) {
            throw new IllegalArgumentException("Debe existir al menos una cuenta ACTIVA de Administrador del torneo.\n"
                    + "No se puede eliminar, desactivar ni cambiar de rol a la última.");
        }
    }

    private void validarAdministrador(Administrador actual, String correo, String usuario) {
        for (Administrador a : administradores) {
            if (a == actual) continue;
            if (a.getUsuario().equalsIgnoreCase(usuario)) {
                throw new IllegalArgumentException("Ya existe un administrador con el usuario \"" + usuario + "\".");
            }
            if (a.getCorreo().equalsIgnoreCase(correo)) {
                throw new IllegalArgumentException("Ya existe un administrador con el correo \"" + correo + "\".");
            }
        }
    }

    /** Crea la subclase según el rol; si no cambia el rol, conserva su dato propio. */
    private Administrador crearAdministrador(int id, String nombre, int edad, String correo, String usuario,
                                             String clave, String rol, Administrador anterior) {
        if (AdministradorSistemaDAO.ROL_EQUIPOS.equals(rol)) {
            String seccion = anterior instanceof AdministradorEquipos
                    ? ((AdministradorEquipos) anterior).getSeccionAsignada() : "Equipos y jugadores";
            return new AdministradorEquipos(id, nombre, edad, correo, usuario, clave, seccion);
        } else if (AdministradorSistemaDAO.ROL_PARTIDOS.equals(rol)) {
            String turno = anterior instanceof AdministradorPartidos
                    ? ((AdministradorPartidos) anterior).getTurnoAsignado() : "Matutino";
            return new AdministradorPartidos(id, nombre, edad, correo, usuario, clave, turno);
        } else if (AdministradorSistemaDAO.ROL_TORNEO.equals(rol)) {
            String nivel = anterior instanceof AdministradorTorneo
                    ? ((AdministradorTorneo) anterior).getNivelPermiso() : "Total";
            return new AdministradorTorneo(id, nombre, edad, correo, usuario, clave, nivel);
        }
        throw new IllegalArgumentException("Selecciona un rol válido.");
    }

    // ---------- Torneos ----------
    public Torneo agregarTorneo(String nombre, TipoTorneo tipo, LocalDate inicio, LocalDate fin, EstadoTorneo estado) {
        for (Torneo t : torneos) {
            if (t.getNombreTorneo().equalsIgnoreCase(nombre) && t.getTipo() == tipo
                    && t.getFechaInicio().getYear() == inicio.getYear()) {
                throw new IllegalArgumentException("Ya existe el " + tipo.getTexto() + " " + inicio.getYear()
                        + " de ese torneo.");
            }
        }
        Torneo t = new Torneo(0, nombre, tipo, inicio, fin);
        t.setEstado(estado);
        t.setId(torneoDAO.insertar(t));
        torneos.add(t);
        return t;
    }

    // ---------- Categorías ----------
    public Categoria agregarCategoria(String nombre, int min, int max, EstadoCategoria estado) {
        validarCategoria(null, nombre, min, max);
        Categoria c = new Categoria(0, nombre, min, max);
        c.setEstado(estado);
        c.setId(categoriaDAO.insertar(c));
        categorias.add(c);
        return c;
    }

    public void actualizarCategoria(Categoria c, String nombre, int min, int max, EstadoCategoria estado) {
        validarCategoria(c, nombre, min, max);
        for (Jugador j : jugadores) {
            if (j.getEquipo().getCategoria() == c && (j.getEdad() < min || j.getEdad() > max)) {
                throw new IllegalArgumentException("El jugador " + j.getNombre()
                        + " (" + j.getEdad() + " años) quedaría fuera del nuevo rango.");
            }
        }
        Categoria copia = new Categoria(c.getId(), nombre, min, max);
        copia.setEstado(estado);
        categoriaDAO.actualizar(copia);
        // la base aceptó el cambio: se aplica en memoria
        c.setNombreCategoria(nombre);
        c.setEdadMinima(min);
        c.setEdadMaxima(max);
        c.setEstado(estado);
    }

    public void eliminarCategoria(Categoria c) {
        for (Equipo e : equipos) {
            if (e.getCategoria() == c) {
                throw new IllegalArgumentException("No se puede eliminar: hay equipos en esta categoría.");
            }
        }
        categoriaDAO.eliminar(c.getId());
        categorias.remove(c);
    }

    private void validarCategoria(Categoria actual, String nombre, int min, int max) {
        if (min > max) throw new IllegalArgumentException("La edad mínima no puede ser mayor que la máxima.");
        for (Categoria c : categorias) {
            if (c != actual && c.getNombreCategoria().equalsIgnoreCase(nombre)) {
                throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
            }
        }
    }

    // ---------- Equipos ----------
    public Equipo agregarEquipo(String nombre, String universidad, String entrenador,
                                Categoria categoria, String rutaLogo, EstadoEquipo estado) {
        validarEquipo(null, nombre);
        Equipo e = new Equipo(0, nombre, universidad, categoria);
        e.setEntrenador(entrenador);
        e.setRutaLogo(rutaLogo);
        e.setEstado(estado);
        int[] ids = equipoDAO.insertar(e); // inserta el equipo y su fila de posiciones
        e.setId(ids[0]);
        equipos.add(e);
        posiciones.add(new Posicion(ids[1], e));
        return e;
    }

    public void actualizarEquipo(Equipo e, String nombre, String universidad, String entrenador,
                                 Categoria categoria, String rutaLogo, EstadoEquipo estado) {
        validarEquipo(e, nombre);
        for (Jugador j : jugadoresDe(e)) {
            if (!categoria.admiteEdad(j.getEdad())) {
                throw new IllegalArgumentException("El jugador " + j.getNombre()
                        + " no cumple el rango de edad de la categoría elegida.");
            }
        }
        if (categoria != e.getCategoria() && tienePartidos(e)) {
            throw new IllegalArgumentException("No se puede cambiar la categoría: el equipo ya tiene partidos registrados.");
        }
        String logoAnterior = e.getRutaLogo();
        Equipo copia = new Equipo(e.getId(), nombre, universidad, categoria);
        copia.setEntrenador(entrenador);
        copia.setRutaLogo(rutaLogo);
        copia.setEstado(estado);
        equipoDAO.actualizar(copia);
        e.setNombreEquipo(nombre);
        e.setUniversidad(universidad);
        e.setEntrenador(entrenador);
        e.setCategoria(categoria);
        e.setRutaLogo(rutaLogo);
        e.setEstado(estado);
        // Si se cambió el logo, se borra el archivo anterior de Documentos
        if (logoAnterior != null && !logoAnterior.equals(rutaLogo)) ImagenUtil.borrar(logoAnterior);
    }

    public void eliminarEquipo(Equipo e) {
        if (!jugadoresDe(e).isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar: el equipo tiene jugadores registrados.");
        }
        if (tienePartidos(e)) {
            throw new IllegalArgumentException("No se puede eliminar: el equipo tiene partidos programados.");
        }
        equipoDAO.eliminar(e.getId());
        equipos.remove(e);
        posiciones.removeIf(p -> p.getEquipo() == e);
        ImagenUtil.borrar(e.getRutaLogo());
    }

    private boolean tienePartidos(Equipo e) {
        for (Partido p : partidos) {
            if (p.getEquipoLocal() == e || p.getEquipoVisitante() == e) return true;
        }
        return false;
    }

    private void validarEquipo(Equipo actual, String nombre) {
        for (Equipo e : equipos) {
            if (e != actual && e.getNombreEquipo().equalsIgnoreCase(nombre)) {
                throw new IllegalArgumentException("Ya existe un equipo con ese nombre.");
            }
        }
    }

    // ---------- Jugadores ----------
    public List<Jugador> jugadoresDe(Equipo e) {
        return jugadores.stream().filter(j -> j.getEquipo() == e).collect(Collectors.toList());
    }

    public Jugador agregarJugador(String nombre, int edad, int numero, PosicionCampo posicion,
                                  Equipo equipo, String rutaFoto, EstadoJugador estado) {
        validarJugador(null, edad, numero, equipo);
        Jugador j = new Jugador(0, nombre, edad, numero, posicion, equipo);
        j.setRutaFoto(rutaFoto);
        j.setEstado(estado);
        j.setId(jugadorDAO.insertar(j));
        jugadores.add(j);
        return j;
    }

    public void actualizarJugador(Jugador j, String nombre, int edad, int numero, PosicionCampo posicion,
                                  Equipo equipo, String rutaFoto, EstadoJugador estado) {
        validarJugador(j, edad, numero, equipo);
        String fotoAnterior = j.getRutaFoto();
        Jugador copia = new Jugador(j.getId(), nombre, edad, numero, posicion, equipo);
        copia.setRutaFoto(rutaFoto);
        copia.setEstado(estado);
        jugadorDAO.actualizar(copia);
        j.setNombre(nombre);
        j.setEdad(edad);
        j.setNumeroCamiseta(numero);
        j.setPosicionCampo(posicion);
        j.setEquipo(equipo);
        j.setRutaFoto(rutaFoto);
        j.setEstado(estado);
        // Si se cambió la foto, se borra el archivo anterior de Documentos
        if (fotoAnterior != null && !fotoAnterior.equals(rutaFoto)) ImagenUtil.borrar(fotoAnterior);
    }

    public void eliminarJugador(Jugador j) {
        jugadorDAO.eliminar(j.getId());
        jugadores.remove(j);
        ImagenUtil.borrar(j.getRutaFoto());
    }

    private void validarJugador(Jugador actual, int edad, int numero, Equipo equipo) {
        Categoria c = equipo.getCategoria();
        if (!c.admiteEdad(edad)) {
            throw new IllegalArgumentException("La edad " + edad + " no corresponde a la categoría "
                    + c.getNombreCategoria() + " (" + c.getEdadMinima() + "-" + c.getEdadMaxima() + ").");
        }
        for (Jugador j : jugadoresDe(equipo)) {
            if (j != actual && j.getNumeroCamiseta() == numero) {
                throw new IllegalArgumentException("El número " + numero + " ya lo usa " + j.getNombre() + ".");
            }
        }
    }

    // ---------- Partidos y resultados ----------
    public Partido agregarPartido(Torneo torneo, int jornada, Equipo local, Equipo visitante, LocalDate fecha,
                                  LocalTime hora, String lugar, EstadoPartido estado) {
        validarPartido(local, visitante);
        validarJornada(null, torneo, jornada, fecha, local, visitante);
        if (estado == EstadoPartido.FINALIZADO) {
            throw new IllegalArgumentException("Un partido nuevo no puede estar Finalizado: "
                    + "guárdalo y después registra su resultado.");
        }
        Partido p = new Partido(0, local, visitante, fecha, hora);
        p.setTorneo(torneo);
        p.setJornada(jornada);
        p.setLugar(lugar);
        p.setEstado(estado);
        p.setId(partidoDAO.insertar(p));
        partidos.add(p);
        return p;
    }

    public void actualizarPartido(Partido p, Torneo torneo, int jornada, Equipo local, Equipo visitante,
                                  LocalDate fecha, LocalTime hora, String lugar, EstadoPartido estado) {
        validarPartido(local, visitante);
        validarJornada(p, torneo, jornada, fecha, local, visitante);
        if (p.getResultado() != null) {
            if (local != p.getEquipoLocal() || visitante != p.getEquipoVisitante()) {
                throw new IllegalArgumentException("No se pueden cambiar los equipos de un partido con resultado.");
            }
            if (estado != EstadoPartido.FINALIZADO) {
                throw new IllegalArgumentException("Un partido con resultado debe permanecer Finalizado.");
            }
        } else if (estado == EstadoPartido.FINALIZADO) {
            throw new IllegalArgumentException("Para finalizar el partido usa \"Registrar resultado\".");
        }
        Partido copia = new Partido(p.getId(), local, visitante, fecha, hora);
        copia.setTorneo(torneo);
        copia.setJornada(jornada);
        copia.setLugar(lugar);
        copia.setEstado(estado);
        partidoDAO.actualizar(copia);
        p.setTorneo(torneo);
        p.setJornada(jornada);
        p.setEquipoLocal(local);
        p.setEquipoVisitante(visitante);
        p.setFecha(fecha);
        p.setHora(hora);
        p.setLugar(lugar);
        p.setEstado(estado);
    }

    public void eliminarPartido(Partido p) {
        partidoDAO.eliminar(p.getId()); // su resultado se borra en cascada
        partidos.remove(p);
        recalcularPosiciones();
    }

    /** Guarda el marcador, marca el partido como Finalizado y actualiza la tabla de posiciones. */
    public void registrarResultado(Partido p, int golesLocal, int golesVisitante, EstadoResultado estado) {
        if (golesLocal < 0 || golesVisitante < 0) {
            throw new IllegalArgumentException("Los goles no pueden ser negativos.");
        }
        if (p.getEstado() == EstadoPartido.SUSPENDIDO) {
            throw new IllegalArgumentException("No se puede registrar el resultado de un partido Suspendido.");
        }
        Resultado r = new Resultado(0, golesLocal, golesVisitante);
        r.determinarGanador(p.getEquipoLocal(), p.getEquipoVisitante());
        r.setEstado(estado);
        r.setId(partidoDAO.guardarResultado(p.getId(), r));
        p.setResultado(r);
        p.setEstado(EstadoPartido.FINALIZADO);
        recalcularPosiciones();
    }

    private void validarPartido(Equipo local, Equipo visitante) {
        if (local == visitante) throw new IllegalArgumentException("El equipo local y el visitante deben ser distintos.");
        if (local.getCategoria() != visitante.getCategoria()) {
            throw new IllegalArgumentException("Ambos equipos deben ser de la misma categoría.");
        }
        for (Equipo e : new Equipo[]{local, visitante}) {
            if (e.getEstado() == EstadoEquipo.ELIMINADO || e.getEstado() == EstadoEquipo.RETIRADO) {
                throw new IllegalArgumentException("El equipo " + e.getNombreEquipo() + " está " + e.getEstado() + ".");
            }
        }
    }

    /**
     * Busca un partido del mismo torneo y jornada en el que ya juegue alguno de los dos equipos.
     *
     * @param actual partido que se está editando (se ignora) o {@code null} si es nuevo
     * @return el partido en conflicto o {@code null} si no hay
     */
    public Partido buscarChoqueJornada(Partido actual, Torneo torneo, int jornada, Equipo local, Equipo visitante) {
        if (torneo == null) return null;
        for (Partido p : partidos) {
            if (p == actual || !torneo.equals(p.getTorneo()) || p.getJornada() != jornada) continue;
            for (Equipo e : new Equipo[]{local, visitante}) {
                if (e != null && (p.getEquipoLocal() == e || p.getEquipoVisitante() == e)) return p;
            }
        }
        return null;
    }

    private void validarJornada(Partido actual, Torneo torneo, int jornada, LocalDate fecha,
                                Equipo local, Equipo visitante) {
        if (torneo == null) throw new IllegalArgumentException("Selecciona el torneo del partido.");
        if (!torneo.incluyeFecha(fecha)) {
            throw new IllegalArgumentException("La fecha debe estar dentro del " + torneo.getTipo().getTexto()
                    + " (" + torneo.getPeriodo() + ").");
        }
        if (jornada < 1 || jornada > 38) throw new IllegalArgumentException("La jornada debe estar entre 1 y 38.");
        Partido choque = buscarChoqueJornada(actual, torneo, jornada, local, visitante);
        if (choque != null) {
            throw new IllegalArgumentException("En la jornada " + jornada + " ya está el partido "
                    + choque.getNombreLocal() + " vs " + choque.getNombreVisitante() + ".");
        }
    }

    // ---------- Tabla de posiciones ----------
    /** Recalcula la tabla a partir de los resultados y la guarda en la tabla "posicion" de la BD. */
    public void recalcularPosiciones() {
        for (Posicion pos : posiciones) pos.reiniciar();
        for (Partido p : partidos) {
            Resultado r = p.getResultado();
            if (r == null) continue;
            Posicion l = buscarPosicion(p.getEquipoLocal());
            Posicion v = buscarPosicion(p.getEquipoVisitante());
            if (l != null) l.registrar(r.getGolesLocal(), r.getGolesVisitante());
            if (v != null) v.registrar(r.getGolesVisitante(), r.getGolesLocal());
        }
        Comparator<Posicion> orden = Comparator.comparingInt(Posicion::getPuntos).reversed()
                .thenComparing(Comparator.comparingInt(Posicion::getDiferenciaGoles).reversed())
                .thenComparing(Comparator.comparingInt(Posicion::getGolesAFavor).reversed());
        FXCollections.sort(posiciones, orden);
        posicionDAO.guardarTodas(posiciones);
    }

    private Posicion buscarPosicion(Equipo e) {
        for (Posicion p : posiciones) if (p.getEquipo() == e) return p;
        return null;
    }
}
