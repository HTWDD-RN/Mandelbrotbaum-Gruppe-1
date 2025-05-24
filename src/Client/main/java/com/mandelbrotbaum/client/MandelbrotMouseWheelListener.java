package com.mandelbrotbaum.client;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class MandelbrotMouseWheelListener implements MouseWheelListener {
    private final Model model;
    private final MandelbrotPresenter presenter;
    private final MandelbrotView view;
    
    /**
     * Constructor of MandelbrotMouseListener
     * @param presenter
     * @param model
     */
    public MandelbrotMouseWheelListener(MandelbrotPresenter presenter, Model model, MandelbrotView view) {
        this.presenter = presenter;
        this.model = model;
        this.view = view;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            System.out.println(e.getPreciseWheelRotation() < 0.0 ? "incrementing zoomfactor" : "decrementing zoomfactor");
            view.incrZoomFactor((int)e.getPreciseWheelRotation()*-1);
        }
    }
}
