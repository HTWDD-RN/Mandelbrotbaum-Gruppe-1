package com.mandelbrotbaum.client;

import java.awt.event.ActionListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.ActionEvent;

public class MandelbrotPresenter implements ActionListener, ChangeListener {

    private final Model model;
    private final MandelbrotView view;
    private Ticker ticker = null;
    private boolean isUpdating = false;

    /**
     * Constructor of presenter
     * @param model
     * @param view
     */
    public MandelbrotPresenter(Model model, MandelbrotView view) {
        this.model = model;
        this.view = view;
    }

    /**
     * shows the GUI
     */
    public void start() {
        view.initView();
        //ticker.start();
    }

    public void startGenMandelbrot(int x, int y) {
        if (ticker != null) {ticker.interrupt();}
        try {
            ticker = new Ticker(model, view, x, y, model.getWidth(), model.getHeight(), (int)view.getWorkSpinner().getValue(), (int)view.getIterationSpinner().getValue(), view.getZoomFactor(), (int)view.getNumberOfStepsSpinner().getValue());
            //ticker.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e){
        if (isUpdating) return;

        Object source = e.getSource();
        // System.out.println("Bin im ChangeEvent");
        if (source == view.getNumberOfStepsSpinner()) {
            int numberSteps = (Integer) view.getNumberOfStepsSpinner().getValue();
            if (numberSteps < 1 || numberSteps > 100) {
                isUpdating = true;
                view.getNumberOfStepsSpinner().setValue(100);
                isUpdating = false;
            }
        }
        else if (source == view.getWorkSpinner()) {
            int numberWorker = (Integer) view.getWorkSpinner().getValue();
            if (numberWorker < 2 || numberWorker > 16) {
                isUpdating = true;
                view.getWorkSpinner().setValue(4);
                isUpdating = false;
            }
        }
        else if (source == view.getIterationSpinner()) {
            int numberIteration = (Integer) view.getIterationSpinner().getValue();
            if (numberIteration < 1 || numberIteration > 1000){
                isUpdating = true;
                view.getIterationSpinner().setValue(100);
                isUpdating = false;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("Ausführen")) {
            System.out.println("pressed: " + action);
            System.out.println("Zoomfactor: " + view.getZoomFactor());
            int x;
            int y;
            try { // input validation for x coordinate
                x = Integer.parseInt(view.getXTextFieldText()) >= 0 ? Integer.parseInt(view.getXTextFieldText()) : 0;
            } catch (NumberFormatException exeption) {
                x = 0;
            }
            try { // input validation for y coordinate
                y = Integer.parseInt(view.getYTextFieldText()) >= 0 ? Integer.parseInt(view.getYTextFieldText()) : 0;
            } catch (NumberFormatException exeption) {
                y = 0;
            }

            view.setXTextField(Integer.toString(x));
            view.setYTextField(Integer.toString(y));
            this.startGenMandelbrot(x, y);

            model.isStopped = !model.isStopped;
            model.zoomFaktor = view.getZoomFactor();
        }
        else if (e.getSource() == view.getResolutionComboBox()){
             String selected = (String) view.getResolutionComboBox().getSelectedItem();
             System.out.println(selected);
        }
    }
}