-- =====================================================================
--  PREPARAR LA BASE torneo_futbol PARA QUE TODA LA APP GUARDE EN LA BD
--  Autor: Christian Hernandez Garcia
--
--  Es SEGURO ejecutarlo aunque ya tengas datos: NO borra nada.
--   * Crea solo las tablas y columnas que falten (incluye torneo y jornada).
--   * Funciona hayas ejecutado o no 04_torneo_y_jornada.sql y 05_torneo_tipo_y_fechas.sql.
--   * Inserta los roles y los datos de prueba solo si no existen.
--   * Cifra con SHA-256 las contraseñas que estén en texto plano.
--  Se puede ejecutar varias veces.
--
--  phpMyAdmin: seleccionar la base torneo_futbol > pestaña SQL > pegar > Continuar.
--  (o pestaña Importar > elegir este archivo > Importar)
-- =====================================================================
CREATE DATABASE IF NOT EXISTS torneo_futbol
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE torneo_futbol;

-- ---------------------------------------------------------------------
-- TABLAS (mismo diseño que 01_crear_tablas.sql)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rol (
    id_rol      INT          NOT NULL AUTO_INCREMENT,
    nombre_rol  VARCHAR(40)  NOT NULL,
    PRIMARY KEY (id_rol),
    UNIQUE KEY uq_rol_nombre (nombre_rol)
) ENGINE=InnoDB COMMENT='Catálogo de roles de administrador';

CREATE TABLE IF NOT EXISTS administrador (
    id_administrador  INT           NOT NULL AUTO_INCREMENT,
    nombre            VARCHAR(60)   NOT NULL,
    edad              TINYINT UNSIGNED NOT NULL,
    correo            VARCHAR(60)   NOT NULL,
    usuario           VARCHAR(20)   NOT NULL,
    contrasena        VARCHAR(255)  NOT NULL COMMENT 'Se guarda cifrada (hash), nunca en texto plano',
    estado            ENUM('ACTIVO','INACTIVO','SUSPENDIDO') NOT NULL DEFAULT 'ACTIVO',
    id_rol            INT           NOT NULL,
    seccion_asignada  VARCHAR(40)   NULL,
    turno_asignado    VARCHAR(40)   NULL,
    nivel_permiso     VARCHAR(40)   NULL,
    PRIMARY KEY (id_administrador),
    UNIQUE KEY uq_admin_correo  (correo),
    UNIQUE KEY uq_admin_usuario (usuario),
    CONSTRAINT fk_admin_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol),
    CONSTRAINT ck_admin_edad CHECK (edad BETWEEN 18 AND 70)
) ENGINE=InnoDB COMMENT='Administradores del sistema (3 roles)';

CREATE TABLE IF NOT EXISTS categoria (
    id_categoria     INT          NOT NULL AUTO_INCREMENT,
    nombre_categoria VARCHAR(30)  NOT NULL,
    edad_minima      TINYINT UNSIGNED NOT NULL,
    edad_maxima      TINYINT UNSIGNED NOT NULL,
    estado           ENUM('ABIERTA','EN_COMPETENCIA','CERRADA') NOT NULL DEFAULT 'ABIERTA',
    PRIMARY KEY (id_categoria),
    UNIQUE KEY uq_categoria_nombre (nombre_categoria),
    CONSTRAINT ck_categoria_rango CHECK (edad_minima BETWEEN 1 AND 100
                                     AND edad_maxima BETWEEN 1 AND 100
                                     AND edad_minima <= edad_maxima)
) ENGINE=InnoDB COMMENT='Categorías por rango de edad';

CREATE TABLE IF NOT EXISTS equipo (
    id_equipo     INT          NOT NULL AUTO_INCREMENT,
    nombre_equipo VARCHAR(40)  NOT NULL,
    universidad   VARCHAR(60)  NOT NULL,
    entrenador    VARCHAR(40)  NULL,
    id_categoria  INT          NOT NULL,
    ruta_logo     VARCHAR(255) NULL,
    estado        ENUM('INSCRITO','CLASIFICADO','ELIMINADO','RETIRADO') NOT NULL DEFAULT 'INSCRITO',
    PRIMARY KEY (id_equipo),
    UNIQUE KEY uq_equipo_nombre (nombre_equipo),
    CONSTRAINT fk_equipo_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
) ENGINE=InnoDB COMMENT='Equipos universitarios participantes';

CREATE TABLE IF NOT EXISTS jugador (
    id_jugador      INT          NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(60)  NOT NULL,
    edad            TINYINT UNSIGNED NOT NULL,
    numero_camiseta TINYINT UNSIGNED NOT NULL,
    posicion_campo  ENUM('PORTERO','DEFENSA','MEDIOCAMPISTA','DELANTERO') NOT NULL,
    id_equipo       INT          NOT NULL,
    ruta_foto       VARCHAR(255) NULL,
    estado          ENUM('HABILITADO','SANCIONADO','LESIONADO') NOT NULL DEFAULT 'HABILITADO',
    PRIMARY KEY (id_jugador),
    UNIQUE KEY uq_jugador_camiseta (id_equipo, numero_camiseta),
    CONSTRAINT fk_jugador_equipo FOREIGN KEY (id_equipo) REFERENCES equipo (id_equipo),
    CONSTRAINT ck_jugador_edad     CHECK (edad BETWEEN 15 AND 45),
    CONSTRAINT ck_jugador_camiseta CHECK (numero_camiseta BETWEEN 1 AND 99)
) ENGINE=InnoDB COMMENT='Jugadores inscritos en un equipo';

CREATE TABLE IF NOT EXISTS partido (
    id_partido          INT         NOT NULL AUTO_INCREMENT,
    id_equipo_local     INT         NOT NULL,
    id_equipo_visitante INT         NOT NULL,
    fecha               DATE        NOT NULL,
    hora                TIME        NOT NULL,
    lugar               VARCHAR(40) NOT NULL DEFAULT 'Por definir',
    estado              ENUM('PROGRAMADO','EN_JUEGO','FINALIZADO','SUSPENDIDO') NOT NULL DEFAULT 'PROGRAMADO',
    PRIMARY KEY (id_partido),
    CONSTRAINT fk_partido_local     FOREIGN KEY (id_equipo_local)     REFERENCES equipo (id_equipo),
    CONSTRAINT fk_partido_visitante FOREIGN KEY (id_equipo_visitante) REFERENCES equipo (id_equipo),
    CONSTRAINT ck_partido_equipos_distintos CHECK (id_equipo_local <> id_equipo_visitante)
) ENGINE=InnoDB COMMENT='Partidos programados';

CREATE TABLE IF NOT EXISTS resultado (
    id_resultado      INT      NOT NULL AUTO_INCREMENT,
    id_partido        INT      NOT NULL,
    goles_local       TINYINT UNSIGNED NOT NULL,
    goles_visitante   TINYINT UNSIGNED NOT NULL,
    id_equipo_ganador INT      NULL,
    estado            ENUM('REGISTRADO','EN_REVISION','CONFIRMADO') NOT NULL DEFAULT 'REGISTRADO',
    PRIMARY KEY (id_resultado),
    UNIQUE KEY uq_resultado_partido (id_partido),
    CONSTRAINT fk_resultado_partido FOREIGN KEY (id_partido) REFERENCES partido (id_partido)
        ON DELETE CASCADE,
    CONSTRAINT fk_resultado_ganador FOREIGN KEY (id_equipo_ganador) REFERENCES equipo (id_equipo),
    CONSTRAINT ck_resultado_goles CHECK (goles_local BETWEEN 0 AND 99 AND goles_visitante BETWEEN 0 AND 99)
) ENGINE=InnoDB COMMENT='Marcador de cada partido jugado';

CREATE TABLE IF NOT EXISTS posicion (
    id_posicion        INT      NOT NULL AUTO_INCREMENT,
    id_equipo          INT      NOT NULL,
    partidos_jugados   SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    partidos_ganados   SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    partidos_empatados SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    partidos_perdidos  SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    goles_a_favor      SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    goles_en_contra    SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    estado             ENUM('ACTUALIZADO','CONGELADO') NOT NULL DEFAULT 'ACTUALIZADO',
    PRIMARY KEY (id_posicion),
    UNIQUE KEY uq_posicion_equipo (id_equipo),
    CONSTRAINT fk_posicion_equipo FOREIGN KEY (id_equipo) REFERENCES equipo (id_equipo)
        ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Estadísticas de cada equipo en la tabla de posiciones';

-- ---------------------------------------------------------------------
-- TORNEO (Apertura / Clausura con su periodo) y su relación con PARTIDO
-- (lo mismo que hacen 04_torneo_y_jornada.sql y 05_torneo_tipo_y_fechas.sql)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS torneo (
    id_torneo      INT          NOT NULL AUTO_INCREMENT,
    nombre_torneo  VARCHAR(60)  NOT NULL,
    tipo           ENUM('APERTURA','CLAUSURA') NOT NULL DEFAULT 'APERTURA',
    fecha_inicio   DATE         NOT NULL,
    fecha_fin      DATE         NOT NULL,
    estado         ENUM('EN_PREPARACION','EN_CURSO','PAUSADO','FINALIZADO') NOT NULL DEFAULT 'EN_PREPARACION',
    PRIMARY KEY (id_torneo),
    UNIQUE KEY uq_torneo_nombre_tipo_inicio (nombre_torneo, tipo, fecha_inicio),
    CONSTRAINT ck_torneo_periodo CHECK (fecha_inicio <= fecha_fin)
) ENGINE=InnoDB COMMENT='Torneos (Apertura / Clausura) a los que pertenecen los partidos';

-- Si se ejecutó el 04 pero no el 05, la tabla tiene "temporada" y le faltan tipo y fechas
ALTER TABLE torneo
    ADD COLUMN IF NOT EXISTS tipo ENUM('APERTURA','CLAUSURA') NOT NULL DEFAULT 'APERTURA' AFTER nombre_torneo,
    ADD COLUMN IF NOT EXISTS fecha_inicio DATE NULL AFTER tipo,
    ADD COLUMN IF NOT EXISTS fecha_fin    DATE NULL AFTER fecha_inicio;
UPDATE torneo SET nombre_torneo = 'Torneo Universitario de Fútbol', tipo = 'APERTURA',
       fecha_inicio = '2026-08-03', fecha_fin = '2026-12-12', estado = 'EN_CURSO'
 WHERE id_torneo = 1 AND fecha_inicio IS NULL;
UPDATE torneo SET nombre_torneo = 'Torneo Universitario de Fútbol', tipo = 'CLAUSURA',
       fecha_inicio = '2027-01-11', fecha_fin = '2027-05-29', estado = 'EN_PREPARACION'
 WHERE id_torneo = 2 AND fecha_inicio IS NULL;
UPDATE torneo SET fecha_inicio = CURDATE(), fecha_fin = CURDATE() + INTERVAL 6 MONTH WHERE fecha_inicio IS NULL;
ALTER TABLE torneo
    DROP INDEX IF EXISTS uq_torneo_nombre_temporada,
    DROP COLUMN IF EXISTS temporada,
    MODIFY COLUMN fecha_inicio DATE NOT NULL,
    MODIFY COLUMN fecha_fin    DATE NOT NULL,
    ADD UNIQUE KEY IF NOT EXISTS uq_torneo_nombre_tipo_inicio (nombre_torneo, tipo, fecha_inicio);

-- Torneos de prueba (solo si no hay ninguno)
INSERT INTO torneo (nombre_torneo, tipo, fecha_inicio, fecha_fin, estado)
SELECT * FROM (SELECT 'Torneo Universitario de Fútbol' AS n, 'APERTURA' AS t, DATE('2026-08-03') AS i, DATE('2026-12-12') AS f, 'EN_CURSO' AS e
               UNION ALL SELECT 'Torneo Universitario de Fútbol', 'CLAUSURA', DATE('2027-01-11'), DATE('2027-05-29'), 'EN_PREPARACION') d
WHERE NOT EXISTS (SELECT 1 FROM torneo);

-- Columnas id_torneo y jornada en PARTIDO (si aún no existen)
ALTER TABLE partido
    ADD COLUMN IF NOT EXISTS id_torneo INT              NULL AFTER id_partido,
    ADD COLUMN IF NOT EXISTS jornada   TINYINT UNSIGNED NOT NULL DEFAULT 1 AFTER id_torneo;
UPDATE partido SET id_torneo = (SELECT MIN(id_torneo) FROM torneo) WHERE id_torneo IS NULL;
ALTER TABLE partido
    MODIFY COLUMN id_torneo INT NOT NULL,
    ADD INDEX IF NOT EXISTS idx_partido_torneo_jornada (id_torneo, jornada),
    ADD CONSTRAINT fk_partido_torneo FOREIGN KEY IF NOT EXISTS (id_torneo) REFERENCES torneo (id_torneo);

CREATE OR REPLACE VIEW v_tabla_posiciones AS
SELECT  c.nombre_categoria,
        e.nombre_equipo,
        p.partidos_jugados   AS pj,
        p.partidos_ganados   AS pg,
        p.partidos_empatados AS pe,
        p.partidos_perdidos  AS pp,
        p.goles_a_favor      AS gf,
        p.goles_en_contra    AS gc,
        (CAST(p.goles_a_favor AS SIGNED) - CAST(p.goles_en_contra AS SIGNED)) AS dg,
        (p.partidos_ganados * 3 + p.partidos_empatados)        AS puntos
FROM posicion p
JOIN equipo e    ON e.id_equipo = p.id_equipo
JOIN categoria c ON c.id_categoria = e.id_categoria
ORDER BY c.nombre_categoria, puntos DESC, dg DESC, gf DESC;

-- ---------------------------------------------------------------------
-- ROLES (los nombres deben ser exactamente estos; la app los usa)
-- ---------------------------------------------------------------------
INSERT IGNORE INTO rol (nombre_rol) VALUES
    ('Administrador de equipos'),
    ('Administrador de partidos'),
    ('Administrador del torneo');

-- ---------------------------------------------------------------------
-- USUARIOS PARA INICIAR SESIÓN (contraseña: 1234), solo si no existen
-- ---------------------------------------------------------------------
INSERT IGNORE INTO administrador (nombre, edad, correo, usuario, contrasena, estado, id_rol, seccion_asignada, turno_asignado, nivel_permiso)
SELECT 'Ana Torres', 35, 'ana@uni.mx', 'admin.equipos', SHA2('1234',256), 'ACTIVO', id_rol, 'Equipos y jugadores', NULL, NULL
FROM rol WHERE nombre_rol = 'Administrador de equipos';
INSERT IGNORE INTO administrador (nombre, edad, correo, usuario, contrasena, estado, id_rol, seccion_asignada, turno_asignado, nivel_permiso)
SELECT 'Luis Mora', 40, 'luis@uni.mx', 'admin.partidos', SHA2('1234',256), 'ACTIVO', id_rol, NULL, 'Matutino', NULL
FROM rol WHERE nombre_rol = 'Administrador de partidos';
INSERT IGNORE INTO administrador (nombre, edad, correo, usuario, contrasena, estado, id_rol, seccion_asignada, turno_asignado, nivel_permiso)
SELECT 'Sofía Ruiz', 38, 'sofia@uni.mx', 'admin.torneo', SHA2('1234',256), 'ACTIVO', id_rol, NULL, NULL, 'Total'
FROM rol WHERE nombre_rol = 'Administrador del torneo';

-- Si alguna contraseña quedó en texto plano, se cifra (un SHA-256 siempre mide 64 caracteres)
UPDATE administrador SET contrasena = SHA2(contrasena, 256) WHERE CHAR_LENGTH(contrasena) <> 64;

-- ---------------------------------------------------------------------
-- DATOS DE PRUEBA (solo si las tablas están vacías)
-- ---------------------------------------------------------------------
INSERT INTO categoria (nombre_categoria, edad_minima, edad_maxima, estado)
SELECT * FROM (SELECT 'Sub-20' AS n, 17 AS mi, 20 AS ma, 'ABIERTA' AS es
               UNION ALL SELECT 'Libre', 21, 30, 'ABIERTA') d
WHERE NOT EXISTS (SELECT 1 FROM categoria);

INSERT INTO equipo (nombre_equipo, universidad, entrenador, id_categoria, estado)
SELECT d.n, d.u, d.en, c.id_categoria, 'INSCRITO'
FROM (SELECT 'Leones Negros' AS n, 'Universidad de Guadalajara' AS u, 'Carlos Méndez' AS en, 'Sub-20' AS cat
      UNION ALL SELECT 'Halcones UTJ', 'Universidad Tecnológica de Jalisco', 'Laura Pérez', 'Sub-20'
      UNION ALL SELECT 'Titanes', 'Universidad Panamericana', 'Jorge Ramírez', 'Libre'
      UNION ALL SELECT 'Dragones', 'Universidad del Valle de Atemajac', 'Marta Solís', 'Libre') d
JOIN categoria c ON c.nombre_categoria = d.cat
WHERE NOT EXISTS (SELECT 1 FROM equipo);

INSERT INTO jugador (nombre, edad, numero_camiseta, posicion_campo, id_equipo, estado)
SELECT d.n, d.ed, d.num, d.pos, e.id_equipo, d.es
FROM (SELECT 'Diego Ramírez' AS n, 18 AS ed, 9 AS num, 'DELANTERO' AS pos, 'Leones Negros' AS eq, 'HABILITADO' AS es
      UNION ALL SELECT 'Mateo Hernández', 19, 1, 'PORTERO', 'Leones Negros', 'HABILITADO'
      UNION ALL SELECT 'Luis Ortega', 20, 10, 'MEDIOCAMPISTA', 'Halcones UTJ', 'HABILITADO'
      UNION ALL SELECT 'Andrés Castro', 18, 4, 'DEFENSA', 'Halcones UTJ', 'LESIONADO'
      UNION ALL SELECT 'Ricardo Flores', 24, 7, 'DELANTERO', 'Titanes', 'HABILITADO'
      UNION ALL SELECT 'Pablo Núñez', 22, 3, 'DEFENSA', 'Titanes', 'HABILITADO'
      UNION ALL SELECT 'Emilio Vargas', 27, 5, 'MEDIOCAMPISTA', 'Dragones', 'SANCIONADO') d
JOIN equipo e ON e.nombre_equipo = d.eq
WHERE NOT EXISTS (SELECT 1 FROM jugador);

-- Cada equipo necesita su fila en la tabla de posiciones
INSERT INTO posicion (id_equipo)
SELECT e.id_equipo FROM equipo e
WHERE NOT EXISTS (SELECT 1 FROM posicion p WHERE p.id_equipo = e.id_equipo);

-- Revisión rápida: debe mostrar cuántos registros hay en cada tabla
SELECT 'rol' AS tabla, COUNT(*) AS registros FROM rol
UNION ALL SELECT 'torneo', COUNT(*) FROM torneo
UNION ALL SELECT 'administrador', COUNT(*) FROM administrador
UNION ALL SELECT 'categoria', COUNT(*) FROM categoria
UNION ALL SELECT 'equipo', COUNT(*) FROM equipo
UNION ALL SELECT 'jugador', COUNT(*) FROM jugador
UNION ALL SELECT 'partido', COUNT(*) FROM partido
UNION ALL SELECT 'resultado', COUNT(*) FROM resultado
UNION ALL SELECT 'posicion', COUNT(*) FROM posicion;
