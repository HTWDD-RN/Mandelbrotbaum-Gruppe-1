package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class UpdateRequest implements Runnable {
    private final Model model;
    private final MandelbrotView view;

    public UpdateRequest(Model model, MandelbrotView view) {
        this.model = model;
        this.view = view;
    }

    public void run() {
        if(model.dbgDrawAnz <= model.dbgRepaintAnz){
            model.drawMandelbrot();
        }
        else{
            view.repaint();
        }
    }
}