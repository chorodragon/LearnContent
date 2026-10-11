package com.proyecto.catalogodocumentos;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.formdev.flatlaf.FlatClientProperties;
import com.proyecto.catalogodocumentos.datos.CatalogoDao;
import com.proyecto.catalogodocumentos.entidades.Catalogo;
import com.proyecto.catalogodocumentos.entidades.Categoria;
import com.proyecto.catalogodocumentos.entidades.Documento;
import com.proyecto.catalogodocumentos.util.Estilos;
import com.proyecto.catalogodocumentos.util.Funciones;
import com.proyecto.catalogodocumentos.util.Locations;

public final class AgregarDocumentoDialog extends JDialog {

    private static final int ANCHO_PREVIEW = 100;
    private static final int ALTO_PREVIEW = 140;

    private final Runnable alGuardar;
    private final CatalogoDao catalogoDao = new CatalogoDao();

    private final JTextField txtNombre = new JTextField(30);
    private final JTextField txtAutores = new JTextField(30);
    private final JTextField txtFecha = new JTextField(30);
    private final JComboBox<String> cmbCurso = new JComboBox<>();
    private final JComboBox<String> cmbMateria = new JComboBox<>();
    private final JComboBox<String> cmbTipo = new JComboBox<>();
    private final JTextField txtTags = new JTextField(30);
    private final JTextArea txtDescripcion = new JTextArea(5, 30);
    private final JTextField txtDocumento = new JTextField(30);
    private final JTextField txtCaratula = new JTextField(30);
    private final JLabel lblPreview = new JLabel("Sin carátula", SwingConstants.CENTER);
    private final JButton btnGuardar = new JButton("Guardar");

    private File ficheroDocumento;
    private File ficheroCaratula;

    public AgregarDocumentoDialog(JFrame owner, Runnable alGuardar) {
        super(owner, "Agregar documento", true);
        this.alGuardar = alGuardar;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        txtFecha.setToolTipText("Formato AAAA-MM-DD");
        txtFecha.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "AAAA-MM-DD");
        txtTags.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "etiqueta1, etiqueta2…");
        txtDocumento.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ningún fichero seleccionado");
        txtCaratula.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Opcional");
        txtTags.setToolTipText("Separados por comas");
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDocumento.setEditable(false);
        txtCaratula.setEditable(false);

        Catalogo catalogo = cargarCatalogo(catalogoDao);
        rellenarCombo(cmbCurso, Categoria.CURSO, catalogo, null, null);
        rellenarCombo(cmbTipo, Categoria.TIPO, catalogo, null, null);
        rellenarMaterias(cmbCurso, cmbMateria, catalogo, null);
        cmbCurso.addActionListener(e -> rellenarMaterias(cmbCurso, cmbMateria, catalogo, null));

        JButton btnDocumento = new JButton("Examinar…");
        btnDocumento.addActionListener(e -> elegirDocumento());
        JButton btnCaratula = new JButton("Examinar…");
        btnCaratula.addActionListener(e -> elegirCaratula());

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));
        int fila = 0;
        agregarFila(formulario, fila++, "Nombre *", txtNombre, null);
        agregarFila(formulario, fila++, "Curso *", cmbCurso, null);
        agregarFila(formulario, fila++, "Materia *", cmbMateria, null);
        agregarFila(formulario, fila++, "Tipo de documento *", cmbTipo, null);
        agregarFila(formulario, fila++, "Autores", txtAutores, null);
        agregarFila(formulario, fila++, "Fecha", txtFecha, null);
        agregarFila(formulario, fila++, "Tags", txtTags, null);
        agregarFila(formulario, fila++, "Descripción", new JScrollPane(txtDescripcion), null);
        agregarFila(formulario, fila++, "Documento *", txtDocumento, btnDocumento);
        agregarFila(formulario, fila++, "Carátula", txtCaratula, btnCaratula);

        Estilos.aplicar(lblPreview, "arc:12; background:#F3F4F6; foreground:$Label.disabledForeground");
        lblPreview.setOpaque(true);
        lblPreview.setPreferredSize(new java.awt.Dimension(ANCHO_PREVIEW + 4, ALTO_PREVIEW + 4));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = fila;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 4, 4, 12);
        formulario.add(lblPreview, c);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        Estilos.botonPrincipal(btnGuardar);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(0xE5E7EB)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        setLayout(new BorderLayout());
        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnGuardar);

        pack();
        setLocationRelativeTo(owner);
    }

    static Catalogo cargarCatalogo(CatalogoDao catalogoDao) {
        try {
            return catalogoDao.getCatalogo();
        } catch (IOException e) {
            e.printStackTrace();
            return new Catalogo();
        }
    }

    static void rellenarCombo(JComboBox<String> combo, Categoria categoria, Catalogo catalogo, String curso,
            String seleccionado) {
        combo.removeAllItems();
        catalogo.getValores(categoria, curso).forEach(combo::addItem);
        combo.setSelectedIndex(-1);
        if (seleccionado != null) {
            seleccionarIgnorandoMayusculas(combo, seleccionado);
        }
        combo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, combo.getItemCount() == 0
                ? "No hay " + categoria.getPlural().toLowerCase(Locale.ROOT) + " (agregue desde "
                        + categoria.getConfiguradoEn() + ")"
                : "Seleccione " + categoria.getConArticulo());
        combo.setEnabled(combo.getItemCount() > 0);
    }

    static void rellenarMaterias(JComboBox<String> cmbCurso, JComboBox<String> cmbMateria, Catalogo catalogo,
            String seleccionada) {
        String curso = (String) cmbCurso.getSelectedItem();
        if (curso == null) {
            cmbMateria.removeAllItems();
            cmbMateria.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Seleccione primero un curso");
            cmbMateria.setEnabled(false);
            return;
        }
        rellenarCombo(cmbMateria, Categoria.MATERIA, catalogo, curso, seleccionada);
    }

    private static boolean seleccionarIgnorandoMayusculas(JComboBox<String> combo, String valor) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (CatalogoDao.mismoNombre(combo.getItemAt(i), valor)) {
                combo.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    private String valorObligatorio(JComboBox<String> combo, Categoria categoria) {
        String valor = (String) combo.getSelectedItem();
        if (valor == null) {
            mostrarError(combo.getItemCount() == 0
                    ? "No hay " + categoria.getPlural().toLowerCase(Locale.ROOT)
                            + ". Agregue primero desde " + categoria.getConfiguradoEn() + "."
                    : "Debe seleccionar " + categoria.getConArticulo() + ".");
        }
        return valor;
    }

    private static void agregarFila(JPanel panel, int fila, String etiqueta, JComponent campo, JButton boton) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(4, 4, 4, 12);
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridx = 0;
        JLabel lblEtiqueta = new JLabel(etiqueta);
        Estilos.textoSecundario(lblEtiqueta);
        panel.add(lblEtiqueta, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(campo, c);

        if (boton != null) {
            c.gridx = 2;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            panel.add(boton, c);
        }
    }

    private void elegirDocumento() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar documento");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        ficheroDocumento = chooser.getSelectedFile();
        txtDocumento.setText(ficheroDocumento.getAbsolutePath());

        String nombre = ficheroDocumento.getName();
        int punto = nombre.lastIndexOf('.');
        if (txtNombre.getText().isBlank()) {
            txtNombre.setText(punto > 0 ? nombre.substring(0, punto) : nombre);
        }
        if (cmbTipo.getSelectedIndex() < 0 && punto > 0) {
            seleccionarIgnorandoMayusculas(cmbTipo, nombre.substring(punto + 1));
        }
    }

    private void elegirCaratula() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar carátula");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", ImageIO.getReaderFileSuffixes()));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File seleccionado = chooser.getSelectedFile();
        try {
            BufferedImage imagen = ImageIO.read(seleccionado);
            if (imagen == null) {
                throw new IOException("formato no soportado");
            }
            double escala = Math.min((double) ANCHO_PREVIEW / imagen.getWidth(),
                    (double) ALTO_PREVIEW / imagen.getHeight());
            lblPreview.setText(null);
            lblPreview.setIcon(new ImageIcon(imagen.getScaledInstance(
                    Math.max(1, (int) (imagen.getWidth() * escala)),
                    Math.max(1, (int) (imagen.getHeight() * escala)), Image.SCALE_SMOOTH)));
            ficheroCaratula = seleccionado;
            txtCaratula.setText(seleccionado.getAbsolutePath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo leer la imagen: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String fecha = txtFecha.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError("El nombre es obligatorio.");
            return;
        }
        String curso = valorObligatorio(cmbCurso, Categoria.CURSO);
        if (curso == null) {
            return;
        }
        String materia = valorObligatorio(cmbMateria, Categoria.MATERIA);
        if (materia == null) {
            return;
        }
        String tipo = valorObligatorio(cmbTipo, Categoria.TIPO);
        if (tipo == null) {
            return;
        }
        if (ficheroDocumento == null) {
            mostrarError("Debe seleccionar el documento.");
            return;
        }
        if (!fecha.isEmpty()) {
            try {
                LocalDate.parse(fecha);
            } catch (DateTimeParseException e) {
                mostrarError("La fecha debe tener el formato AAAA-MM-DD.");
                return;
            }
        }
        String tags = Arrays.stream(txtTags.getText().split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.joining(","));

        String autores = txtAutores.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        File documentoOrigen = ficheroDocumento;
        File caratulaOrigen = ficheroCaratula;

        btnGuardar.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws IOException, DocumentoDuplicadoException {
                if (catalogoDao.existeDocumentoConNombre(nombre)) {
                    throw new DocumentoDuplicadoException();
                }
                Path documentoCopiado = copiar(documentoOrigen, Locations.getDocumentsFolder());
                Path caratulaCopiada = null;
                try {
                    if (caratulaOrigen != null) {
                        caratulaCopiada = copiar(caratulaOrigen, Locations.getCoversFolder());
                    }
                    catalogoDao.insertDocumento(new Documento(0, nombre, autores, descripcion, fecha,
                            caratulaCopiada == null ? null : caratulaCopiada.getFileName().toString(),
                            documentoCopiado.getFileName().toString(), tags, curso, materia, tipo));
                } catch (IOException e) {
                    Files.deleteIfExists(documentoCopiado);
                    if (caratulaCopiada != null) {
                        Files.deleteIfExists(caratulaCopiada);
                    }
                    throw e;
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    dispose();
                    alGuardar.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    btnGuardar.setEnabled(true);
                    if (e.getCause() instanceof DocumentoDuplicadoException) {
                        JOptionPane.showMessageDialog(AgregarDocumentoDialog.this,
                                "Ya existe un documento con el título \"" + nombre + "\".",
                                "Documento duplicado", JOptionPane.WARNING_MESSAGE);
                        txtNombre.requestFocusInWindow();
                        return;
                    }
                    e.getCause().printStackTrace();
                    mostrarError("No se pudo guardar el documento: " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    private static Path copiar(File origen, String carpeta) throws IOException {
        String nombre = origen.getName();
        int punto = nombre.lastIndexOf('.');
        String extension = punto > 0 ? nombre.substring(punto).toLowerCase(Locale.ROOT) : "";
        String hash = Funciones.md5(origen.toPath());
        Path destino = Path.of(carpeta, hash + extension);
        for (int i = 1; Files.exists(destino); i++) {
            destino = Path.of(carpeta, hash + "-" + i + extension);
        }
        return Files.copy(origen.toPath(), destino, StandardCopyOption.COPY_ATTRIBUTES);
    }

    private static final class DocumentoDuplicadoException extends Exception {
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
