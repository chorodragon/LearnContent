package com.proyecto.catalogodocumentos;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

import com.formdev.flatlaf.FlatClientProperties;
import com.proyecto.catalogodocumentos.datos.CatalogoDao;
import com.proyecto.catalogodocumentos.entidades.Categoria;
import com.proyecto.catalogodocumentos.util.Estilos;

public final class ConfigurarDialog extends JDialog {

    private static final Categoria CATEGORIA = Categoria.TIPO;

    private final CatalogoDao catalogoDao = new CatalogoDao();

    private final DefaultListModel<String> modelo = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modelo);
    private final JTextField txtNuevo = new JTextField(12);
    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnEliminar = new JButton("Eliminar");

    public ConfigurarDialog(JFrame owner, Runnable alCerrar) {
        super(owner, "Configurar", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                alCerrar.run();
            }
        });

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));
        int fila = agregarTitulo(contenido, 0, CATEGORIA.getPlural(),
                "Cada documento debe tener " + CATEGORIA.getConArticulo()
                        + ". Los cursos y las materias se configuran en LearnCenterHub");

        txtNuevo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, CATEGORIA.getNuevo());
        txtNuevo.addActionListener(e -> agregar());
        btnAgregar.setToolTipText("Agregar " + CATEGORIA.getConArticulo());
        btnAgregar.addActionListener(e -> agregar());
        Estilos.botonPrincipal(btnAgregar);
        JPanel filaNuevo = new JPanel(new BorderLayout(8, 0));
        filaNuevo.add(txtNuevo, BorderLayout.CENTER);
        filaNuevo.add(btnAgregar, BorderLayout.EAST);
        contenido.add(filaNuevo, restricciones(fila++));

        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setVisibleRowCount(8);
        lista.addListSelectionListener(e -> btnEliminar.setEnabled(lista.getSelectedIndex() >= 0));
        GridBagConstraints c = restricciones(fila++);
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        contenido.add(new JScrollPane(lista), c);

        btnEliminar.setEnabled(false);
        btnEliminar.addActionListener(e -> eliminar());
        Estilos.botonPeligro(btnEliminar);
        c = restricciones(fila);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        contenido.add(btnEliminar, c);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(0xE5E7EB)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        botones.add(btnCerrar);

        setLayout(new BorderLayout());
        add(contenido, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        cargar(null);
        pack();
        setSize(Math.max(getWidth(), 420), getHeight());
        setLocationRelativeTo(owner);
    }

    private static int agregarTitulo(JPanel panel, int fila, String titulo, String ayuda) {
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 14f));
        GridBagConstraints c = restricciones(fila++);
        c.insets = new Insets(0, 4, 0, 4);
        panel.add(lblTitulo, c);

        JLabel lblAyuda = new JLabel(ayuda);
        lblAyuda.setFont(lblAyuda.getFont().deriveFont(11f));
        Estilos.textoSecundario(lblAyuda);
        c = restricciones(fila++);
        c.insets = new Insets(0, 4, 6, 4);
        panel.add(lblAyuda, c);
        return fila;
    }

    private static GridBagConstraints restricciones(int fila) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = fila;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 4, 4, 4);
        return c;
    }

    private <T> void enSegundoPlano(Callable<T> tarea, Consumer<T> alTerminar, String mensajeError,
            Runnable alFallar) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return tarea.call();
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    alTerminar.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    e.getCause().printStackTrace();
                    alFallar.run();
                    mostrarError(mensajeError + ": " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void cargar(String seleccionar) {
        enSegundoPlano(catalogoDao::getTiposDocumentos, (List<String> valores) -> {
            modelo.clear();
            valores.forEach(modelo::addElement);
            if (seleccionar != null) {
                lista.setSelectedValue(seleccionar, true);
            }
        }, "No se pudieron leer " + CATEGORIA.getPlural().toLowerCase(Locale.ROOT), modelo::clear);
    }

    private void agregar() {
        String valor = txtNuevo.getText().trim();
        if (valor.isEmpty()) {
            txtNuevo.requestFocusInWindow();
            return;
        }
        btnAgregar.setEnabled(false);
        enSegundoPlano(() -> catalogoDao.insertTipoDocumento(valor), insertado -> {
            btnAgregar.setEnabled(true);
            if (!insertado) {
                JOptionPane.showMessageDialog(this, "Ya existe " + CATEGORIA.getConArticulo() + " \"" + valor + "\".",
                        "Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            txtNuevo.setText("");
            cargar(valor);
        }, "No se pudo guardar", () -> btnAgregar.setEnabled(true));
    }

    private void eliminar() {
        String valor = lista.getSelectedValue();
        if (valor == null) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar " + CATEGORIA.getConArticuloDefinido() + " \"" + valor + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        btnEliminar.setEnabled(false);
        enSegundoPlano(() -> catalogoDao.deleteTipoDocumento(valor), enUso -> {
            if (enUso > 0) {
                btnEliminar.setEnabled(true);
                JOptionPane.showMessageDialog(this,
                        "No se puede eliminar: está en uso por " + enUso
                                + (enUso == 1 ? " documento." : " documentos."),
                        CATEGORIA.getNombre() + " en uso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cargar(null);
        }, "No se pudo eliminar", () -> btnEliminar.setEnabled(true));
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
