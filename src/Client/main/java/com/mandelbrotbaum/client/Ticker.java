package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class Ticker extends Thread {
    private final Model model;
    private final MandelbrotView view;
    private static final long UPDATE_INTERVAL = 100;
    private final UpdateRequest updateRequest;

    public Ticker(Model model, MandelbrotView view){
        this.model = model;
        this.view = view;
        updateRequest = new UpdateRequest(model, view);
    }

    public void run() {
        try{
            while(!isInterrupted()) {
                EventQueue.invokeLater(updateRequest);
                Thread.sleep(UPDATE_INTERVAL);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}