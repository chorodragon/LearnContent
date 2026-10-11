package com.proyecto.catalogodocumentos.entidades;

import java.util.function.Function;

public enum Categoria {

    CURSO("Curso", "Cursos", "un curso", "el curso", "Nuevo curso", "LearnCenterHub", Documento::getCurso),
    MATERIA("Materia", "Materias", "una materia", "la materia", "Nueva materia", "LearnCenterHub",
            Documento::getMateria),
    TIPO("Tipo de documento", "Tipos de documento", "un tipo de documento", "el tipo de documento",
            "Nuevo tipo de documento", "Configurar", Documento::getTipo);

    private final String nombre;
    private final String plural;
    private final String conArticulo;
    private final String conArticuloDefinido;
    private final String nuevo;
    private final String configuradoEn;
    private final Function<Documento, String> valorDe;

    Categoria(String nombre, String plural, String conArticulo, String conArticuloDefinido, String nuevo,
            String configuradoEn, Function<Documento, String> valorDe) {
        this.nombre = nombre;
        this.plural = plural;
        this.conArticulo = conArticulo;
        this.conArticuloDefinido = conArticuloDefinido;
        this.nuevo = nuevo;
        this.configuradoEn = configuradoEn;
        this.valorDe = valorDe;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPlural() {
        return plural;
    }

    public String getConArticulo() {
        return conArticulo;
    }

    public String getConArticuloDefinido() {
        return conArticuloDefinido;
    }

    public String getNuevo() {
        return nuevo;
    }

    public String getConfiguradoEn() {
        return configuradoEn;
    }

    public String valorDe(Documento documento) {
        return valorDe.apply(documento);
    }
}
