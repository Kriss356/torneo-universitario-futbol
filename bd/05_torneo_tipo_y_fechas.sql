-- =====================================================================
--  CAMBIO: el torneo indica si es APERTURA o CLAUSURA y su periodo
--          (fecha de inicio y fecha de fin). Se quita la columna "temporada".
--  Base de datos: torneo_futbol (XAMPP / phpMyAdmin)
--  Autor: Christian Hernandez Garcia
--  Ejecutar UNA sola vez, DESPUÉS de 04_torneo_y_jornada.sql
-- =====================================================================
USE torneo_futbol;

-- 1. Nuevas columnas (primero aceptan NULL para no romper los registros existentes)
ALTER TABLE torneo
    ADD COLUMN tipo         ENUM('APERTURA','CLAUSURA') NOT NULL DEFAULT 'APERTURA' AFTER nombre_torneo,
    ADD COLUMN fecha_inicio DATE NULL AFTER tipo,
    ADD COLUMN fecha_fin    DATE NULL AFTER fecha_inicio;

-- 2. Datos de los torneos que ya existen
UPDATE torneo
   SET nombre_torneo = 'Torneo Universitario de Fútbol', tipo = 'APERTURA',
       fecha_inicio = '2026-08-03', fecha_fin = '2026-12-12', estado = 'EN_CURSO'
 WHERE id_torneo = 1;

UPDATE torneo
   SET nombre_torneo = 'Torneo Universitario de Fútbol', tipo = 'CLAUSURA',
       fecha_inicio = '2027-01-11', fecha_fin = '2027-05-29', estado = 'EN_PREPARACION'
 WHERE id_torneo = 2;

-- 3. Fechas obligatorias, se quita "temporada" y se agregan las reglas
ALTER TABLE torneo
    DROP INDEX uq_torneo_nombre_temporada,
    DROP COLUMN temporada,
    MODIFY COLUMN fecha_inicio DATE NOT NULL,
    MODIFY COLUMN fecha_fin    DATE NOT NULL,
    ADD UNIQUE KEY uq_torneo_nombre_tipo_inicio (nombre_torneo, tipo, fecha_inicio),
    ADD CONSTRAINT ck_torneo_periodo CHECK (fecha_inicio <= fecha_fin);

-- 4. Comprobación: partidos con su torneo, periodo y jornada
SELECT  p.id_partido,
        CONCAT(t.tipo, ' ', YEAR(t.fecha_inicio), ' - ', t.nombre_torneo) AS torneo,
        t.fecha_inicio, t.fecha_fin,
        p.jornada,
        el.nombre_equipo AS local,
        ev.nombre_equipo AS visitante,
        p.fecha,
        IF(p.fecha BETWEEN t.fecha_inicio AND t.fecha_fin, 'Sí', 'NO') AS dentro_del_torneo
FROM partido p
JOIN torneo t  ON t.id_torneo  = p.id_torneo
JOIN equipo el ON el.id_equipo = p.id_equipo_local
JOIN equipo ev ON ev.id_equipo = p.id_equipo_visitante
ORDER BY t.fecha_inicio, p.jornada;
