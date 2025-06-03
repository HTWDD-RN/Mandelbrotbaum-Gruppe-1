package com.mandelbrotbaum.worker;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class Ticker extends Thread {
    private final WorkerImpl worker;
    private static final long UPDATE_INTERVAL = 2000;

    public Ticker(WorkerImpl worker){
        this.worker = worker;
    }

    public void run() {
        while(true){
            try{
                worker.checkMaster();
                Thread.sleep(UPDATE_INTERVAL);
            }
            catch(Exception e){
                int dummy = 5;
            }
        }
    }
}