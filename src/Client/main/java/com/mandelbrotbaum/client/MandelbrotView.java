package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class MandelbrotView extends JPanel {
    
    private final Model model;
    private MandelbrotPresenter presenter = null;
    private MandelbrotMouseListener mouseListener;
    private MandelbrotMouseWheelListener mouseWheelListener;
    private JFrame frame;
    private JSpinner zoomSpinner = null;
    private final String[] s1 = {"1024 x 768", "1920 x 1080"};

    public MandelbrotView(Model model) {
        this.model = model;
        this.zoomSpinner = new JSpinner(new SpinnerNumberModel(0.8,0,10,0.2));
    }

    /**
     * sets all needed listeners
     * @param presenter
     * @param mouseListener
     * @param mouseWheelListener
     */
    public void setAllListeners(MandelbrotPresenter presenter, MandelbrotMouseListener mouseListener, MandelbrotMouseWheelListener mouseWheelListener) {
        this.presenter = presenter;
        this.mouseListener = mouseListener;
        this.mouseWheelListener = mouseWheelListener;
    }

    /**
     * increments the zoom factor value by t times, if t is negative it will decrement the zoom factor t times
     * @param t
     */
    public void incrZoomFactor(int t) {
        double current = this.getZoomFactor();
        if (current+(0.2*t) <= 0)   zoomSpinner.setValue((double)0.0);
        else                        zoomSpinner.setValue((double)current+(0.2*t));
    }

    /**
     * gets the current zoom factor value
     * @return zoom factor
     * @throws NullPointerException
     */
    public double getZoomFactor() throws NullPointerException {
        if (zoomSpinner == null) {throw new NullPointerException("zoomSpinner has not been initialized yet");}
        return (double)zoomSpinner.getValue();
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
        panel.add(zoomSpinner);

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

        JButton exec = new JButton("Ausführen");
        panel.add(exec);

        // Action Listeners
        exec.addActionListener(this.presenter);
        this.addMouseListener(this.mouseListener);
        this.addMouseWheelListener(this.mouseWheelListener);

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