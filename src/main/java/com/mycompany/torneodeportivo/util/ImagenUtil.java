package com.mycompany.torneodeportivo.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Estrategia de almacenamiento: la imagen elegida se COPIA a
 * Documentos/TorneoUniversitarioFutbol/imagenes con un nombre único (UUID)
 * y en la BD solo se guarda el nombre del archivo (ver {@link Almacenamiento}).
 * Así el registro no depende de la ubicación original, del proyecto ni del lugar donde esté el JAR.
 *
 * @author Christian Hernandez Garcia
 */
public class ImagenUtil {

    public static File elegir(Window ventana) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar imagen");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        return fc.showOpenDialog(ventana);
    }

    /** Registro / actualización: copia la imagen a Documentos y regresa el nombre que se guarda en la BD. */
    public static String guardar(File origen) throws IOException {
        Path carpeta = Almacenamiento.getCarpetaImagenes();
        String nombre = origen.getName();
        String ext = nombre.contains(".") ? nombre.substring(nombre.lastIndexOf('.')).toLowerCase() : "";
        String nuevo = UUID.randomUUID() + ext;
        Path destino = carpeta.resolve(nuevo);
        try {
            Files.copy(origen.toPath(), destino);
            Almacenamiento.registrar("GUARDAR_IMAGEN", destino.toString(), "OK (origen: " + origen.getName() + ")");
        } catch (IOException ex) {
            Almacenamiento.registrar("GUARDAR_IMAGEN", destino.toString(), "ERROR: " + ex.getMessage());
            throw ex;
        }
        return nuevo;
    }

    /** Consulta: carga la imagen desde Documentos (acepta también rutas viejas "imagenes/..."). */
    public static Image cargar(String ruta) {
        Path p = Almacenamiento.resolverImagen(ruta);
        if (p == null || !Files.exists(p)) return null;
        return new Image(p.toUri().toString(), 140, 140, true, true);
    }

    /** Eliminación: borra el archivo de la carpeta de imágenes en Documentos. */
    public static void borrar(String ruta) {
        Path p = Almacenamiento.resolverImagen(ruta);
        if (p == null) return;
        try {
            boolean borrado = Files.deleteIfExists(p);
            Almacenamiento.registrar("ELIMINAR_IMAGEN", p.toString(), borrado ? "OK" : "NO EXISTIA");
        } catch (IOException ex) {
            Almacenamiento.registrar("ELIMINAR_IMAGEN", p.toString(), "ERROR: " + ex.getMessage());
        }
    }
}
