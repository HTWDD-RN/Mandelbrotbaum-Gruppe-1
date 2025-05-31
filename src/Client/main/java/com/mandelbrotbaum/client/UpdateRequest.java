package com.mandelbrotbaum.client;

import java.awt.image.BufferedImage;

public class UpdateRequest implements Runnable {
    private final Model model;
    private final MandelbrotView view;
    private BufferedImage image;

    public UpdateRequest(Model model, MandelbrotView view) {
        this.model = model;
        this.view = view;
    }

    /**
     * sets the image that the update request will send to the view
     * @param image
     */
    public void setImage(BufferedImage image) {this.image = image;}

    public void run() {
        //if(model.dbgDrawAnz <= model.dbgRepaintAnz){
        //    model.drawMandelbrot();
        //}
        //else{
        //    view.repaint();
        //}
        model.setImage(image);
        view.repaint();
    }
}