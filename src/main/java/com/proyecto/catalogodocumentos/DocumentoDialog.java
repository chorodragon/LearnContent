package com.proyecto.catalogodocumentos;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import com.proyecto.catalogodocumentos.datos.CatalogoDao;
import com.proyecto.catalogodocumentos.entidades.Catalogo;
import com.proyecto.catalogodocumentos.entidades.Categoria;
import com.proyecto.catalogodocumentos.entidades.Documento;
import com.proyecto.catalogodocumentos.util.Estilos;
import com.proyecto.catalogodocumentos.util.Locations;

public final class DocumentoDialog extends JDialog {

    private final File fichero;
    private final Documento documento;
    private final Runnable alCambiar;
    private final CatalogoDao catalogoDao = new CatalogoDao();
    private final JComboBox<String> cmbCurso = new JComboBox<>();
    private final JComboBox<String> cmbMateria = new JComboBox<>();
    private final JComboBox<String> cmbTipo = new JComboBox<>();
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnEliminar = new JButton("Eliminar");

    public DocumentoDialog(JFrame owner, Documento documento, Runnable alCambiar) {
        super(owner, valorOVacio(documento.getNombre()), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.documento = documento;
        this.alCambiar = alCambiar;
        fichero = resolverFichero(documento.getPath());

        JPanel datos = new JPanel(new GridBagLayout());
        datos.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));
        int fila = 0;

        JLabel lblTitulo = new JLabel(valorOVacio(documento.getNombre()));
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        JLabel lblAutores = new JLabel(valorOVacio(documento.getAutores()));
        Estilos.textoSecundario(lblAutores);
        GridBagConstraints cabecera = new GridBagConstraints();
        cabecera.gridx = 0;
        cabecera.gridwidth = 2;
        cabecera.anchor = GridBagConstraints.WEST;
        cabecera.insets = new Insets(0, 4, 2, 4);
        cabecera.gridy = fila++;
        datos.add(lblTitulo, cabecera);
        cabecera.gridy = fila++;
        cabecera.insets = new Insets(0, 4, 14, 4);
        datos.add(lblAutores, cabecera);

        Catalogo catalogo = AgregarDocumentoDialog.cargarCatalogo(catalogoDao);
        AgregarDocumentoDialog.rellenarCombo(cmbCurso, Categoria.CURSO, catalogo, null, documento.getCurso());
        AgregarDocumentoDialog.rellenarCombo(cmbTipo, Categoria.TIPO, catalogo, null, documento.getTipo());
        AgregarDocumentoDialog.rellenarMaterias(cmbCurso, cmbMateria, catalogo, documento.getMateria());
        cmbCurso.addActionListener(e -> {
            AgregarDocumentoDialog.rellenarMaterias(cmbCurso, cmbMateria, catalogo, null);
            actualizarBotonGuardar();
        });
        cmbMateria.addActionListener(e -> actualizarBotonGuardar());
        cmbTipo.addActionListener(e -> actualizarBotonGuardar());

        fila = agregarCampo(datos, fila, "ID", String.valueOf(documento.getId()));
        fila = agregarFila(datos, fila, "Curso", cmbCurso);
        fila = agregarFila(datos, fila, "Materia", cmbMateria);
        fila = agregarFila(datos, fila, "Tipo de documento", cmbTipo);
        fila = agregarCampo(datos, fila, "Autores", documento.getAutores());
        fila = agregarCampo(datos, fila, "Fecha", documento.getFecha());
        fila = agregarCampo(datos, fila, "Tags", documento.getTags());
        fila = agregarCampo(datos, fila, "Carátula", documento.getCaratula());
        fila = agregarCampo(datos, fila, "Ruta", fichero == null ? "" : fichero.getAbsolutePath());
        agregarCampo(datos, fila, "Descripción", documento.getDescripcion());

        JButton btnAbrir = new JButton("Abrir");
        btnAbrir.addActionListener(e -> ejecutarEnSegundoPlano(this::abrir));

        JButton btnAbrirUbicacion = new JButton("Abrir ubicación");
        btnAbrirUbicacion.addActionListener(e -> ejecutarEnSegundoPlano(this::abrirUbicacion));

        boolean existe = fichero != null && fichero.exists();
        btnAbrir.setEnabled(existe);
        btnAbrirUbicacion.setEnabled(existe);
        if (!existe) {
            btnAbrir.setToolTipText("No se encuentra el fichero");
            btnAbrirUbicacion.setToolTipText("No se encuentra el fichero");
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setToolTipText("Guardar el curso, la materia y el tipo de documento");
        btnGuardar.addActionListener(e -> guardarClasificacion());

        btnEliminar.addActionListener(e -> eliminar());
        Estilos.botonPeligro(btnEliminar);
        Estilos.botonPrincipal(btnAbrir);

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izquierda.add(btnEliminar);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.add(btnGuardar);
        derecha.add(btnAbrirUbicacion);
        derecha.add(btnAbrir);
        JPanel botones = new JPanel(new BorderLayout());
        botones.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(0xE5E7EB)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        botones.add(izquierda, BorderLayout.WEST);
        botones.add(derecha, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(datos, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        pack();
        setSize(Math.max(getWidth(), 520), getHeight());
        setLocationRelativeTo(owner);
    }

    private static int agregarFila(JPanel panel, int fila, String etiqueta, JComboBox<String> combo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(4, 4, 4, 12);
        c.anchor = GridBagConstraints.WEST;

        JLabel lblEtiqueta = new JLabel(etiqueta);
        Estilos.textoSecundario(lblEtiqueta);
        c.gridx = 0;
        panel.add(lblEtiqueta, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(combo, c);
        return fila + 1;
    }

    private String getCursoElegido() {
        return (String) cmbCurso.getSelectedItem();
    }

    private String getMateriaElegida() {
        return (String) cmbMateria.getSelectedItem();
    }

    private String getTipoElegido() {
        return (String) cmbTipo.getSelectedItem();
    }

    private void actualizarBotonGuardar() {
        boolean cambiado = !Objects.equals(getCursoElegido(), documento.getCurso())
                || !Objects.equals(getMateriaElegida(), documento.getMateria())
                || !Objects.equals(getTipoElegido(), documento.getTipo());
        btnGuardar.setEnabled(cambiado && getCursoElegido() != null && getMateriaElegida() != null
                && getTipoElegido() != null);
    }

    private void guardarClasificacion() {
        String curso = getCursoElegido();
        String materia = getMateriaElegida();
        String tipo = getTipoElegido();
        btnGuardar.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws IOException {
                catalogoDao.updateClasificacion(documento.getId(), curso, materia, tipo);
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    documento.setCurso(curso);
                    documento.setMateria(materia);
                    documento.setTipo(tipo);
                    alCambiar.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    e.getCause().printStackTrace();
                    JOptionPane.showMessageDialog(DocumentoDialog.this,
                            "No se pudo guardar: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                actualizarBotonGuardar();
            }
        }.execute();
    }

    private static int agregarCampo(JPanel panel, int fila, String etiqueta, String valor) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(4, 4, 4, 12);
        c.anchor = GridBagConstraints.NORTHWEST;

        JLabel lblEtiqueta = new JLabel(etiqueta);
        Estilos.textoSecundario(lblEtiqueta);
        c.gridx = 0;
        panel.add(lblEtiqueta, c);

        JTextArea txtValor = new JTextArea(valorOVacio(valor));
        txtValor.setEditable(false);
        txtValor.setLineWrap(true);
        txtValor.setWrapStyleWord(true);
        txtValor.setOpaque(false);
        txtValor.setFont(lblEtiqueta.getFont().deriveFont(Font.PLAIN));
        txtValor.setBorder(null);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        if ("Descripción".equals(etiqueta)) {
            txtValor.setRows(5);
            txtValor.setColumns(30);
            JScrollPane scroll = new JScrollPane(txtValor);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;
            panel.add(scroll, c);
        } else {
            txtValor.setColumns(30);
            panel.add(txtValor, c);
        }
        return fila + 1;
    }

    private static File resolverFichero(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        File f = new File(path);
        return f.isAbsolute() ? f : new File(Locations.getDocumentsFolder(), path);
    }

    private void abrir() throws IOException {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            throw new IOException("El sistema no permite abrir ficheros desde la aplicación.");
        }
        Desktop.getDesktop().open(fichero);
    }

    private void abrirUbicacion() throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
            Desktop.getDesktop().browseFileDirectory(fichero);
            return;
        }
        if (mostrarEnExplorador() || mostrarEnGestorLinux()) {
            return;
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            throw new IOException("El sistema no permite abrir carpetas desde la aplicación.");
        }
        Desktop.getDesktop().open(fichero.getAbsoluteFile().getParentFile());
    }

    private boolean mostrarEnExplorador() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return false;
        }
        try {
            new ProcessBuilder("explorer.exe", "/select,", fichero.getAbsolutePath()).start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean mostrarEnGestorLinux() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            return false;
        }
        try {
            Process proceso = new ProcessBuilder("dbus-send", "--session", "--print-reply",
                    "--dest=org.freedesktop.FileManager1", "/org/freedesktop/FileManager1",
                    "org.freedesktop.FileManager1.ShowItems",
                    "array:string:" + fichero.getAbsoluteFile().toURI(), "string:")
                    .redirectErrorStream(true)
                    .start();
            return proceso.waitFor() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void eliminar() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar definitivamente \"" + valorOVacio(documento.getNombre()) + "\"?\n"
                        + "Se borrarán el registro, el documento y la carátula. Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        btnEliminar.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws IOException {
                catalogoDao.deleteDocumento(documento.getId());
                List<String> noBorrados = new ArrayList<>();
                borrarFichero(fichero, noBorrados);
                String caratula = documento.getCaratula();
                if (caratula != null && !caratula.isBlank()) {
                    borrarFichero(new File(Locations.getCoversFolder(), caratula), noBorrados);
                }
                return noBorrados;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    List<String> noBorrados = get();
                    if (!noBorrados.isEmpty()) {
                        JOptionPane.showMessageDialog(DocumentoDialog.this,
                                "El documento se eliminó del catálogo, pero no se pudieron borrar:\n"
                                        + String.join("\n", noBorrados),
                                "Aviso", JOptionPane.WARNING_MESSAGE);
                    }
                    dispose();
                    alCambiar.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    e.getCause().printStackTrace();
                    btnEliminar.setEnabled(true);
                    JOptionPane.showMessageDialog(DocumentoDialog.this,
                            "No se pudo eliminar el documento: " + e.getCause().getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static void borrarFichero(File f, List<String> noBorrados) {
        if (f == null) {
            return;
        }
        try {
            Files.deleteIfExists(f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            noBorrados.add(f.getAbsolutePath());
        }
    }

    private interface Accion {
        void ejecutar() throws IOException;
    }

    private void ejecutarEnSegundoPlano(Accion accion) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws IOException {
                accion.ejecutar();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    JOptionPane.showMessageDialog(DocumentoDialog.this,
                            "No se pudo abrir: " + e.getCause().getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static String valorOVacio(String valor) {
        return valor == null ? "" : valor;
    }
}
