package com.proyecto.catalogodocumentos.entidades;

public class Documento {

    private int id;
    private String nombre;
    private String autores;
    private String descripcion;
    private String fecha;
    private String caratula;
    private String path;
    private String tags;
    private String curso;
    private String materia;
    private String tipo;

    public Documento(int id, String nombre, String autores, String descripcion, String fecha, String caratula,
            String path, String tags, String curso, String materia, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.autores = autores;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.caratula = caratula;
        this.path = path;
        this.tags = tags;
        this.curso = curso;
        this.materia = materia;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getAutores() {
        return autores;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public String getCaratula() {
        return caratula;
    }

    public String getPath() {
        return path;
    }

    public String getTags() {
        return tags;
    }

    public String getCurso() {
        return curso;
    }

    public String getMateria() {
        return materia;
    }

    public String getTipo() {
        return tipo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setAutores(String autores) {
        this.autores = autores;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

}
