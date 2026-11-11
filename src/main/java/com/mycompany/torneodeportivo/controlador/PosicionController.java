package com.mycompany.torneodeportivo.controlador;

import com.mycompany.torneodeportivo.modelo.Categoria;
import com.mycompany.torneodeportivo.modelo.Posicion;
import com.mycompany.torneodeportivo.mysql.modelos.EPermisos;
import com.mycompany.torneodeportivo.servicio.Autorizacion;
import com.mycompany.torneodeportivo.servicio.DatosServicio;
import com.mycompany.torneodeportivo.util.Alertas;
import com.mycompany.torneodeportivo.util.Convertidores;
import com.mycompany.torneodeportivo.util.Sesion;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/** Tabla de posiciones: solo lectura, se calcula sola a partir de los resultados. */
public class PosicionController {

    @FXML private TableView<Posicion> tabla;
    @FXML private TableColumn<Posicion, Integer> colPos, colPJ, colG, colE, colP, colGF, colGC, colDG, colPts;
    @FXML private TableColumn<Posicion, String> colEquipo;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private Button btnActualizar;
    @FXML private Label lblResumen;

    private final DatosServicio servicio = DatosServicio.getInstancia();
    private FilteredList<Posicion> filtrada;

    @FXML
    private void initialize() {
        filtrada = new FilteredList<>(servicio.getPosiciones(), p -> true);
        tabla.setItems(filtrada);
        cmbCategoria.setItems(servicio.getCategorias());
        cmbCategoria.setConverter(Convertidores.categorias());
        cmbCategoria.valueProperty().addListener((obs, anterior, nueva) -> {
            filtrada.setPredicate(p -> nueva == null || p.getEquipo().getCategoria() == nueva);
            tabla.refresh();
        });

        colPos.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(tabla.getItems().indexOf(d.getValue()) + 1));
        colEquipo.setCellValueFactory(new PropertyValueFactory<>("nombreEquipo"));
        colPJ.setCellValueFactory(new PropertyValueFactory<>("partidosJugados"));
        colG.setCellValueFactory(new PropertyValueFactory<>("partidosGanados"));
        colE.setCellValueFactory(new PropertyValueFactory<>("partidosEmpatados"));
        colP.setCellValueFactory(new PropertyValueFactory<>("partidosPerdidos"));
        colGF.setCellValueFactory(new PropertyValueFactory<>("golesAFavor"));
        colGC.setCellValueFactory(new PropertyValueFactory<>("golesEnContra"));
        colDG.setCellValueFactory(new PropertyValueFactory<>("diferenciaGoles"));
        colPts.setCellValueFactory(new PropertyValueFactory<>("puntos"));

        // Autorización: recalcular la tabla requiere el permiso POSICIONES_RECALCULAR
        boolean puedeRecalcular = Sesion.puede(EPermisos.POSICIONES_RECALCULAR);
        btnActualizar.setVisible(puedeRecalcular);
        btnActualizar.setManaged(puedeRecalcular);
        actualizarResumen();
    }

    @FXML
    private void mostrarTodas() { cmbCategoria.getSelectionModel().clearSelection(); }

    @FXML
    private void actualizarTabla() {
        try {
            Autorizacion.exigir(EPermisos.POSICIONES_RECALCULAR);
            servicio.recalcularPosiciones(); // recalcula y guarda en la tabla "posicion" de la BD
            tabla.refresh();
            actualizarResumen();
            Alertas.info("Tabla de posiciones actualizada y guardada en la base de datos.");
        } catch (IllegalArgumentException ex) {
            Alertas.error(ex.getMessage());
        }
    }

    private void actualizarResumen() {
        long conResultado = servicio.getPartidos().stream().filter(p -> p.getResultado() != null).count();
        int goles = servicio.getPartidos().stream().filter(p -> p.getResultado() != null)
                .mapToInt(p -> p.getResultado().getGolesLocal() + p.getResultado().getGolesVisitante()).sum();
        lblResumen.setText("Equipos: " + servicio.getEquipos().size()
                + "   |   Jugadores: " + servicio.getJugadores().size()
                + "   |   Partidos con resultado: " + conResultado
                + "   |   Goles totales: " + goles);
    }
}
