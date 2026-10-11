package com.proyecto.catalogodocumentos.entidades;

import java.util.ArrayList;
import java.util.List;

public class Catalogo {

    private transient List<Curso> cursos = new ArrayList<>();
    private List<String> tiposDocumentos = new ArrayList<>();
    private List<Documento> documentos = new ArrayList<>();

    public List<Curso> getCursos() {
        if (cursos == null) {
            cursos = new ArrayList<>();
        }
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    public List<String> getTiposDocumentos() {
        if (tiposDocumentos == null) {
            tiposDocumentos = new ArrayList<>();
        }
        return tiposDocumentos;
    }

    public List<Documento> getDocumentos() {
        if (documentos == null) {
            documentos = new ArrayList<>();
        }
        return documentos;
    }

    public List<String> getValores(Categoria categoria, String curso) {
        return switch (categoria) {
            case CURSO -> getCursos().stream().map(Curso::getNombre).toList();
            case MATERIA -> getCursos().stream()
                    .filter(c -> c.getNombre() != null && curso != null
                            && c.getNombre().trim().equalsIgnoreCase(curso.trim()))
                    .findFirst()
                    .map(Curso::getMaterias)
                    .orElse(List.of());
            case TIPO -> getTiposDocumentos();
        };
    }

}
