package com.mycompany.torneodeportivo.util;

import com.mycompany.torneodeportivo.modelo.Posicion;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Exporta la tabla de posiciones a un archivo CSV que se puede abrir en Excel.
 * El archivo se escribe en UTF-8 con BOM para que Excel muestre bien los acentos.
 */
public final class ExportadorCSV {

    /** Encabezados de las columnas, en el mismo orden que la tabla de la vista. */
    public static final String ENCABEZADO = "Pos,Equipo,PJ,G,E,P,GF,GC,DG,Pts";

    private ExportadorCSV() { }

    /**
     * Escribe las posiciones en el archivo indicado.
     *
     * @param posiciones filas a exportar, en el orden en que se ven en la tabla
     * @param archivo    archivo destino (se sobrescribe si ya existe)
     * @throws IOException si no se puede escribir el archivo
     * @throws IllegalArgumentException si no hay posiciones o el archivo es nulo
     */
    public static void exportarPosiciones(List<Posicion> posiciones, File archivo) throws IOException {
        if (archivo == null) {
            throw new IllegalArgumentException("Selecciona dónde guardar el archivo.");
        }
        if (posiciones == null || posiciones.isEmpty()) {
            throw new IllegalArgumentException("No hay posiciones para exportar.");
        }
        try (BufferedWriter out = Files.newBufferedWriter(archivo.toPath(), StandardCharsets.UTF_8)) {
            out.write('﻿'); // BOM para Excel
            out.write(ENCABEZADO);
            out.newLine();
            int lugar = 1;
            for (Posicion p : posiciones) {
                out.write(lugar++ + "," + texto(p.getNombreEquipo()) + ","
                        + p.getPartidosJugados() + "," + p.getPartidosGanados() + ","
                        + p.getPartidosEmpatados() + "," + p.getPartidosPerdidos() + ","
                        + p.getGolesAFavor() + "," + p.getGolesEnContra() + ","
                        + p.getDiferenciaGoles() + "," + p.getPuntos());
                out.newLine();
            }
        }
    }

    /** Pone el texto entre comillas si trae comas, comillas o saltos de línea. */
    static String texto(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
