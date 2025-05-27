package com.mandelbrotbaum.client;

import java.awt.event.ActionListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.transform.Source;

import java.awt.event.ActionEvent;
import javax.swing.*;

public class MandelbrotPresenter implements ActionListener, ChangeListener {

    private final Model model;
    private final MandelbrotView view;
    private final Ticker ticker;
    private boolean isUpdating = false;

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
    public void stateChanged(ChangeEvent e){
        if (isUpdating) return;

        Object source = e.getSource();
        System.out.println("Bin im ChangeEvent");
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
            if (numberIteration > 1 || numberIteration < 1000){
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
        }
        else if (e.getSource() == view.getResolutionComboBox()){
             String selected = (String) view.getResolutionComboBox().getSelectedItem();
             System.out.println(selected);
        }
    }
}