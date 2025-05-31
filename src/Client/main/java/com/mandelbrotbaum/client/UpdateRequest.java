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
        // - if image changed: show image
        // - if Status-label changed: show status label
        // - if play-button visiblity changed: set button visiblity
        // - ... anz Worker

        boolean needRepaint = false;
        if(model.changedJobStatus){
            view.jobStatusLblSetText(model.jobStatusText());
            model.changedJobStatus = false;
            //needRepaint = true;
        }
        if(model.changedPlayButtonVisiblity){
            view.playButtonSetVisiblity(model.playButtonVisible);
            model.changedPlayButtonVisiblity = false;
            //needRepaint = true;
        }
        if(model.changedCntWorkers){
            view.workersCountLblSetText("Anzahl Worker(" + model.cntWorkers + ")");
            model.changedCntWorkers = false;
            //needRepaint = true;
        }
        if(model.changedImage){
            model.drawMandelbrot();
            model.changedImage = false;
            needRepaint = true;
        }
        if(needRepaint){
            view.repaint();
        }

        Thread t = new Thread(){
                public void run(){
                    try{
                        model.checkCalculation();
                    }
                    catch(Exception e){
                        System.out.println("Ausnahme in UpdateRequest.run(): " + e.getMessage());
                    }
                }};
            t.start();
    }
}