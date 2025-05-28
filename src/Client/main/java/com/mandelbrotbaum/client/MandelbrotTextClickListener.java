package com.mandelbrotbaum.client;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextField;

public class MandelbrotTextClickListener implements MouseListener {

    private final MandelbrotView view;

    /**
     * Constructor of MandelbrotTextClickListener
     * @param view
     */
    public MandelbrotTextClickListener(MandelbrotView view) {
        this.view = view;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == e.BUTTON1) {
            ((JTextField)e.getComponent()).setText("");
        }
    }

    // following not used
    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}
}
