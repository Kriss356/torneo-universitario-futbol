package com.mycompany.torneodeportivo.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Almacenamiento externo de la aplicación, fuera del proyecto y del JAR.
 * <pre>
 * Documentos/
 * └── TorneoUniversitarioFutbol/     carpeta principal
 *     ├── imagenes/                  logos de equipos y fotos de jugadores
 *     └── registro.log               bitácora (fecha, hora, operación y resultado)
 * </pre>
 * Las carpetas se crean automáticamente si no existen; si ya existen la aplicación
 * continúa normalmente. En la base de datos solo se guarda el NOMBRE del archivo de
 * la imagen y la ruta completa se arma aquí, así funciona en cualquier equipo o usuario.
 *
 * @author Christian Hernandez Garcia
 */
public final class Almacenamiento {

    public static final String NOMBRE_CARPETA = "TorneoUniversitarioFutbol";
    public static final String NOMBRE_IMAGENES = "imagenes";
    public static final String NOMBRE_LOG = "registro.log";

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static Path carpetaPrincipal;
    private static Path carpetaImagenes;
    private static Path archivoLog;

    private Almacenamiento() {
    }

    /**
     * Crea (si hace falta) la carpeta principal y la subcarpeta de imágenes en Documentos
     * y registra cada operación en el log. Se llama al iniciar la aplicación.
     */
    public static synchronized void inicializar() {
        Path documentos = carpetaDocumentos();
        carpetaPrincipal = documentos.resolve(NOMBRE_CARPETA);
        carpetaImagenes = carpetaPrincipal.resolve(NOMBRE_IMAGENES);
        archivoLog = carpetaPrincipal.resolve(NOMBRE_LOG);

        boolean existiaPrincipal = Files.isDirectory(carpetaPrincipal);
        boolean principalOk = crearCarpeta(carpetaPrincipal);
        // El log vive dentro de la carpeta principal: se escribe hasta que ésta existe
        registrar("INICIO_APLICACION", documentos.toString(), "Usuario del sistema: " + System.getProperty("user.name"));
        registrar("CREAR_CARPETA_PRINCIPAL", carpetaPrincipal.toString(),
                !principalOk ? "ERROR: no se pudo crear"
                        : existiaPrincipal ? "YA EXISTIA - se continua con el funcionamiento normal"
                        : "CREADA correctamente");
        if (principalOk) {
            boolean existiaImagenes = Files.isDirectory(carpetaImagenes);
            boolean imagenesOk = crearCarpeta(carpetaImagenes);
            registrar("CREAR_CARPETA_IMAGENES", carpetaImagenes.toString(),
                    !imagenesOk ? "ERROR: no se pudo crear"
                            : existiaImagenes ? "YA EXISTIA - se continua con el funcionamiento normal"
                            : "CREADA correctamente");
            migrarImagenesAnteriores();
        }
    }

    /** Carpeta principal: Documentos/TorneoUniversitarioFutbol */
    public static Path getCarpetaPrincipal() {
        asegurarInicio();
        return carpetaPrincipal;
    }

    /** Subcarpeta de imágenes: Documentos/TorneoUniversitarioFutbol/imagenes */
    public static Path getCarpetaImagenes() {
        asegurarInicio();
        if (!Files.isDirectory(carpetaImagenes)) {
            // Si alguien borró la carpeta con la app abierta, se vuelve a crear
            registrar("CREAR_CARPETA_IMAGENES", carpetaImagenes.toString(),
                    crearCarpeta(carpetaImagenes) ? "CREADA correctamente (no existia)" : "ERROR: no se pudo crear");
        }
        return carpetaImagenes;
    }

    /** Archivo de log: Documentos/TorneoUniversitarioFutbol/registro.log */
    public static Path getArchivoLog() {
        asegurarInicio();
        return archivoLog;
    }

    /**
     * Convierte lo guardado en la BD (nombre del archivo) en la ruta completa dentro de Documentos.
     * También acepta rutas viejas como "imagenes/abc.png": se toma solo el nombre del archivo.
     */
    public static Path resolverImagen(String guardado) {
        if (guardado == null || guardado.trim().isEmpty()) return null;
        Path nombre = Paths.get(guardado.trim().replace('\\', '/')).getFileName();
        return nombre == null ? null : getCarpetaImagenes().resolve(nombre.toString());
    }

    /** Escribe una línea en el log: fecha y hora | operación | ruta | resultado. */
    public static synchronized void registrar(String operacion, String ruta, String resultado) {
        String linea = LocalDateTime.now().format(FORMATO) + " | " + operacion + " | " + ruta + " | " + resultado
                + System.lineSeparator();
        if (archivoLog == null || !Files.isDirectory(archivoLog.getParent())) {
            System.err.print("[LOG] " + linea);
            return;
        }
        try {
            Files.write(archivoLog, linea.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            System.err.print("[LOG no escrito: " + ex.getMessage() + "] " + linea);
        }
    }

    // ---------- Apoyo ----------

    private static void asegurarInicio() {
        if (carpetaPrincipal == null) inicializar();
    }

    private static boolean crearCarpeta(Path carpeta) {
        try {
            Files.createDirectories(carpeta);
            return true;
        } catch (IOException ex) {
            System.err.println("No se pudo crear " + carpeta + ": " + ex.getMessage());
            return false;
        }
    }

    /**
     * Ubica la carpeta Documentos del usuario. En Windows normalmente es
     * C:/Users/usuario/Documents; si está redirigida a OneDrive también se detecta.
     */
    private static Path carpetaDocumentos() {
        Path home = Paths.get(System.getProperty("user.home"));
        Path[] candidatas = {
            home.resolve("Documents"),
            home.resolve("Documentos"),
            home.resolve("OneDrive").resolve("Documents"),
            home.resolve("OneDrive").resolve("Documentos")
        };
        for (Path p : candidatas) {
            if (Files.isDirectory(p)) return p;
        }
        return home.resolve("Documents"); // no existe ninguna: se crea junto con la carpeta principal
    }

    /**
     * Si existe la carpeta "imagenes" vieja (junto al proyecto, versión anterior de la app),
     * copia sus imágenes a Documentos para que los registros que ya estaban en la BD sigan mostrando su imagen.
     */
    private static void migrarImagenesAnteriores() {
        Path vieja = Paths.get(NOMBRE_IMAGENES).toAbsolutePath().normalize();
        if (!Files.isDirectory(vieja) || vieja.equals(carpetaImagenes.toAbsolutePath().normalize())) return;
        int copiadas = 0;
        try (DirectoryStream<Path> archivos = Files.newDirectoryStream(vieja)) {
            for (Path origen : archivos) {
                Path destino = carpetaImagenes.resolve(origen.getFileName().toString());
                if (Files.isRegularFile(origen) && !Files.exists(destino)) {
                    Files.copy(origen, destino);
                    copiadas++;
                }
            }
            if (copiadas > 0) {
                registrar("MIGRAR_IMAGENES", vieja + " -> " + carpetaImagenes, copiadas + " imagen(es) copiada(s)");
            }
        } catch (IOException ex) {
            registrar("MIGRAR_IMAGENES", vieja.toString(), "ERROR: " + ex.getMessage());
        }
    }
}
