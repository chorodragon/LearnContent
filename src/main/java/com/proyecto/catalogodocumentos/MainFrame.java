package com.proyecto.catalogodocumentos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.proyecto.catalogodocumentos.datos.CatalogoDao;
import com.proyecto.catalogodocumentos.entidades.Catalogo;
import com.proyecto.catalogodocumentos.entidades.Categoria;
import com.proyecto.catalogodocumentos.entidades.Documento;
import com.proyecto.catalogodocumentos.util.Estilos;
import com.proyecto.catalogodocumentos.util.Locations;

public final class MainFrame extends JFrame {

    private static final int ITEMS_POR_PAGINA = 6;
    private static final int COLUMNAS = 3;
    private static final int MAX_TAGS = 4;
    private static final int ANCHO_CARATULA = 140;
    private static final int ALTO_CARATULA = 200;
    private static final String ESTILO_TARJETA = "arc:16; background:#FFFFFF;"
            + "border:12,12,12,12,#E5E7EB,1,16";
    private static final String ESTILO_TARJETA_HOVER = "arc:16; background:#FFFFFF;"
            + "border:12,12,12,12," + Estilos.ACENTO + ",1,16";
    private static final String TODOS_LOS_CURSOS = "Todos los cursos";
    private static final String TODAS_LAS_MATERIAS = "Todas las materias";
    private static final ImageIcon ICONO_SIN_CARATULA = new ImageIcon(crearCaratulaVacia());

    private final CatalogoDao catalogoDao = new CatalogoDao();
    private final Map<String, ImageIcon> cacheCaratulas = new ConcurrentHashMap<>();

    private final JTextField txtBuscar = new JTextField(30);
    private final JComboBox<String> cmbCurso = new JComboBox<>(new String[] { TODOS_LOS_CURSOS });
    private final JComboBox<String> cmbMateria = new JComboBox<>(new String[] { TODAS_LAS_MATERIAS });
    private final JPanel panelCatalogo = new JPanel(new GridLayout(0, COLUMNAS, 16, 16));
    private final JPanel panelPaginas = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
    private final JButton btnRefrescar = new JButton("Refrescar");

    private Catalogo catalogo = new Catalogo();
    private List<Documento> documentos = new ArrayList<>();
    private List<Documento> documentosFiltrados = new ArrayList<>();
    private int paginaActual = 0;
    private boolean cargandoFiltros = false;

    public MainFrame() {
        super("Catálogo de Documentos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 420));

        setLayout(new BorderLayout());
        add(crearBarraSuperior(), BorderLayout.NORTH);

        panelCatalogo.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        add(panelCatalogo, BorderLayout.CENTER);
        add(panelPaginas, BorderLayout.SOUTH);

        setSize(960, 760);
        setLocationRelativeTo(null);
        refrescar();
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout(8, 0));
        barra.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));

        txtBuscar.setToolTipText("Buscar por nombre, autores, tipo de documento o tags");
        txtBuscar.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar por nombre, autores, tipo o tags…");
        txtBuscar.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        txtBuscar.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        Estilos.aplicar(txtBuscar, "margin:4,6,4,6");
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }
        });

        cmbCurso.setToolTipText("Filtrar por curso");
        cmbCurso.addActionListener(e -> {
            if (!cargandoFiltros) {
                cargarMaterias();
                filtrar();
            }
        });
        cmbMateria.setToolTipText("Filtrar por materia del curso");
        cmbMateria.setEnabled(false);
        cmbMateria.addActionListener(e -> {
            if (!cargandoFiltros) {
                filtrar();
            }
        });

        btnRefrescar.addActionListener(e -> refrescar());

        JButton btnAgregar = new JButton("+ Agregar");
        Estilos.botonPrincipal(btnAgregar);
        btnAgregar.addActionListener(e -> new AgregarDocumentoDialog(this, this::refrescar).setVisible(true));

        JButton btnConfigurar = new JButton("Configurar");
        btnConfigurar.addActionListener(e -> new ConfigurarDialog(this, this::refrescar).setVisible(true));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnRefrescar);
        botones.add(btnAgregar);
        botones.add(btnConfigurar);

        JPanel busqueda = new JPanel(new BorderLayout(8, 0));
        busqueda.add(txtBuscar, BorderLayout.CENTER);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(cmbCurso);
        filtros.add(cmbMateria);
        busqueda.add(filtros, BorderLayout.EAST);

        barra.add(busqueda, BorderLayout.CENTER);
        barra.add(botones, BorderLayout.EAST);
        return barra;
    }

    private void refrescar() {
        btnRefrescar.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Catalogo, Void>() {
            @Override
            protected Catalogo doInBackground() throws IOException {
                return catalogoDao.getCatalogo();
            }

            @Override
            protected void done() {
                Catalogo catalogo;
                try {
                    catalogo = get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    catalogo = new Catalogo();
                } catch (ExecutionException e) {
                    e.getCause().printStackTrace();
                    catalogo = new Catalogo();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "No se pudo leer el catálogo:\n" + e.getCause().getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                MainFrame.this.catalogo = catalogo;
                documentos = catalogo.getDocumentos();
                cargarCursos();
                cacheCaratulas.clear();
                filtrar();
                btnRefrescar.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());
            }
        }.execute();
    }

    private void cargarCursos() {
        rellenarFiltro(cmbCurso, TODOS_LOS_CURSOS, catalogo.getValores(Categoria.CURSO, null));
        cargarMaterias();
    }

    private void cargarMaterias() {
        String curso = getCursoFiltrado();
        rellenarFiltro(cmbMateria, TODAS_LAS_MATERIAS,
                curso == null ? List.of() : catalogo.getValores(Categoria.MATERIA, curso));
        cmbMateria.setEnabled(curso != null);
    }

    private void rellenarFiltro(JComboBox<String> combo, String todos, List<String> valores) {
        Object seleccionado = combo.getSelectedItem();
        cargandoFiltros = true;
        combo.removeAllItems();
        combo.addItem(todos);
        valores.forEach(combo::addItem);
        combo.setSelectedItem(valores.contains(seleccionado) ? seleccionado : todos);
        cargandoFiltros = false;
    }

    private String getCursoFiltrado() {
        return cmbCurso.getSelectedIndex() > 0 ? (String) cmbCurso.getSelectedItem() : null;
    }

    private void filtrar() {
        String texto = txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
        String curso = getCursoFiltrado();
        String materia = cmbMateria.getSelectedIndex() > 0 ? (String) cmbMateria.getSelectedItem() : null;
        documentosFiltrados = new ArrayList<>();
        for (Documento documento : documentos) {
            if (curso != null && !CatalogoDao.mismoNombre(documento.getCurso(), curso)
                    || materia != null && !CatalogoDao.mismoNombre(documento.getMateria(), materia)) {
                continue;
            }
            if (texto.isEmpty() || contiene(documento.getNombre(), texto)
                    || contiene(documento.getAutores(), texto) || contiene(documento.getTags(), texto)
                    || contiene(documento.getTipo(), texto)) {
                documentosFiltrados.add(documento);
            }
        }
        mostrarPagina(0);
    }

    private static boolean contiene(String valor, String texto) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(texto);
    }

    private int getTotalPaginas() {
        return Math.max(1, (documentosFiltrados.size() + ITEMS_POR_PAGINA - 1) / ITEMS_POR_PAGINA);
    }

    private void mostrarPagina(int pagina) {
        paginaActual = Math.max(0, Math.min(pagina, getTotalPaginas() - 1));

        panelCatalogo.removeAll();
        int inicio = paginaActual * ITEMS_POR_PAGINA;
        int fin = Math.min(inicio + ITEMS_POR_PAGINA, documentosFiltrados.size());
        for (int i = inicio; i < fin; i++) {
            panelCatalogo.add(crearItem(documentosFiltrados.get(i)));
        }
        for (int i = fin - inicio; i < ITEMS_POR_PAGINA; i++) {
            panelCatalogo.add(new JPanel());
        }
        panelCatalogo.revalidate();
        panelCatalogo.repaint();

        actualizarPaginacion();
    }

    private void actualizarPaginacion() {
        panelPaginas.removeAll();
        int totalPaginas = getTotalPaginas();

        JButton btnAnterior = new JButton("‹");
        estiloPaginacion(btnAnterior);
        btnAnterior.setEnabled(paginaActual > 0);
        btnAnterior.addActionListener(e -> mostrarPagina(paginaActual - 1));
        panelPaginas.add(btnAnterior);

        for (int i = 0; i < totalPaginas; i++) {
            final int pagina = i;
            JToggleButton btnPagina = new JToggleButton(String.valueOf(i + 1), i == paginaActual);
            estiloPaginacion(btnPagina);
            btnPagina.addActionListener(e -> mostrarPagina(pagina));
            panelPaginas.add(btnPagina);
        }

        JButton btnSiguiente = new JButton("›");
        estiloPaginacion(btnSiguiente);
        btnSiguiente.setEnabled(paginaActual < totalPaginas - 1);
        btnSiguiente.addActionListener(e -> mostrarPagina(paginaActual + 1));
        panelPaginas.add(btnSiguiente);

        panelPaginas.revalidate();
        panelPaginas.repaint();
    }

    private static void estiloPaginacion(AbstractButton boton) {
        boton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        Estilos.aplicar(boton, "selectedBackground:" + Estilos.ACENTO + "; selectedForeground:#FFFFFF;"
                + "toolbar.selectedBackground:" + Estilos.ACENTO + "; margin:4,10,4,10; arc:999");
    }

    private JPanel crearItem(Documento documento) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        Estilos.aplicar(item, ESTILO_TARJETA);

        JLabel lblCaratula = new JLabel();
        lblCaratula.setAlignmentX(Component.CENTER_ALIGNMENT);
        cargarCaratulaEnSegundoPlano(documento.getCaratula(), lblCaratula);
        item.add(Box.createVerticalGlue());
        item.add(lblCaratula);
        item.add(Box.createVerticalStrut(6));

        JLabel lblNombre = crearEtiquetaCentrada(valorOVacio(documento.getNombre()));
        lblNombre.setFont(lblNombre.getFont().deriveFont(Font.BOLD, 15f));
        item.add(lblNombre);
        item.add(Box.createVerticalStrut(2));

        JLabel lblAutores = crearEtiquetaCentrada(valorOVacio(documento.getAutores()));
        lblAutores.setFont(lblAutores.getFont().deriveFont(12f));
        Estilos.textoSecundario(lblAutores);
        item.add(lblAutores);
        item.add(Box.createVerticalStrut(6));

        JPanel panelTags = crearPanelTags(documento.getTags());
        panelTags.setMaximumSize(new Dimension(Integer.MAX_VALUE, panelTags.getPreferredSize().height));
        item.add(panelTags);
        item.add(Box.createVerticalGlue());

        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Estilos.aplicar(item, ESTILO_TARJETA_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!item.contains(e.getPoint())) {
                    Estilos.aplicar(item, ESTILO_TARJETA);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                new DocumentoDialog(MainFrame.this, documento, MainFrame.this::refrescar).setVisible(true);
            }
        });
        return item;
    }

    private static JLabel crearEtiquetaCentrada(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setToolTipText(texto);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private static JPanel crearPanelTags(String tags) {
        JPanel panelTags = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
        panelTags.setOpaque(false);
        panelTags.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (tags == null) {
            return panelTags;
        }
        List<String> lista = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
        for (String tag : lista.subList(0, Math.min(MAX_TAGS, lista.size()))) {
            JLabel lblTag = new JLabel(tag);
            lblTag.setFont(lblTag.getFont().deriveFont(11f));
            lblTag.setOpaque(true);
            lblTag.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            Estilos.aplicar(lblTag, "arc:999; background:#EFF6FF; foreground:#1D4ED8");
            panelTags.add(lblTag);
        }
        if (lista.size() > MAX_TAGS) {
            JLabel lblMas = new JLabel("+" + (lista.size() - MAX_TAGS));
            lblMas.setFont(lblMas.getFont().deriveFont(11f));
            Estilos.textoSecundario(lblMas);
            lblMas.setToolTipText(String.join(", ", lista));
            panelTags.add(lblMas);
        }
        return panelTags;
    }

    private static String valorOVacio(String valor) {
        return valor == null ? "" : valor;
    }

    private void cargarCaratulaEnSegundoPlano(String caratula, JLabel lblCaratula) {
        String clave = valorOVacio(caratula);
        ImageIcon enCache = cacheCaratulas.get(clave);
        if (enCache != null) {
            lblCaratula.setIcon(enCache);
            return;
        }
        lblCaratula.setIcon(ICONO_SIN_CARATULA);
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                return cacheCaratulas.computeIfAbsent(clave, MainFrame::cargarCaratula);
            }

            @Override
            protected void done() {
                try {
                    lblCaratula.setIcon(get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private static ImageIcon cargarCaratula(String caratula) {
        if (!caratula.isBlank()) {
            File fichero = new File(Locations.getCoversFolder(), caratula);
            try {
                BufferedImage imagen = fichero.isFile() ? ImageIO.read(fichero) : null;
                if (imagen != null) {
                    return new ImageIcon(escalar(imagen));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ICONO_SIN_CARATULA;
    }

    private static Image escalar(BufferedImage imagen) {
        double escala = Math.min((double) ANCHO_CARATULA / imagen.getWidth(),
                (double) ALTO_CARATULA / imagen.getHeight());
        int ancho = Math.max(1, (int) (imagen.getWidth() * escala));
        int alto = Math.max(1, (int) (imagen.getHeight() * escala));
        return imagen.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
    }

    private static BufferedImage crearCaratulaVacia() {
        BufferedImage imagen = new BufferedImage(ANCHO_CARATULA, ALTO_CARATULA, BufferedImage.TYPE_INT_RGB);
        var g = imagen.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, ANCHO_CARATULA, ALTO_CARATULA);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(0xF3F4F6));
        g.fillRoundRect(0, 0, ANCHO_CARATULA, ALTO_CARATULA, 12, 12);
        g.setColor(new Color(0x9CA3AF));
        String texto = "Sin carátula";
        int x = (ANCHO_CARATULA - g.getFontMetrics().stringWidth(texto)) / 2;
        g.drawString(texto, x, ALTO_CARATULA / 2);
        g.dispose();
        return imagen;
    }

}
