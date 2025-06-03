package com.mandelbrotbaum.server;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class Ticker extends Thread {
    private final MasterImpl master;
    private static final long UPDATE_INTERVAL = 200;

    public Ticker(MasterImpl master){
        this.master = master;
    }

    public void run() {
        while(true){
            try{
                master.checkWorkers();
                Thread.sleep(UPDATE_INTERVAL);
            } catch (InterruptedException e) {
                //Thread.currentThread().interrupt();
            }
        }
    }
}