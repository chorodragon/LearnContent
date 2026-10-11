package com.proyecto.catalogodocumentos.datos;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.proyecto.catalogodocumentos.entidades.Catalogo;
import com.proyecto.catalogodocumentos.entidades.Curso;
import com.proyecto.catalogodocumentos.entidades.Documento;
import com.proyecto.catalogodocumentos.util.Locations;

public class CatalogoDao {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Object BLOQUEO = new Object();

    public Catalogo getCatalogo() throws IOException {
        Catalogo catalogo;
        synchronized (BLOQUEO) {
            catalogo = leer();
        }
        catalogo.setCursos(leerCursos());
        return catalogo;
    }

    public void insertDocumento(Documento documento) throws IOException {
        synchronized (BLOQUEO) {
            Catalogo catalogo = leer();
            int maxId = catalogo.getDocumentos().stream().mapToInt(Documento::getId).max().orElse(0);
            documento.setId(maxId + 1);
            catalogo.getDocumentos().add(documento);
            escribir(catalogo);
        }
    }

    public boolean existeDocumentoConNombre(String nombre) throws IOException {
        synchronized (BLOQUEO) {
            return leer().getDocumentos().stream().anyMatch(d -> mismoNombre(d.getNombre(), nombre));
        }
    }

    public void updateClasificacion(int id, String curso, String materia, String tipo) throws IOException {
        synchronized (BLOQUEO) {
            Catalogo catalogo = leer();
            Documento documento = catalogo.getDocumentos().stream()
                    .filter(d -> d.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new IOException("El documento ya no existe en el catálogo."));
            documento.setCurso(curso);
            documento.setMateria(materia);
            documento.setTipo(tipo);
            escribir(catalogo);
        }
    }

    public void deleteDocumento(int id) throws IOException {
        synchronized (BLOQUEO) {
            Catalogo catalogo = leer();
            if (catalogo.getDocumentos().removeIf(d -> d.getId() == id)) {
                escribir(catalogo);
            }
        }
    }

    public List<String> getTiposDocumentos() throws IOException {
        synchronized (BLOQUEO) {
            return leer().getTiposDocumentos();
        }
    }

    public boolean insertTipoDocumento(String valor) throws IOException {
        synchronized (BLOQUEO) {
            Catalogo catalogo = leer();
            List<String> tipos = catalogo.getTiposDocumentos();
            if (tipos.stream().anyMatch(v -> mismoNombre(v, valor))) {
                return false;
            }
            tipos.add(valor.trim());
            escribir(catalogo);
            return true;
        }
    }

    public long deleteTipoDocumento(String valor) throws IOException {
        synchronized (BLOQUEO) {
            Catalogo catalogo = leer();
            long enUso = catalogo.getDocumentos().stream()
                    .filter(d -> mismoNombre(d.getTipo(), valor))
                    .count();
            if (enUso == 0 && catalogo.getTiposDocumentos().removeIf(v -> mismoNombre(v, valor))) {
                escribir(catalogo);
            }
            return enUso;
        }
    }

    public static boolean mismoNombre(String a, String b) {
        return a != null && b != null && a.trim().toLowerCase(Locale.ROOT).equals(b.trim().toLowerCase(Locale.ROOT));
    }

    private static Path fichero() {
        return Path.of(Locations.getDataFile());
    }

    private static Catalogo leer() throws IOException {
        Path fichero = fichero();
        if (!Files.exists(fichero) || Files.size(fichero) == 0) {
            return new Catalogo();
        }
        try (Reader reader = Files.newBufferedReader(fichero, StandardCharsets.UTF_8)) {
            Catalogo catalogo = GSON.fromJson(reader, Catalogo.class);
            return catalogo == null ? new Catalogo() : catalogo;
        } catch (JsonParseException e) {
            throw new IOException("El fichero " + fichero + " está dañado: " + e.getMessage(), e);
        }
    }

    private static List<Curso> leerCursos() throws IOException {
        Path fichero = Path.of(Locations.getCursosFile());
        if (!Files.exists(fichero) || Files.size(fichero) == 0) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(fichero, StandardCharsets.UTF_8)) {
            JsonObject raiz = GSON.fromJson(reader, JsonObject.class);
            List<Curso> cursos = raiz == null || !raiz.has("cursos") ? null
                    : GSON.fromJson(raiz.get("cursos"), new TypeToken<List<Curso>>() {
                    }.getType());
            return cursos == null ? new ArrayList<>() : cursos;
        } catch (JsonParseException e) {
            throw new IOException("El fichero " + fichero + " está dañado: " + e.getMessage(), e);
        }
    }

    private static void escribir(Catalogo catalogo) throws IOException {
        Path fichero = fichero();
        Path temporal = fichero.resolveSibling(fichero.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
            GSON.toJson(catalogo, writer);
        }
        Files.move(temporal, fichero, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

}
