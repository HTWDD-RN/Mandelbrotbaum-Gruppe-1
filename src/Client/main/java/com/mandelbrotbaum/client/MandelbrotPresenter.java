package com.mandelbrotbaum.client;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class MandelbrotPresenter implements ActionListener {

    private final Model model;
    private final MandelbrotView view;
    private final Ticker ticker;

    public MandelbrotPresenter(Model model, MandelbrotView view) {
        this.model = model;
        this.view = view;
        this.ticker = new Ticker(model, view);
    }

    public void start() {
        view.initView();
        ticker.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("Ausführen")) {
            System.out.println("pressed: " + action);
            System.out.println("Zoomfactor: " + view.getZoomFactor());
        }
    }
}