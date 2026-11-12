-- =====================================================================
--  AUTENTICACIÓN Y AUTORIZACIÓN (login, perfiles, permisos y sesiones)
--  Autor: Christian Hernandez Garcia
--  Materia: Programación Orientada a Objetos - Universidad Tecnológica de Jalisco
--
--  Requisito: haber ejecutado antes bd/06_preparar_base.sql (tablas rol y administrador).
--
--  Qué hace (es SEGURO, no borra datos y se puede ejecutar varias veces):
--   1. Agrega la descripción (responsabilidades) a cada uno de los 3 roles.
--   2. Crea el catálogo de permisos (tabla permiso).
--   3. Crea la relación rol <-> permiso (tabla rol_permiso) y asigna los permisos de cada perfil.
--   4. Crea la bitácora de inicios y cierres de sesión (tabla sesion).
--   5. Garantiza que exista al menos una cuenta ACTIVA de Administrador del torneo
--      (no hay formulario de registro: las cuentas las crea ese administrador).
--   6. Crea una cuenta INACTIVA para probar que no puede iniciar sesión.
--   7. Crea la vista v_permisos_por_rol para consultar qué puede hacer cada perfil.
--
--  phpMyAdmin: seleccionar la base torneo_futbol > pestaña SQL > pegar > Continuar.
-- =====================================================================
USE torneo_futbol;

-- ---------------------------------------------------------------------
-- 1. ROLES (perfiles) con su descripción
-- ---------------------------------------------------------------------
INSERT IGNORE INTO rol (nombre_rol) VALUES
    ('Administrador de equipos'),
    ('Administrador de partidos'),
    ('Administrador del torneo');

ALTER TABLE rol
    ADD COLUMN IF NOT EXISTS descripcion VARCHAR(255) NULL AFTER nombre_rol;

UPDATE rol SET descripcion = 'Responsable general del torneo. Tiene acceso total: administra las cuentas de usuario, categorías, equipos, jugadores, partidos, resultados y la tabla de posiciones.'
 WHERE nombre_rol = 'Administrador del torneo';
UPDATE rol SET descripcion = 'Responsable de las inscripciones. Registra, consulta, actualiza y elimina categorías, equipos y jugadores. Solo consulta partidos y posiciones.'
 WHERE nombre_rol = 'Administrador de equipos';
UPDATE rol SET descripcion = 'Responsable del calendario y de los marcadores. Registra, consulta, actualiza y elimina partidos y registra resultados. Solo consulta categorías, equipos, jugadores y posiciones.'
 WHERE nombre_rol = 'Administrador de partidos';

-- ---------------------------------------------------------------------
-- 2. CATÁLOGO DE PERMISOS (la clave debe coincidir con el enum EPermisos de Java)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS permiso (
    id_permiso   INT          NOT NULL AUTO_INCREMENT,
    clave        VARCHAR(40)  NOT NULL,
    modulo       VARCHAR(30)  NOT NULL,
    descripcion  VARCHAR(120) NOT NULL,
    PRIMARY KEY (id_permiso),
    UNIQUE KEY uq_permiso_clave (clave)
) ENGINE=InnoDB COMMENT='Acciones que se pueden autorizar dentro de la aplicación';

INSERT IGNORE INTO permiso (clave, modulo, descripcion) VALUES
    ('CATEGORIAS_VER',          'Categorías',       'Consultar categorías'),
    ('CATEGORIAS_CREAR',        'Categorías',       'Registrar categorías'),
    ('CATEGORIAS_EDITAR',       'Categorías',       'Actualizar categorías'),
    ('CATEGORIAS_ELIMINAR',     'Categorías',       'Eliminar categorías'),
    ('EQUIPOS_VER',             'Equipos',          'Consultar equipos'),
    ('EQUIPOS_CREAR',           'Equipos',          'Registrar equipos'),
    ('EQUIPOS_EDITAR',          'Equipos',          'Actualizar equipos'),
    ('EQUIPOS_ELIMINAR',        'Equipos',          'Eliminar equipos'),
    ('JUGADORES_VER',           'Jugadores',        'Consultar jugadores'),
    ('JUGADORES_CREAR',         'Jugadores',        'Registrar jugadores'),
    ('JUGADORES_EDITAR',        'Jugadores',        'Actualizar jugadores'),
    ('JUGADORES_ELIMINAR',      'Jugadores',        'Eliminar jugadores'),
    ('PARTIDOS_VER',            'Partidos',         'Consultar partidos'),
    ('PARTIDOS_CREAR',          'Partidos',         'Programar partidos'),
    ('PARTIDOS_EDITAR',         'Partidos',         'Actualizar partidos'),
    ('PARTIDOS_ELIMINAR',       'Partidos',         'Eliminar partidos'),
    ('RESULTADOS_REGISTRAR',    'Partidos',         'Registrar resultados de partidos'),
    ('POSICIONES_VER',          'Posiciones',       'Consultar la tabla de posiciones'),
    ('POSICIONES_RECALCULAR',   'Posiciones',       'Recalcular la tabla de posiciones'),
    ('ADMINISTRADORES_VER',     'Administradores',  'Consultar cuentas de usuario'),
    ('ADMINISTRADORES_CREAR',   'Administradores',  'Registrar cuentas de usuario'),
    ('ADMINISTRADORES_EDITAR',  'Administradores',  'Actualizar cuentas de usuario'),
    ('ADMINISTRADORES_ELIMINAR','Administradores',  'Eliminar cuentas de usuario');

-- ---------------------------------------------------------------------
-- 3. PERMISOS DE CADA PERFIL (relación muchos a muchos)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rol_permiso (
    id_rol      INT NOT NULL,
    id_permiso  INT NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    CONSTRAINT fk_rp_rol     FOREIGN KEY (id_rol)     REFERENCES rol (id_rol)         ON DELETE CASCADE,
    CONSTRAINT fk_rp_permiso FOREIGN KEY (id_permiso) REFERENCES permiso (id_permiso) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Qué permisos tiene cada rol';

-- Administrador del torneo: TODOS los permisos
INSERT IGNORE INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r CROSS JOIN permiso p
WHERE r.nombre_rol = 'Administrador del torneo';

-- Administrador de equipos: CRUD de categorías, equipos y jugadores + consultar partidos y posiciones
INSERT IGNORE INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r JOIN permiso p
  ON p.clave IN ('CATEGORIAS_VER','CATEGORIAS_CREAR','CATEGORIAS_EDITAR','CATEGORIAS_ELIMINAR',
                 'EQUIPOS_VER','EQUIPOS_CREAR','EQUIPOS_EDITAR','EQUIPOS_ELIMINAR',
                 'JUGADORES_VER','JUGADORES_CREAR','JUGADORES_EDITAR','JUGADORES_ELIMINAR',
                 'PARTIDOS_VER','POSICIONES_VER')
WHERE r.nombre_rol = 'Administrador de equipos';

-- Administrador de partidos: CRUD de partidos + registrar resultados + consultar lo demás
INSERT IGNORE INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r JOIN permiso p
  ON p.clave IN ('PARTIDOS_VER','PARTIDOS_CREAR','PARTIDOS_EDITAR','PARTIDOS_ELIMINAR','RESULTADOS_REGISTRAR',
                 'CATEGORIAS_VER','EQUIPOS_VER','JUGADORES_VER','POSICIONES_VER')
WHERE r.nombre_rol = 'Administrador de partidos';

-- ---------------------------------------------------------------------
-- 4. BITÁCORA DE SESIONES (inicio y cierre de sesión)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sesion (
    id_sesion         INT       NOT NULL AUTO_INCREMENT,
    id_administrador  INT       NOT NULL,
    fecha_inicio      DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre      DATETIME  NULL,
    estado            ENUM('ABIERTA','CERRADA') NOT NULL DEFAULT 'ABIERTA',
    PRIMARY KEY (id_sesion),
    KEY idx_sesion_admin (id_administrador),
    CONSTRAINT fk_sesion_admin FOREIGN KEY (id_administrador) REFERENCES administrador (id_administrador)
        ON DELETE CASCADE,
    CONSTRAINT ck_sesion_fechas CHECK (fecha_cierre IS NULL OR fecha_cierre >= fecha_inicio)
) ENGINE=InnoDB COMMENT='Registro de cada inicio y cierre de sesión';

-- ---------------------------------------------------------------------
-- 5. CUENTAS (contraseña de todas: 1234, guardada con SHA-256)
-- ---------------------------------------------------------------------
-- Cuenta principal de Administrador del torneo (obligatoria para poder entrar)
INSERT IGNORE INTO administrador (nombre, edad, correo, usuario, contrasena, estado, id_rol, nivel_permiso)
SELECT 'Sofía Ruiz', 38, 'sofia@uni.mx', 'admin.torneo', SHA2('1234',256), 'ACTIVO', id_rol, 'Total'
FROM rol WHERE nombre_rol = 'Administrador del torneo';
-- Si por alguna razón no queda ningún administrador del torneo ACTIVO, se reactiva la cuenta principal
UPDATE administrador SET estado = 'ACTIVO'
 WHERE correo = 'sofia@uni.mx'
   AND NOT EXISTS (SELECT 1 FROM (SELECT a.id_administrador FROM administrador a JOIN rol r ON r.id_rol = a.id_rol
                                  WHERE r.nombre_rol = 'Administrador del torneo' AND a.estado = 'ACTIVO') t);

INSERT IGNORE INTO administrador (nombre, edad, correo, usuario, contrasena, estado, id_rol, seccion_asignada)
SELECT 'Ana Torres', 35, 'ana@uni.mx', 'admin.equipos', SHA2('1234',256), 'ACTIVO', id_rol, 'Equipos y jugadores'
FROM rol WHERE nombre_rol = 'Administrador de equipos';
INSERT IGNORE INTO administrador (nombre, edad, correo, usuario, contrasena, estado, id_rol, turno_asignado)
SELECT 'Luis Mora', 40, 'luis@uni.mx', 'admin.partidos', SHA2('1234',256), 'ACTIVO', id_rol, 'Matutino'
FROM rol WHERE nombre_rol = 'Administrador de partidos';

-- 6. Cuenta INACTIVA de prueba: aunque la contraseña sea correcta, NO debe poder entrar
INSERT IGNORE INTO administrador (nombre, edad, correo, usuario, contrasena, estado, id_rol, seccion_asignada)
SELECT 'Pedro Inactivo', 30, 'pedro@uni.mx', 'pedro.inactivo', SHA2('1234',256), 'INACTIVO', id_rol, 'Equipos y jugadores'
FROM rol WHERE nombre_rol = 'Administrador de equipos';

-- Por si alguna contraseña quedó en texto plano
UPDATE administrador SET contrasena = SHA2(contrasena, 256) WHERE CHAR_LENGTH(contrasena) <> 64;

-- ---------------------------------------------------------------------
-- 7. VISTA para consultar los permisos de cada perfil
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_permisos_por_rol AS
SELECT r.nombre_rol, p.modulo, p.clave, p.descripcion
FROM rol r
JOIN rol_permiso rp ON rp.id_rol = r.id_rol
JOIN permiso p      ON p.id_permiso = rp.id_permiso
ORDER BY r.nombre_rol, p.id_permiso;

-- Revisión: cuentas para iniciar sesión (usar el ID o el correo) y permisos por perfil
SELECT a.id_administrador AS id, a.correo, a.nombre, r.nombre_rol AS rol, a.estado
FROM administrador a JOIN rol r ON r.id_rol = a.id_rol ORDER BY a.id_administrador;

SELECT nombre_rol AS rol, COUNT(*) AS total_permisos FROM v_permisos_por_rol GROUP BY nombre_rol;
