package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.rmi.Naming;
import java.rmi.RemoteException;

import com.mandelbrotbaum.sharedobjects.MasterInterface;

public class Ticker extends Thread {
    private final Model model;
    private final MandelbrotView view;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int workerCount;
    private final int maxIterations;
    private final int stepCount;
    private final double zoom;
    private final MasterInterface server;
    private static final long UPDATE_INTERVAL = 100;
    private final UpdateRequest updateRequest;

    public Ticker(Model model, MandelbrotView view, int x, int y, int width, int height, int workerCount, int maxIterations, double zoom, int stepCount) throws Exception{
        this.model = model;
        this.view = view;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.workerCount = workerCount;
        this.maxIterations = maxIterations;
        this.zoom = zoom;
        this.stepCount = stepCount;
        updateRequest = new UpdateRequest(model, view);

        System.out.println("Created Ticker with: " +
        this.x + " " +
        this.y + " " +
        this.width + " " +
        this.height + " " +
        this.workerCount + " " +
        this.maxIterations + " " +
        this.zoom + " " +
        this.stepCount);

        this.server = (MasterInterface) Naming.lookup("rmi://localhost:1099/Master");
    }

    private BufferedImage iterationsToImage(int[][] iterationValues) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixelColor = iterationValues[x][y] < this.maxIterations ? model.getRgbFromInt(iterationValues[x][y], 25) : Color.BLACK.getRGB();
                image.setRGB(x, y, pixelColor);
            }
        }

        return image;
    }

    public void run() {
        for (int i = 0; i < stepCount && !isInterrupted(); i++) {
            try {
                int[][] iterationValues = server.startCalculation(this.workerCount, this.width, this.height, this.zoom, (double)this.x, (double)this.y, this.maxIterations);
                BufferedImage image = this.iterationsToImage(iterationValues);
                updateRequest.setImage(image);
                EventQueue.invokeLater(updateRequest);
            } catch (RemoteException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        Thread.currentThread().interrupt();
    }
}