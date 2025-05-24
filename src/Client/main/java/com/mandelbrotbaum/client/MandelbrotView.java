package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class MandelbrotView extends JPanel {
    
    private final Model model;
    private JFrame frame;
    private final String[] s1 = {"1024 x 768", "1920 x 1080"};

    public MandelbrotView(Model model) {
        this.model = model;
    }

    public void initView() {
        frame = new JFrame("Mandelbrot Gruppe 1");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(4, 4, 2, 2));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10, 10, 10));
        
        panel.add(new JLabel("Auflösung: "));
        panel.add(new JComboBox<String>(s1));

        panel.add(new JLabel("Zoomfaktor: ")); 
        panel.add(new JSpinner(new SpinnerNumberModel(0.8,0,10,0.2)));

        panel.add(new JLabel("Stufenanzahl: "));
        panel.add(new JSpinner(new SpinnerNumberModel(100, 0, 100, 1)));

        panel.add(new JLabel("Anzahl Worker: "));
        panel.add(new JSpinner(new SpinnerNumberModel(4, 2, 16, 2)));

        panel.add(new JLabel("Iterationsanzahl: "));
        panel.add(new JSpinner(new SpinnerNumberModel(100, 1, 1000, 1)));

        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        panel.add(new JLabel("Zoompunkt: "));
        panel.add(new JTextField("x-position",16));
        panel.add(new JTextField("y-position",16));

        panel.add(new JButton("Ausführen"));

        frame.add(panel, BorderLayout.NORTH);
        frame.add(this, BorderLayout.CENTER);

        frame.setSize(model.getWidth() + 40 , model.getHeight() + 220);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        synchronized (model){
            g.drawImage(model.getImage(),10, 40, this);
        }
    }
}