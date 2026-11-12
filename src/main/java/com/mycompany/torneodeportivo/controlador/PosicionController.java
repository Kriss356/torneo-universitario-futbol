package com.mycompany.torneodeportivo.controlador;

import com.mycompany.torneodeportivo.modelo.Categoria;
import com.mycompany.torneodeportivo.modelo.Posicion;
import com.mycompany.torneodeportivo.mysql.modelos.EPermisos;
import com.mycompany.torneodeportivo.servicio.Autorizacion;
import com.mycompany.torneodeportivo.servicio.DatosServicio;
import com.mycompany.torneodeportivo.util.Alertas;
import com.mycompany.torneodeportivo.util.Convertidores;
import com.mycompany.torneodeportivo.util.ExportadorCSV;
import com.mycompany.torneodeportivo.util.Sesion;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

/** Tabla de posiciones: solo lectura, se calcula sola a partir de los resultados. */
public class PosicionController {

    @FXML private TableView<Posicion> tabla;
    @FXML private TableColumn<Posicion, Integer> colPos, colPJ, colG, colE, colP, colGF, colGC, colDG, colPts;
    @FXML private TableColumn<Posicion, String> colEquipo;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private Button btnActualizar, btnExportar;
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

    /** Exporta a CSV lo que se ve en la tabla (respeta el filtro de categoría). */
    @FXML
    private void exportarCSV() {
        try {
            Autorizacion.exigir(EPermisos.POSICIONES_VER);
            if (tabla.getItems().isEmpty()) {
                Alertas.error("No hay posiciones para exportar. Registra resultados o cambia el filtro de categoría.");
                return;
            }
            FileChooser selector = new FileChooser();
            selector.setTitle("Guardar tabla de posiciones");
            selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo CSV (*.csv)", "*.csv"));
            Categoria categoria = cmbCategoria.getValue();
            String sufijo = categoria == null ? "todas" : categoria.getNombreCategoria().replaceAll("[^\\p{L}0-9]+", "_");
            selector.setInitialFileName("posiciones_" + sufijo + "_" + LocalDate.now() + ".csv");
            File archivo = selector.showSaveDialog(tabla.getScene().getWindow());
            if (archivo == null) {
                return; // el usuario canceló
            }
            if (!archivo.getName().toLowerCase().endsWith(".csv")) {
                archivo = new File(archivo.getParentFile(), archivo.getName() + ".csv");
            }
            ExportadorCSV.exportarPosiciones(tabla.getItems(), archivo);
            Alertas.informacion("Exportación completa", "Se exportaron " + tabla.getItems().size()
                    + " equipos a:\n" + archivo.getAbsolutePath());
        } catch (IllegalArgumentException ex) {
            Alertas.error(ex.getMessage());
        } catch (IOException ex) {
            Alertas.error("No se pudo guardar el archivo: " + ex.getMessage());
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
