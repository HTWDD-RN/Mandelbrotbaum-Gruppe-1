package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.rmi.Naming;

import javax.swing.*;

import org.w3c.dom.css.Counter;

import com.mandelbrotbaum.sharedobjects.MasterInterface;

public class Mandelbrotmenge {

    public static void main(String[] args) throws InterruptedException {
        String serverHost = "localhost:1099";
        if(args.length !=1){
            System.out.println(("Argument missing.\n"
                             + "Command: <Name or IP of Server-Host>[:portNr]\n"
                             + "assuming host name is: " + serverHost));
            
        }
        else{
            serverHost = args[0];
        }

        //connection to RMI-Server-Master happenes in the getMaster method of model
        Model model = new Model(1024,768, serverHost);
        MandelbrotView view = new MandelbrotView(model);
        MandelbrotPresenter presenter = new MandelbrotPresenter(model, view);
        MandelbrotMouseListener mouseListener = new MandelbrotMouseListener(presenter, model, view);
        MandelbrotMouseWheelListener mouseWheelListener = new MandelbrotMouseWheelListener(presenter, model, view);
        view.setAllListeners(presenter, mouseListener, mouseWheelListener);
        presenter.start();

    }

}