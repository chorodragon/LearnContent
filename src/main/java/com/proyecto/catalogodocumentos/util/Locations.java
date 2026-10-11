package com.proyecto.catalogodocumentos.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class Locations {

    public static final String VARIABLE = "LEARNOS_RESOURCES_DIR";
    public static final String DEFAULT_ROOT_FOLDER = Path.of("dev-resources").toAbsolutePath().toString();

    private static final String DATA_FILE = "documentos.json";
    private static final String CURSOS_FILE = "catalogo.json";
    private static final String DOCUMENTS_FOLDER = "documentos";
    private static final String COVERS_FOLDER = "covers";

    private static final String ROOT_FOLDER = resolverRootFolder();

    public static String getRootFolder() {
        return ROOT_FOLDER;
    }

    public static String getDataFile() {
        return Path.of(ROOT_FOLDER, DATA_FILE).toString();
    }

    public static String getCursosFile() {
        return Path.of(ROOT_FOLDER, CURSOS_FILE).toString();
    }

    public static String getLogFile() {
        return Path.of(ROOT_FOLDER, "log.txt").toString();
    }

    public static String getDocumentsFolder() {
        return Path.of(ROOT_FOLDER, DOCUMENTS_FOLDER).toString();
    }

    public static String getCoversFolder() {
        return Path.of(ROOT_FOLDER, COVERS_FOLDER).toString();
    }

    private static String resolverRootFolder() {
        String valor = System.getenv(VARIABLE);
        if (valor == null || valor.isBlank()) {
            return DEFAULT_ROOT_FOLDER;
        }
        try {
            return normalizar(valor);
        } catch (InvalidPathException e) {
            e.printStackTrace();
            return DEFAULT_ROOT_FOLDER;
        }
    }

    private static String normalizar(String valor) {
        return Path.of(valor.trim()).toAbsolutePath().normalize().toString();
    }

    public static void createDirectories() throws IOException {
        Files.createDirectories(Path.of(getDocumentsFolder()));
        Files.createDirectories(Path.of(getCoversFolder()));

        File log = new File(getLogFile());
        if (!log.exists()) {
            log.createNewFile();
        }
    }

}
