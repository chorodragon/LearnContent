package com.proyecto.catalogodocumentos.util;

import javax.swing.JButton;
import javax.swing.JComponent;

import com.formdev.flatlaf.FlatClientProperties;

public final class Estilos {

    public static final String ACENTO = "#3B82F6";
    public static final String PELIGRO = "#DC2626";

    private Estilos() {
    }

    public static void aplicar(JComponent componente, String estilo) {
        componente.putClientProperty(FlatClientProperties.STYLE, estilo);
    }

    public static void botonPrincipal(JButton boton) {
        aplicar(boton, "background:" + ACENTO + "; foreground:#FFFFFF; borderWidth:0; focusWidth:0;"
                + "hoverBackground:darken(" + ACENTO + ",6%); pressedBackground:darken(" + ACENTO + ",12%);"
                + "font:bold; margin:5,14,5,14");
    }

    public static void botonPeligro(JButton boton) {
        aplicar(boton, "foreground:" + PELIGRO + "; borderColor:lighten(" + PELIGRO + ",30%);"
                + "hoverBackground:lighten(" + PELIGRO + ",48%); hoverBorderColor:" + PELIGRO + "; focusWidth:0");
    }

    public static void textoSecundario(JComponent componente) {
        aplicar(componente, "foreground:$Label.disabledForeground");
    }
}
