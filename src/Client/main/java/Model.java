package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class Model {

    private final BufferedImage image;
    private int height;
    private int width;

    public Model(int width, int height) {
        this.width = width;
        this.height = height;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public synchronized BufferedImage getImage() {
        return image;
    }

    public synchronized void drawMandelbrot () {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0,0, width, height);
        g2d.dispose();
    }
}