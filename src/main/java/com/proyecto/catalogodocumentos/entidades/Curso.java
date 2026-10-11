package com.proyecto.catalogodocumentos.entidades;

import java.util.ArrayList;
import java.util.List;

public class Curso {

    private String nombre;
    private List<String> materias = new ArrayList<>();

    public String getNombre() {
        return nombre;
    }

    public List<String> getMaterias() {
        if (materias == null) {
            materias = new ArrayList<>();
        }
        return materias;
    }

}
