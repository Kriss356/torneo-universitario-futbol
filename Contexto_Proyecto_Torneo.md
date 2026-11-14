# Contexto del proyecto: Torneo Universitario de Fútbol

**Alumno:** Christian Hernandez Garcia · Universidad Tecnológica de Jalisco · Programación Orientada a Objetos
**Repositorio:** https://github.com/Kriss356/torneo-universitario-futbol
**Carpeta local (NetBeans):** `C:\Users\ASPIRE\Documents\NetBeansProjects\VistasTorneoDeportivo`
**Paquete raíz:** `com.mycompany.torneodeportivo` · módulo `com.mycompany.torneodeportivo`
**Stack:** Java 11 (release 11), JavaFX 13, Maven (`javafx-maven-plugin`), NetBeans, XAMPP (MariaDB), phpMyAdmin.
**mainClass en el pom:** `com.mycompany.torneodeportivo.App`

## Estructura actual
```
com.mycompany.torneodeportivo
├── Launcher.java            clase principal del JAR ejecutable (llama a App.main)
├── App.java                 inicia Almacenamiento; 2 ventanas: login (primera) y principal; abrirVentanaPrincipal(), cerrarSesion(), cargarFXML(), cargarFXMLMysql()
├── modelo                   Persona (abstract) > Administrador (abstract) > AdministradorEquipos / Partidos / Torneo (final)
│                            Jugador (final), Categoria, Equipo, Partido, Resultado, Posicion, Torneo + enums de estado
├── servicio                 DatosServicio (listas en memoria sincronizadas con la BD, reglas de negocio)
│                            Autorizacion.exigir(permiso): valida sesión + permiso en la BD antes de cada acción
├── controlador              Login, Menu, Categoria, Equipo, Jugador, Partido, Posicion, Administrador, FormularioBase
├── util                     Alertas, Validador, ImagenUtil, Almacenamiento (Documentos + log), Sesion (admin + idSesion + permisos), Cifrado (SHA-256), Convertidores, Reglas, Filtros, CampoValidado, GestorFormulario, ExportadorCSV
└── mysql
    ├── modelos              Administrador, ERoles, EEstados, EPermisos (23 permisos)
    ├── daos                 Conexion (jdbc:mariadb://localhost:3306/torneo_futbol, root, sin clave), ErroresBD, Ids,
    │                        TorneoDAO, CategoriaDAO, EquipoDAO, JugadorDAO, PartidoDAO, PosicionDAO,
    │                        AdministradorSistemaDAO (tabla administrador + rol), AdministradorDAO (tabla administradores),
    │                        SesionDAO (login/logout), PermisoDAO (validación de permisos)
    └── controladores        AdministradorController (módulo "Administradores (BD)")
resources/.../vista          login, menu, categoria, equipo, jugador, partido, posicion, administrador + estilos.css
resources/.../mysql/vista    administrador.fxml
bd/                          03 … 06_preparar_base.sql, 07_login_roles_permisos.sql
```
`module-info.java`: requires `javafx.controls`, `javafx.fxml`, `java.sql`, `org.mariadb.jdbc`; abre a `javafx.fxml` los paquetes raíz, `controlador` y `mysql.controladores`; abre a `javafx.base` `modelo` y `mysql.modelos`.
`pom.xml`: javafx-controls, javafx-fxml (13) y `org.mariadb.jdbc:mariadb-java-client:3.3.3`; `maven-shade-plugin` 3.5.1 genera `distribucion/TorneoUniversitarioFutbol.jar` (Main-Class `Launcher`) en Clean and Build.

## Base de datos `torneo_futbol` (MariaDB en XAMPP)
- Tablas: `rol` (+ descripcion), `administrador`, `categoria`, `equipo`, `jugador`, `torneo`, `partido`, `resultado`, `posicion`,
  `permiso`, `rol_permiso`, `sesion`, `administradores` (módulo DAO anterior). Vistas `v_tabla_posiciones`, `v_permisos_por_rol`.
- Toda la app guarda en la BD (CRUD con DAOs). Logos y fotos se copian a `Documentos/TorneoUniversitarioFutbol/imagenes/` con nombre UUID y en la BD se guarda solo el nombre del archivo (las rutas viejas `imagenes/x.png` siguen funcionando; `bd/08_rutas_imagenes_documentos.sql` las normaliza). Log en `Documentos/TorneoUniversitarioFutbol/registro.log` (fecha y hora | operación | ruta | resultado).
- Contraseñas cifradas con SHA-256 (`Cifrado.sha256` = `SHA2(x,256)`).

## Autenticación y autorización (actividad 7)
- Login con **ID o correo** + contraseña, solo cuentas ACTIVAS (`SesionDAO.validarCredenciales`). Mensaje genérico si falla.
- No hay formulario de registro; el script 07 garantiza una cuenta ACTIVA de Administrador del torneo y la app no deja eliminar/desactivar la última.
- 3 perfiles: Administrador del torneo (23 permisos, todo), de equipos (14: CRUD categorías/equipos/jugadores + ver partidos y posiciones), de partidos (9: CRUD partidos + resultados + ver lo demás).
- Menú por permisos VER; botones por CREAR/EDITAR/ELIMINAR en `FormularioBase`; "modo consulta" si solo puede ver; RESULTADOS_REGISTRAR y POSICIONES_RECALCULAR en sus botones.
- Cerrar sesión: registra cierre en `sesion`, cierra ventana principal y regresa al login. Botón "Mi perfil" muestra rol y permisos.
- Cuentas (clave `1234`): sofia@uni.mx (torneo), ana@uni.mx (equipos), luis@uni.mx (partidos), pedro@uni.mx (INACTIVO).

## Actividades hechas
1. CRUD de vistas con JavaFX.
2. Modelo de dominio POO (abstract, final, 2 constructores, setters que validan, equals/hashCode por id, toString, Javadoc).
3. Validaciones en tiempo real y video sin narración.
4. Diseño de la BD en phpMyAdmin.
5. Botón "Salir de la aplicación" en el login.
6. CRUD de administradores con DAO y MariaDB; después toda la app pasó a guardar en la BD (torneo y jornada en partidos).
7. Login contra la BD, 3 perfiles con permisos, autorización en la interfaz, cerrar sesión y DAOs de sesión/permisos. *(Pendiente: probar en XAMPP, video y entrega.)*
8. Control de versiones con Git/GitHub: la carpeta de NetBeans ya es un repo Git (remoto `origin`, rama `main`, usuario "Christian Hernandez Garcia"). Commits con mensajes claros, rama `feature/exportar-posiciones-csv` (botón **Exportar CSV** en Posiciones + `util/ExportadorCSV`) unida a `main` con merge `--no-ff`. Pendiente: push desde NetBeans, capturas y PDF.
9. JAR ejecutable + almacenamiento externo: `util/Almacenamiento` crea al iniciar `Documentos/TorneoUniversitarioFutbol/` e `imagenes/` (o continúa si existen) y escribe `registro.log` (creación de carpetas, guardar/eliminar imagen, migración de la carpeta `imagenes/` vieja). Al actualizar con imagen nueva se borra la anterior. JAR "gordo" con shade + `Launcher` y `distribucion/Ejecutar_Torneo.bat` + `LEEME.txt`. Respaldo en `_respaldo_antes_jar`. *(Pendiente: Clean and Build en NetBeans, probar el JAR con XAMPP, video y entrega.)*

## Git
- Trabajar las funcionalidades nuevas en ramas `feature/<nombre>` y unirlas a `main` con merge.
- Subir con NetBeans: Team → Remote → Push (usuario de GitHub + token personal como contraseña).
- `.gitignore` excluye `target/`, `nbactions.xml`, `_respaldo_antes_*/`, `imagenes/` y `distribucion/*.jar`.

## Cómo corre
XAMPP (Apache + MySQL) → phpMyAdmin, base `torneo_futbol`, pestaña SQL: `bd/06_preparar_base.sql` y luego `bd/07_login_roles_permisos.sql`
→ NetBeans: Clean and Build y Run → login con `sofia@uni.mx` / `1234`.
Sin NetBeans: Clean and Build → doble clic en `distribucion/Ejecutar_Torneo.bat` (o `java -jar TorneoUniversitarioFutbol.jar`), requiere Java 11+ y XAMPP encendido.

## Cosas pendientes o por decidir
- Actividad "calculadora de calificaciones" (una sola clase, Scene Builder): se construyó pero nunca se entregó.
- Respaldos antes de cada cambio grande en carpetas `_respaldo_antes_*` (no subirlas al repositorio).

## Formas de trabajo que funcionaron
- Editar directo en la carpeta de NetBeans conectada (respaldo previo en `_respaldo_antes_*`); compilar con javac usando los jars de `.m2`.
- Probar los scripts SQL y los DAOs en un MariaDB de prueba antes de entregar.
- Reportes en Word con campos en amarillo para lo que debe llenar el alumno.
- Videos de demostración: checklist de qué mostrar (app + phpMyAdmin lado a lado).
