# Torneo Universitario de Fútbol – Modelo de dominio en POO

Aplicación de escritorio (Java + JavaFX) para administrar un torneo de fútbol universitario: categorías por rango de
edad, equipos, jugadores, partidos, resultados y tabla de posiciones. Este repositorio entrega, sobre todo, la **capa del
modelo de dominio** diseñada con los principios de la Programación Orientada a Objetos (abstracción, encapsulamiento,
herencia y polimorfismo), junto con las interfaces gráficas que la utilizan.

> Las pantallas del torneo trabajan con datos en memoria. El **módulo de Administradores (BD)** guarda la información en una base de datos MariaDB/MySQL mediante el patrón DAO (ver la sección *CRUD de administradores con MySQL*).

## Tecnologías

- Java (JDK 17 o superior)
- JavaFX y Scene Builder
- Maven
- Apache NetBeans

## Estructura de paquetes

```
com.mycompany.torneodeportivo
├── App.java              Arranque de la aplicación
├── modelo                Clases del dominio y enumeraciones  ← objeto de esta actividad
├── servicio              DatosServicio: listas en memoria y reglas de negocio
├── controlador           Un controlador por pantalla
└── util                  Validador, Alertas, ImagenUtil, Sesion, Convertidores
src/main/resources/.../vista   Archivos FXML y hoja de estilos
```

## Modelo de dominio

### Árbol de herencia (entidades humanas)

```
Persona (abstract)                    id, nombre, edad  +  getRol() abstracto
├── Administrador (abstract)          correo, usuario, contrasena, estado
│   ├── AdministradorEquipos  (final) seccionAsignada
│   ├── AdministradorPartidos (final) turnoAsignado
│   └── AdministradorTorneo   (final) nivelPermiso
└── Jugador (final)                   numeroCamiseta, posicionCampo, equipo, rutaFoto, estado
```

Otras clases del dominio: `Categoria`, `Equipo`, `Partido`, `Resultado` y `Posicion`, más ocho enumeraciones de estado
(`EstadoCategoria`, `EstadoEquipo`, `EstadoJugador`, `EstadoPartido`, `EstadoResultado`, `EstadoPosicion`,
`EstadoAdministrador` y `PosicionCampo`).

### Cumplimiento de los requerimientos de la actividad

| Requerimiento | Cómo se cumple |
|---|---|
| Clase base `abstract` | `Persona` y `Administrador` son abstractas y no se pueden instanciar. |
| Uso justificado de `final` | Clases hoja (`AdministradorEquipos`, `AdministradorPartidos`, `AdministradorTorneo`, `Jugador`); métodos `iniciarSesion`, `cerrarSesion`, `consultarInformacion`, `equals` y `hashCode` de `Persona`; setters llamados desde constructores. |
| Atributos `private` + getters/setters | Todas las clases. Los setters validan los datos y lanzan `IllegalArgumentException`. |
| Exactamente dos constructores | Uno vacío y otro con los datos mínimos para un estado válido (los datos opcionales tienen valores por defecto). |
| `equals` y `hashCode` | Basados únicamente en el atributo entero `id`. En las personas están definidos una sola vez, como `final`, en `Persona`. |
| `toString` | Devuelve el estado completo; en las subclases concatena `super.toString()` con sus datos propios. La contraseña se muestra enmascarada. |
| Javadoc y `@author` | Todas las clases, constructores y métodos públicos documentados con `@param` y `@return`. |

Para generar la documentación HTML: `javadoc -d docs -sourcepath src/main/java -subpackages com.mycompany.torneodeportivo.modelo`.

## Cómo compilar y ejecutar

1. Abrir el proyecto en NetBeans (**File → Open Project**).
2. Clic derecho sobre el proyecto → **Clean and Build**.
3. Clic derecho sobre el proyecto → **Run** (F6).

Para iniciar sesión se usa el **ID o el correo** de la cuenta y su contraseña (ver *Autenticación y autorización*).

## Validaciones y control de eventos (JavaFX)

Todos los formularios (Categorías, Equipos, Jugadores, Partidos y Administradores) validan en tiempo real:

- **Caracteres permitidos:** un filtro `KEY_TYPED` bloquea las teclas no válidas y un `TextFormatter` filtra lo que se pega.
- **Mensajes bajo cada control:** cada campo tiene su propio `Label` de error (obligatorio, formato correcto, rango), que se actualiza con `textProperty()` al escribir, borrar o pegar.
- **Validación completa:** los botones **Validar** y **Guardar** revisan todos los campos y, si son correctos, muestran un `Alert`. No se guarda, modifica ni elimina información.
- **Estado de los botones:** al iniciar solo Nuevo, Validar y Limpiar están habilitados; Guardar se habilita cuando todo es válido; Editar y Eliminar solo con un registro seleccionado en la tabla (acciones visuales).

Clases de apoyo en `util`: `Reglas`, `Filtros`, `CampoValidado` y `GestorFormulario`; el comportamiento común de los botones está en `controlador/FormularioBase`.
Se cargan datos de demostración en memoria al iniciar para poder probar las tablas y los desplegables.

## Funcionalidades de la aplicación

- Inicio de sesión contra la base de datos (ID o correo), tres perfiles con permisos y cierre de sesión.
- CRUD de categorías, equipos, jugadores y partidos, con tablas, formularios y mensajes de éxito o error.
- Estados administrados con `ComboBox`; logo de equipo y foto de jugador con vista previa.
- Registro de resultados y tabla de posiciones calculada automáticamente.

## CRUD de administradores con MySQL (DAO)

Pantalla **Administradores (BD)** del menú (visible para el rol Administrador del torneo). Realiza el CRUD completo sobre la
tabla `administradores` de la base de datos `torneo_futbol`, con comunicación directa entre el controlador y el DAO.

### Estructura

```
com.mycompany.torneodeportivo.mysql
├── modelos         Administrador, ERoles, EEstados
├── daos            Conexion (JDBC) y AdministradorDAO (guardar, actualizar, eliminar, listarTodos)
└── controladores   AdministradorController
src/main/resources/.../mysql/vista/administrador.fxml   Interfaz (formulario + tabla)
bd/03_tabla_administradores.sql                         Script de la tabla y datos de prueba
```

### Tabla `administradores`

| Columna | Tipo | Restricción |
|---|---|---|
| id | INT | PK, AUTO_INCREMENT |
| nombre | VARCHAR(60) | NOT NULL |
| telefono | VARCHAR(10) | NULL |
| email | VARCHAR(60) | NOT NULL, UNIQUE |
| direccion | VARCHAR(100) | NULL |
| usuario | VARCHAR(20) | NOT NULL, UNIQUE |
| clave_acceso | VARCHAR(255) | NOT NULL |
| ruta_imagen | VARCHAR(255) | NULL |
| rol | ENUM('ADMIN_EQUIPOS','ADMIN_PARTIDOS','ADMIN_TORNEO') | NOT NULL |
| estado | ENUM('ACTIVO','INACTIVO','SUSPENDIDO') | NOT NULL |

### Cómo ejecutarlo

1. Iniciar **Apache** y **MySQL** en XAMPP.
2. En phpMyAdmin, seleccionar la base `torneo_futbol`, abrir la pestaña **SQL** y ejecutar `bd/03_tabla_administradores.sql`.
3. Si el usuario `root` de MariaDB tiene contraseña, escribirla en `daos/Conexion.java` (constante `CLAVE`).
4. En NetBeans: **Clean and Build** y **Run**. Iniciar sesión como Administrador del torneo (`sofia@uni.mx`, contraseña `1234`) y abrir **Administradores (BD)**.

Operaciones: **Guardar** (INSERT), clic en una fila para consultar, **Actualizar** (UPDATE), **Eliminar** (DELETE) y **Limpiar**.
El módulo `module-info.java` requiere `java.sql` y el driver `org.mariadb.jdbc`, y abre los paquetes `mysql.controladores`
y `mysql.modelos` para JavaFX.

## Toda la aplicación guarda en la base de datos

Todas las pantallas (Partidos con su torneo y jornada, Categorías, Equipos, Jugadores, Partidos, Resultados, Posiciones y Administradores)
guardan, editan y eliminan en la base `torneo_futbol` (MariaDB) mediante DAOs del paquete `mysql.daos`:
`TorneoDAO`, `CategoriaDAO`, `EquipoDAO`, `JugadorDAO`, `PartidoDAO` (partido + resultado), `PosicionDAO` y
`AdministradorSistemaDAO` (tablas `administrador` + `rol`). La capa `servicio/DatosServicio` valida las reglas
de negocio, llama al DAO y solo si la base acepta el cambio actualiza la tabla en pantalla.

- **Login** contra la tabla `administrador` con `SesionDAO`; la contraseña se compara cifrada con SHA-256.
- **Editar:** seleccionar una fila → *Editar* (o doble clic) → cambiar datos → *Guardar cambios*.
- **Eliminar:** pide confirmación y respeta las llaves foráneas (no deja borrar equipos con jugadores, etc.).
- **Registrar resultado:** guarda el marcador, finaliza el partido y recalcula la tabla `posicion`.
- Los logos y fotos se copian a la carpeta `imagenes/` y en la base se guarda su ruta.

### Preparar la base
1. XAMPP: encender **Apache** y **MySQL**.
2. phpMyAdmin → base `torneo_futbol` → pestaña **SQL** → pegar `bd/06_preparar_base.sql` → **Continuar**.
   Es seguro: no borra nada, solo crea lo que falte. Se puede ejecutar varias veces.
3. Después ejecutar también `bd/07_login_roles_permisos.sql` (permisos, roles y bitácora de sesiones).
4. NetBeans: *Clean and Build* → *Run*. Cuentas en la sección *Autenticación y autorización*.

## Autenticación y autorización (login, perfiles y permisos)

Al ejecutar la aplicación se abre primero la **ventana de inicio de sesión**. Es un área administrativa de acceso privado,
por eso **no existe formulario de registro**: las cuentas las crea el *Administrador del torneo* desde el módulo
**Administradores**, y el script SQL garantiza que siempre exista al menos una cuenta activa de ese perfil.

### Flujo

1. **Login** (`login.fxml` + `LoginController`): se escribe el **ID o el correo electrónico** y la contraseña.
   `SesionDAO.validarCredenciales()` busca la cuenta en la tabla `administrador`, compara la contraseña cifrada con SHA-256
   y solo deja entrar si el estado es **ACTIVO**. Si el ID/correo o la contraseña no coinciden se muestra un mensaje genérico
   (no se revela cuál de los dos falló); si la cuenta está INACTIVA o SUSPENDIDA se indica que no puede entrar.
2. `PermisoDAO.permisosDe()` lee los permisos del rol (tablas `rol_permiso` y `permiso`) y `SesionDAO.registrarInicio()`
   guarda el inicio en la tabla `sesion`. Los datos quedan en `util/Sesion`.
3. Se abre la **ventana principal** (menú) y se cierra la del login.
4. **Autorización:** el menú solo muestra los módulos que el rol puede consultar; en cada pantalla los botones Nuevo, Guardar,
   Editar, Eliminar, Registrar resultado y Actualizar tabla se habilitan o bloquean según el permiso. Si el rol solo puede
   consultar un módulo, el formulario queda en **modo consulta** (aviso naranja; al seleccionar una fila se ven sus datos e
   imagen sin poder modificarlos). Antes de cada acción, `servicio/Autorizacion.exigir()` vuelve a validar el permiso en la
   base de datos con `PermisoDAO.tienePermiso()` (si mientras tanto la cuenta se desactivó, la acción se bloquea).
5. **Cerrar sesión** (botón del menú, con confirmación): `SesionDAO.registrarCierre()` guarda la fecha de cierre, se limpia la
   sesión, se cierra la ventana principal y se regresa al login. Cerrar la ventana con la X también registra el cierre.
6. **Mi perfil** (botón del menú): muestra el rol, sus responsabilidades y la lista de permisos leída de la base de datos.

### Los tres perfiles de usuario

| Perfil | Responsabilidades | Puede hacer | Restricciones |
|---|---|---|---|
| **Administrador del torneo** | Responsable general del torneo y de las cuentas de usuario. | Todo: registrar, consultar, actualizar y eliminar categorías, equipos, jugadores, partidos y cuentas de usuario; registrar resultados; recalcular la tabla de posiciones. | No puede eliminarse ni desactivarse a sí mismo, ni dejar al sistema sin un Administrador del torneo activo. |
| **Administrador de equipos** | Inscripciones: categorías, equipos y jugadores (con logo y foto). | CRUD completo de categorías, equipos y jugadores. Consultar partidos y tabla de posiciones. | No programa ni modifica partidos, no registra resultados, no recalcula posiciones y no ve las cuentas de usuario. |
| **Administrador de partidos** | Calendario del torneo y marcadores. | CRUD completo de partidos y registrar resultados. Consultar categorías, equipos, jugadores y posiciones. | No modifica categorías, equipos ni jugadores, no recalcula posiciones y no ve las cuentas de usuario. |

### Matriz de permisos (tabla `permiso` + `rol_permiso`)

| Permiso | Torneo | Equipos | Partidos |
|---|:-:|:-:|:-:|
| CATEGORIAS_VER / EQUIPOS_VER / JUGADORES_VER | ✔ | ✔ | ✔ |
| CATEGORIAS_ / EQUIPOS_ / JUGADORES_ CREAR, EDITAR, ELIMINAR | ✔ | ✔ | ✖ |
| PARTIDOS_VER | ✔ | ✔ | ✔ |
| PARTIDOS_CREAR, PARTIDOS_EDITAR, PARTIDOS_ELIMINAR | ✔ | ✖ | ✔ |
| RESULTADOS_REGISTRAR | ✔ | ✖ | ✔ |
| POSICIONES_VER | ✔ | ✔ | ✔ |
| POSICIONES_RECALCULAR | ✔ | ✖ | ✖ |
| ADMINISTRADORES_VER, _CREAR, _EDITAR, _ELIMINAR | ✔ | ✖ | ✖ |
| **Total** | 23 | 14 | 9 |

En phpMyAdmin se pueden consultar con `SELECT * FROM v_permisos_por_rol;`.

### Clases nuevas o modificadas

```
mysql/modelos/EPermisos.java       enum con los 23 permisos (misma clave que la tabla permiso)
mysql/daos/SesionDAO.java          validarCredenciales (ID o correo + contraseña + ACTIVO), registrarInicio, registrarCierre
mysql/daos/PermisoDAO.java         permisosDe, tienePermiso, detallePermisos, descripcionRol
servicio/Autorizacion.java         exigir(permiso): valida la sesión y el permiso en la BD antes de cada acción
util/Sesion.java                   administrador, id de sesión y permisos; puede(permiso)
App.java                           ventana de login y ventana principal; cerrarSesion()
controlador/LoginController        login contra la BD          controlador/MenuController   menú por permisos, Mi perfil, cerrar sesión
controlador/FormularioBase         botones según CREAR/EDITAR/ELIMINAR y modo consulta (lo heredan todos los CRUD)
```

### Tablas nuevas (`bd/07_login_roles_permisos.sql`)

| Tabla | Columnas | Restricciones |
|---|---|---|
| `permiso` | id_permiso, clave, modulo, descripcion | PK, `clave` UNIQUE, NOT NULL |
| `rol_permiso` | id_rol, id_permiso | PK compuesta, FK a `rol` y a `permiso` (ON DELETE CASCADE) |
| `sesion` | id_sesion, id_administrador, fecha_inicio, fecha_cierre, estado ENUM('ABIERTA','CERRADA') | PK, FK a `administrador`, CHECK cierre ≥ inicio |
| `rol` (cambio) | + descripcion | responsabilidades de cada perfil |

### Cuentas para probar (contraseña `1234`)

| Correo | Perfil | Estado |
|---|---|---|
| `sofia@uni.mx` | Administrador del torneo | ACTIVO (cuenta obligatoria) |
| `ana@uni.mx` | Administrador de equipos | ACTIVO |
| `luis@uni.mx` | Administrador de partidos | ACTIVO |
| `pedro@uni.mx` | Administrador de equipos | **INACTIVO** (no debe poder entrar) |

El **ID** de cada cuenta se ve en la última consulta que muestra el script 07 o en la tabla del módulo Administradores;
también se puede iniciar sesión con ese número.

## Autor

Christian Hernandez Garcia – Programación Orientada a Objetos – Universidad Tecnológica de Jalisco
