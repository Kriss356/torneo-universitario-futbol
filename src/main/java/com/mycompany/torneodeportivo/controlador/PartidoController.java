package com.mycompany.torneodeportivo.controlador;

import com.mycompany.torneodeportivo.modelo.Equipo;
import com.mycompany.torneodeportivo.modelo.EstadoPartido;
import com.mycompany.torneodeportivo.modelo.EstadoResultado;
import com.mycompany.torneodeportivo.modelo.Partido;
import com.mycompany.torneodeportivo.modelo.Torneo;
import com.mycompany.torneodeportivo.mysql.modelos.EPermisos;
import com.mycompany.torneodeportivo.servicio.Autorizacion;
import com.mycompany.torneodeportivo.servicio.DatosServicio;
import com.mycompany.torneodeportivo.util.Alertas;
import com.mycompany.torneodeportivo.util.CampoValidado;
import com.mycompany.torneodeportivo.util.Convertidores;
import com.mycompany.torneodeportivo.util.GestorFormulario;
import com.mycompany.torneodeportivo.util.Reglas;
import com.mycompany.torneodeportivo.util.Sesion;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class PartidoController extends FormularioBase<Partido> {

    @FXML private TableColumn<Partido, Integer> colId, colJornada;
    @FXML private TableColumn<Partido, String> colTorneo, colLocal, colVisitante, colLugar, colMarcador, colGanador;
    @FXML private TableColumn<Partido, LocalDate> colFecha;
    @FXML private TableColumn<Partido, LocalTime> colHora;
    @FXML private TableColumn<Partido, EstadoPartido> colEstado;

    @FXML private ComboBox<Torneo> cmbTorneo;
    @FXML private TextField txtJornada;
    @FXML private Label errTorneo, errJornada, lblPeriodoTorneo;
    @FXML private ComboBox<Equipo> cmbLocal, cmbVisitante;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora, txtLugar;
    @FXML private ComboBox<EstadoPartido> cmbEstado;
    @FXML private Label errLocal, errVisitante, errFecha, errHora, errLugar, errEstado;

    // Subformulario del resultado (se valida por separado)
    @FXML private TextField txtGolesLocal, txtGolesVisitante;
    @FXML private ComboBox<EstadoResultado> cmbEstadoResultado;
    @FXML private Label errGolesLocal, errGolesVisitante, errEstadoResultado;
    @FXML private Button btnRegistrar;

    private final GestorFormulario gestorResultado = new GestorFormulario(this::actualizarBotones);

    @FXML
    private void initialize() {
        DatosServicio servicio = DatosServicio.getInstancia();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTorneo.setCellValueFactory(new PropertyValueFactory<>("nombreTorneo"));
        colJornada.setCellValueFactory(new PropertyValueFactory<>("jornada"));
        colLocal.setCellValueFactory(new PropertyValueFactory<>("nombreLocal"));
        colVisitante.setCellValueFactory(new PropertyValueFactory<>("nombreVisitante"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colLugar.setCellValueFactory(new PropertyValueFactory<>("lugar"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colMarcador.setCellValueFactory(new PropertyValueFactory<>("marcador"));
        colGanador.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getResultado() == null ? "—" : d.getValue().getResultado().getGanadorTexto()));
        tabla.setItems(servicio.getPartidos());

        cmbTorneo.setItems(servicio.getTorneos());
        cmbTorneo.setConverter(Convertidores.torneos());
        // muestra el periodo del torneo elegido debajo del combo
        cmbTorneo.valueProperty().addListener((obs, antes, t) ->
                lblPeriodoTorneo.setText(t == null ? "" : "Periodo: " + t.getPeriodo()));
        cmbLocal.setItems(servicio.getEquipos());
        cmbLocal.setConverter(Convertidores.equipos());
        cmbVisitante.setItems(servicio.getEquipos());
        cmbVisitante.setConverter(Convertidores.equipos());
        cmbEstado.setItems(FXCollections.observableArrayList(EstadoPartido.values()));
        cmbEstadoResultado.setItems(FXCollections.observableArrayList(EstadoResultado.values()));
        dpFecha.setEditable(false); // la fecha se elige en el calendario

        // Formulario del partido
        gestor.agregar(cmbTorneo, errTorneo, cmbTorneo.valueProperty(), Reglas.seleccion(cmbTorneo.valueProperty()));
        CampoValidado jornada = gestor.agregarTexto(txtJornada, errJornada, () -> {
            String error = Reglas.entero(txtJornada, 1, 38).get();
            if (error != null) return error;
            Partido choque = servicio.buscarChoqueJornada(editando, // el partido que se edita no choca consigo mismo
                    cmbTorneo.getValue(), Integer.parseInt(txtJornada.getText().trim()),
                    cmbLocal.getValue(), cmbVisitante.getValue());
            if (choque != null) {
                return "En esa jornada ya juega " + choque.getNombreLocal() + " vs " + choque.getNombreVisitante() + ".";
            }
            return null;
        }, Reglas.CAR_ENTERO, 2, Reglas.MSG_ENTERO);
        // si cambia el torneo o algún equipo, se vuelve a revisar la jornada
        gestor.revalidarCuando(jornada, cmbTorneo.valueProperty());
        gestor.revalidarCuando(jornada, cmbLocal.valueProperty());
        gestor.revalidarCuando(jornada, cmbVisitante.valueProperty());
        gestor.agregar(cmbLocal, errLocal, cmbLocal.valueProperty(), Reglas.seleccion(cmbLocal.valueProperty()));
        CampoValidado visitante = gestor.agregar(cmbVisitante, errVisitante, cmbVisitante.valueProperty(), () -> {
            Equipo v = cmbVisitante.getValue();
            Equipo l = cmbLocal.getValue();
            if (v == null) return "Selecciona una opción.";
            if (l != null && v.equals(l)) return "El visitante debe ser distinto del local.";
            if (l != null && !v.getCategoria().equals(l.getCategoria())) {
                return "Debe ser de la misma categoría que el local (" + l.getNombreCategoria() + ").";
            }
            return null;
        });
        gestor.revalidarCuando(visitante, cmbLocal.valueProperty()); // si cambia el local, se revisa el visitante
        CampoValidado fecha = gestor.agregar(dpFecha, errFecha, dpFecha.valueProperty(), () -> {
            String error = Reglas.fecha(dpFecha.valueProperty()).get();
            if (error != null) return error;
            Torneo t = cmbTorneo.getValue();
            if (t != null && !t.incluyeFecha(dpFecha.getValue())) {
                return "Debe estar dentro del " + t.getTipo().getTexto() + " (" + t.getPeriodo() + ").";
            }
            return null;
        });
        gestor.revalidarCuando(fecha, cmbTorneo.valueProperty()); // si cambia el torneo, se revisa la fecha
        gestor.agregarTexto(txtHora, errHora, Reglas.hora(txtHora), Reglas.CAR_HORA, 5, Reglas.MSG_HORA);
        gestor.agregarTexto(txtLugar, errLugar, Reglas.textoGeneral(txtLugar, 3),
                Reglas.CAR_TEXTO, 40, Reglas.MSG_TEXTO);
        gestor.agregar(cmbEstado, errEstado, cmbEstado.valueProperty(), Reglas.seleccion(cmbEstado.valueProperty()));

        // Subformulario del resultado
        gestorResultado.agregarTexto(txtGolesLocal, errGolesLocal, Reglas.entero(txtGolesLocal, 0, 30),
                Reglas.CAR_ENTERO, 2, Reglas.MSG_ENTERO);
        gestorResultado.agregarTexto(txtGolesVisitante, errGolesVisitante, Reglas.entero(txtGolesVisitante, 0, 30),
                Reglas.CAR_ENTERO, 2, Reglas.MSG_ENTERO);
        gestorResultado.agregar(cmbEstadoResultado, errEstadoResultado, cmbEstadoResultado.valueProperty(),
                Reglas.seleccion(cmbEstadoResultado.valueProperty()));
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, antes, ahora) -> actualizarBotones());

        // Autorización: solo los roles con RESULTADOS_REGISTRAR capturan marcadores
        boolean puedeResultados = Sesion.puede(EPermisos.RESULTADOS_REGISTRAR);
        txtGolesLocal.setDisable(!puedeResultados);
        txtGolesVisitante.setDisable(!puedeResultados);
        cmbEstadoResultado.setDisable(!puedeResultados);

        iniciarFormulario();
    }

    /** Registrar resultado: guarda el marcador en la BD, finaliza el partido y actualiza la tabla de posiciones. */
    @FXML
    private void registrarResultado() {
        Partido p = tabla.getSelectionModel().getSelectedItem();
        if (p == null) {
            Alertas.error("Selecciona en la tabla el partido al que le quieres registrar el resultado.");
            return;
        }
        if (!gestorResultado.validarTodo()) {
            gestorResultado.enfocarPrimerInvalido();
            return;
        }
        try {
            Autorizacion.exigir(EPermisos.RESULTADOS_REGISTRAR);
            int golesLocal = Integer.parseInt(txtGolesLocal.getText().trim());
            int golesVisitante = Integer.parseInt(txtGolesVisitante.getText().trim());
            DatosServicio.getInstancia().registrarResultado(p, golesLocal, golesVisitante, cmbEstadoResultado.getValue());
            tabla.refresh();
            Alertas.info("Resultado guardado: " + p.getNombreLocal() + " " + p.getMarcador() + " "
                    + p.getNombreVisitante() + ".\nLa tabla de posiciones se actualizó.");
            limpiar();
        } catch (IllegalArgumentException ex) {
            Alertas.error(ex.getMessage());
        }
    }

    @Override
    protected void actualizarBotones() {
        super.actualizarBotones();
        if (btnRegistrar != null) {
            btnRegistrar.setDisable(!Sesion.puede(EPermisos.RESULTADOS_REGISTRAR)
                    || tabla.getSelectionModel().getSelectedItem() == null || !gestorResultado.esValido());
        }
    }

    /** Prefijo de los permisos de este módulo (tabla permiso). */
    @Override
    protected String modulo() {
        return "PARTIDOS";
    }

    @Override
    protected void limpiarCampos() {
        cmbTorneo.getSelectionModel().clearSelection();
        txtJornada.clear();
        cmbLocal.getSelectionModel().clearSelection();
        cmbVisitante.getSelectionModel().clearSelection();
        dpFecha.setValue(null);
        txtHora.clear();
        txtLugar.clear();
        cmbEstado.getSelectionModel().clearSelection();
        gestorResultado.silenciar(() -> {
            txtGolesLocal.clear();
            txtGolesVisitante.clear();
            cmbEstadoResultado.getSelectionModel().clearSelection();
        });
    }

    @Override
    protected void cargarCampos(Partido p) {
        cmbTorneo.setValue(p.getTorneo());
        txtJornada.setText(String.valueOf(p.getJornada()));
        cmbLocal.setValue(p.getEquipoLocal());
        cmbVisitante.setValue(p.getEquipoVisitante());
        dpFecha.setValue(p.getFecha());
        txtHora.setText(p.getHora().toString());
        txtLugar.setText(p.getLugar());
        cmbEstado.setValue(p.getEstado());
        gestorResultado.silenciar(() -> {
            if (p.getResultado() != null) {
                txtGolesLocal.setText(String.valueOf(p.getResultado().getGolesLocal()));
                txtGolesVisitante.setText(String.valueOf(p.getResultado().getGolesVisitante()));
                cmbEstadoResultado.setValue(p.getResultado().getEstado());
            } else {
                txtGolesLocal.clear();
                txtGolesVisitante.clear();
                cmbEstadoResultado.getSelectionModel().clearSelection();
            }
        });
    }

    @Override
    protected String describir(Partido p) {
        return p.getNombreLocal() + " vs " + p.getNombreVisitante() + " (jornada " + p.getJornada()
                + ", " + p.getFecha() + ")";
    }

    @Override
    protected void guardarRegistro(Partido editando) {
        DatosServicio servicio = DatosServicio.getInstancia();
        int jornada = Integer.parseInt(txtJornada.getText().trim());
        LocalTime hora = LocalTime.parse(txtHora.getText().trim());
        String lugar = txtLugar.getText().trim();
        if (editando == null) {
            servicio.agregarPartido(cmbTorneo.getValue(), jornada, cmbLocal.getValue(), cmbVisitante.getValue(),
                    dpFecha.getValue(), hora, lugar, cmbEstado.getValue());
        } else {
            servicio.actualizarPartido(editando, cmbTorneo.getValue(), jornada, cmbLocal.getValue(), cmbVisitante.getValue(),
                    dpFecha.getValue(), hora, lugar, cmbEstado.getValue());
        }
    }

    @Override
    protected void eliminarRegistro(Partido p) {
        DatosServicio.getInstancia().eliminarPartido(p);
    }
}
