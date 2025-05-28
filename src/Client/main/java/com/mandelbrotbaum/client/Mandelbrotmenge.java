package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class Mandelbrotmenge {

    public static void main(String[] args) throws InterruptedException {
        Model model = new Model(1024,768);
        MandelbrotView view = new MandelbrotView(model);
        MandelbrotPresenter presenter = new MandelbrotPresenter(model, view);
        MandelbrotMouseListener mouseListener = new MandelbrotMouseListener(presenter, model, view);
        MandelbrotMouseWheelListener mouseWheelListener = new MandelbrotMouseWheelListener(presenter, model, view);
        view.setAllListeners(presenter, mouseListener, mouseWheelListener);
        presenter.start();
    }
}