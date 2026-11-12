-- =====================================================================
--  CAMBIO: torneo y jornada en los partidos
--  Base de datos: torneo_futbol (XAMPP / phpMyAdmin)
--  Autor: Christian Hernandez Garcia
--  Ejecutar UNA sola vez, después de 01_crear_tablas.sql (y 02 / 03 si los usas).
-- =====================================================================
USE torneo_futbol;

-- ---------------------------------------------------------------------
-- 1. Tabla TORNEO (clase Torneo / enum EstadoTorneo)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS torneo (
    id_torneo      INT          NOT NULL AUTO_INCREMENT,
    nombre_torneo  VARCHAR(60)  NOT NULL,
    temporada      VARCHAR(30)  NOT NULL,
    estado         ENUM('EN_PREPARACION','EN_CURSO','PAUSADO','FINALIZADO') NOT NULL DEFAULT 'EN_PREPARACION',
    PRIMARY KEY (id_torneo),
    UNIQUE KEY uq_torneo_nombre_temporada (nombre_torneo, temporada)
) ENGINE=InnoDB COMMENT='Torneos o temporadas a los que pertenecen los partidos';

INSERT INTO torneo (nombre_torneo, temporada, estado) VALUES
    ('Torneo Universitario de Fútbol', 'Otoño 2026',     'EN_CURSO'),
    ('Copa Intertecnológicos',         'Primavera 2027', 'EN_PREPARACION');

-- ---------------------------------------------------------------------
-- 2. Nuevas columnas en PARTIDO: id_torneo y jornada
--    Primero se agregan permitiendo NULL para no romper los partidos que ya existen.
-- ---------------------------------------------------------------------
ALTER TABLE partido
    ADD COLUMN id_torneo INT              NULL AFTER id_partido,
    ADD COLUMN jornada   TINYINT UNSIGNED NOT NULL DEFAULT 1 AFTER id_torneo;

-- 3. Los partidos existentes se asignan al primer torneo
UPDATE partido SET id_torneo = 1 WHERE id_torneo IS NULL;
UPDATE partido SET jornada = 2 WHERE id_partido = 2;   -- datos de prueba: el 2.º partido es de la jornada 2

-- 4. Ahora sí: torneo obligatorio, llave foránea y rango de la jornada
ALTER TABLE partido
    MODIFY COLUMN id_torneo INT NOT NULL,
    ADD CONSTRAINT fk_partido_torneo FOREIGN KEY (id_torneo) REFERENCES torneo (id_torneo),
    ADD CONSTRAINT ck_partido_jornada CHECK (jornada BETWEEN 1 AND 38),
    ADD INDEX idx_partido_torneo_jornada (id_torneo, jornada);

-- ---------------------------------------------------------------------
-- 5. Comprobación: los partidos con su torneo y jornada
-- ---------------------------------------------------------------------
SELECT  p.id_partido,
        CONCAT(t.nombre_torneo, ' (', t.temporada, ')') AS torneo,
        p.jornada,
        el.nombre_equipo AS local,
        ev.nombre_equipo AS visitante,
        p.fecha, p.hora, p.estado
FROM partido p
JOIN torneo t  ON t.id_torneo  = p.id_torneo
JOIN equipo el ON el.id_equipo = p.id_equipo_local
JOIN equipo ev ON ev.id_equipo = p.id_equipo_visitante
ORDER BY t.id_torneo, p.jornada, p.fecha;
