package com.mandelbrotbaum.client;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MandelbrotMouseListener implements MouseListener {
    private final Model model;
    private final MandelbrotPresenter presenter;
    private final MandelbrotView view;
    
    /**
     * Constructor of MandelbrotMouseListener
     * @param presenter
     * @param model
     */
    public MandelbrotMouseListener(MandelbrotPresenter presenter, Model model, MandelbrotView view) {
        this.presenter = presenter;
        this.model = model;
        this.view = view;
    }

    /**
     * action handler for a mouse click (means pressed AND released)
     * @param e
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == e.BUTTON1) {
            // TODO execute the function to start the image generation in presenter
            // -> will need e.getX() and e.getY(), also width and height from model
            // because of the position of the image inside the panel for it we have to calculate the position inside the image
            int correctX = e.getX()-10;
            int correctY = e.getY()-40;
            if (correctX > model.getWidth() || correctY > model.getHeight() || correctX < 0 || correctY < 0) {return;} // abort if the click is outside of the image
            System.out.println("pressed panel at: (" + correctX + "," + correctY + ")");

            //@MoD: below is an example of how the settings work in the current model. AlZaj.
            model.setCenterXfloat(correctX);
            model.setCenterYfloat(correctY);
            System.out.println("Setting Zoompunkt to: (" 
                + model.getCenterXfloat() + " | " + model.getCenterYfloat() + ")");
            System.out.println("current zoom = " + model.getZoom());

            // setting the x and y coordinate inside the textfields
            view.setXTextField(Integer.toString(correctX));
            view.setYTextField(Integer.toString(correctY));

            presenter.startGenMandelbrot(correctX, correctY);
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
