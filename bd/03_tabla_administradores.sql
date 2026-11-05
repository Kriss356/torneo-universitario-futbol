-- =====================================================================
--  Actividad: CRUD de administradores (JavaFX + DAO + MariaDB)
--  Agrega la tabla ADMINISTRADORES a la base torneo_futbol (NO borra nada).
--  Ejecutar en phpMyAdmin: seleccionar la BD torneo_futbol > pestaña SQL.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS torneo_futbol
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE torneo_futbol;

DROP TABLE IF EXISTS administradores;
CREATE TABLE administradores (
    id            INT          NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(60)  NOT NULL,
    telefono      VARCHAR(10)  NULL,
    email         VARCHAR(60)  NOT NULL,
    direccion     VARCHAR(100) NULL,
    usuario       VARCHAR(20)  NOT NULL,
    clave_acceso  VARCHAR(255) NOT NULL,
    ruta_imagen   VARCHAR(255) NULL,
    rol           ENUM('ADMIN_EQUIPOS','ADMIN_PARTIDOS','ADMIN_TORNEO') NOT NULL DEFAULT 'ADMIN_EQUIPOS',
    estado        ENUM('ACTIVO','INACTIVO','SUSPENDIDO') NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id),
    UNIQUE KEY uq_administradores_email   (email),
    UNIQUE KEY uq_administradores_usuario (usuario)
) ENGINE=InnoDB COMMENT='Administradores del torneo (CRUD desde JavaFX)';

INSERT INTO administradores (nombre, telefono, email, direccion, usuario, clave_acceso, ruta_imagen, rol, estado) VALUES
 ('Laura Méndez',  '3312345678', 'laura@uni.mx',  'Av. Juárez 120, Col. Centro',  'lmendez', '1234', NULL, 'ADMIN_EQUIPOS',   'ACTIVO'),
 ('Carlos Pérez',  '3398765432', 'carlos@uni.mx', 'Calle Hidalgo 45, Col. Norte', 'cperez',  '1234', NULL, 'ADMIN_PARTIDOS', 'ACTIVO'),
 ('Sofía Ruiz',    '3355512345', 'sofia@uni.mx',  'Blvd. Universidad 300',        'sruiz',   '1234', NULL, 'ADMIN_TORNEO',   'ACTIVO');
