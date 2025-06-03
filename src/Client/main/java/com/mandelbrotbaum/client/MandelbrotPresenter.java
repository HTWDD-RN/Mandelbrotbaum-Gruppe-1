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
            if (numberSteps < 1 || numberSteps > 200) {
                isUpdating = true;
                //view.getNumberOfStepsSpinner().setValue(100);
                isUpdating = false;
            }
        }
        else if (source == view.getWorkSpinner()) {
            int numberWorker = (Integer) view.getWorkSpinner().getValue();
            if (numberWorker < 1 || numberWorker > 16) {
                isUpdating = true;
                view.getWorkSpinner().setValue(4);
                isUpdating = false;
            }
        }
        else if (source == view.getIterationSpinner()) {
            int numberIteration = (Integer) view.getIterationSpinner().getValue();
            if (numberIteration < 1 || numberIteration > 10000){
                isUpdating = true;
                view.getIterationSpinner().setValue(1000);
                isUpdating = false;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("Ausführen")) {
            System.out.println("pressed: " + action);
            System.out.println("Zoomfactor: " + view.getZoomFactorSpinner().getValue());
            
            //ToDo: read the following values from GUI
            int stuffenanzahl = (int) view.getNumberOfStepsSpinner().getValue();
            int iterationsanzahl = (int) view.getIterationSpinner().getValue();
            double zoompunktX = -0.34837308755059104;
            try{
                zoompunktX = Double.parseDouble(view.getXPosition().getText());
            }
            catch(Exception ex){
                view.getXPosition().setText("" + zoompunktX);
            }
            double zoompunktY = -0.6065038451823017;
            try{
                zoompunktY = Double.parseDouble(view.getYPosition().getText());
            }
            catch(Exception ex){
                view.getYPosition().setText("" + zoompunktY);
            }
            double zoomFaktor = (double) view.getZoomFactorSpinner().getValue();
            int anzWorker = (int) view.getWorkSpinner().getValue();
            int anzThreadsProWorker = 3;
            boolean divideSingleFrame = true;
            model.submitJob(stuffenanzahl,
                            iterationsanzahl,
                            zoompunktX,
                            zoompunktY,
                            zoomFaktor,
                            anzWorker,
                            anzThreadsProWorker,
                            divideSingleFrame);
        }
        else if(action.equals("Abspielen")){
            model.playButtonPressed();
        }
        else if (e.getSource() == view.getResolutionComboBox()){
             String selected = (String) view.getResolutionComboBox().getSelectedItem();
             if(selected.equals("1920 x 1080")){
                //model.setFrameSize(1920, 1080);
                model.setFrameSize(800, 600);
             }
             if(selected.equals("1024 x 768")){
                model.setFrameSize(1024, 768);
             }
             System.out.println(selected);
        }
    }
}