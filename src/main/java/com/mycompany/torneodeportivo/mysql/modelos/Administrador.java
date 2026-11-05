package com.mycompany.torneodeportivo.mysql.modelos;

/**
 * Administrador del torneo tal como se guarda en la tabla "administradores".
 *
 * @author Christian Hernandez Garcia
 */
public class Administrador {

    private int id;
    private String nombre;
    private String telefono;
    private String email;
    private String direccion;
    private String usuario;
    private String claveAcceso;
    private String rutaImagen;
    private ERoles rol;
    private EEstados estado;

    public Administrador() {
    }

    public Administrador(int id, String nombre, String telefono, String email, String direccion,
            String usuario, String claveAcceso, String rutaImagen, ERoles rol, EEstados estado) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.usuario = usuario;
        this.claveAcceso = claveAcceso;
        this.rutaImagen = rutaImagen;
        this.rol = rol;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getClaveAcceso() { return claveAcceso; }
    public void setClaveAcceso(String claveAcceso) { this.claveAcceso = claveAcceso; }

    public String getRutaImagen() { return rutaImagen; }
    public void setRutaImagen(String rutaImagen) { this.rutaImagen = rutaImagen; }

    public ERoles getRol() { return rol; }
    public void setRol(ERoles rol) { this.rol = rol; }

    public EEstados getEstado() { return estado; }
    public void setEstado(EEstados estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "Administrador{id=" + id + ", nombre=" + nombre + ", usuario=" + usuario
                + ", rol=" + rol + ", estado=" + estado + "}";
    }
}
