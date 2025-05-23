package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class MandelbrotPresenter {

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
}