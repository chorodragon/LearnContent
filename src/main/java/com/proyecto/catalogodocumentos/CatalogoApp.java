package com.proyecto.catalogodocumentos;

import java.awt.EventQueue;
import java.io.IOException;

import java.awt.Color;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;
import com.proyecto.catalogodocumentos.util.Locations;

public final class CatalogoApp {
    private CatalogoApp() {
    }

    public static void main(String[] args) {
        try {
            Locations.createDirectories();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        EventQueue.invokeLater(() -> {
            instalarApariencia();
            new MainFrame().setVisible(true);
        });
    }

    private static void instalarApariencia() {
        UIManager.put("Component.accentColor", new Color(0x3B82F6));
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 10);
        if (FlatLightLaf.setup()) {
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
        }
    }
}
