package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class MandelbrotView extends JPanel {
    
    private final Model model;
    private MandelbrotPresenter presenter = null;
    private MandelbrotMouseListener mouseListener;
    private MandelbrotMouseWheelListener mouseWheelListener;
    private JFrame frame;
    private JSpinner zoomSpinner = null;
    private JSpinner numberOfStepsSpinner = null;
    private JSpinner workerSpinner = null;
    private JSpinner iterationSpinner = null;
    private JComboBox<String> resolutionComboBox = null;
    private JComboBox<Integer> threadsProWorkerComboBox = null;
    private JTextField xPosField = null;
    private JTextField yPosField = null;
    private final String[] resolutions = {"1024 x 768", "800 x 600", "1920 x 1080"};
    private JButton execBtn = null;
    private JButton playBtn = null;
    private JLabel jobStatusLbl = null;
    private JLabel cntWorkersLbl = null;

    /**
     * MandelbrotView constructor
     * @param model
     */
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
     * sets the x coordinate inside the Textfield
     * @param x
     */
    public void setXTextField(String x) {
        xPosField.setText(x);
    }

    /**
     * sets the y coordinate inside the Textfield
     * @param y
     */
    public void setYTextField(String y) {
        yPosField.setText(y);
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

    /**
     * Returns the JSpinner used to set the number of steps
     * @return JSpinner for the number of steps
     */
    public JSpinner getNumberOfStepsSpinner() {
        return this.numberOfStepsSpinner;
    }

    /**
     * returns the JSpinner used to set the number of worker
     * @return JSpinner for the number of worker
     */
    public JSpinner getWorkSpinner() {
        return this.workerSpinner;
    }

    /**
     * Returns the JSpinner used to set the zoom factor
     * @return JSpinner for the zoom factor
     */
    public JSpinner getZoomFactorSpinner() {
        return this.zoomSpinner;
    }

    /**
     * return the JSpinner used to set the number of iterations
     * @return JSpinner for the number if iterations
     */
    public JSpinner getIterationSpinner() {
        return this.iterationSpinner;
    }

    /**
     * returns the combo box used to select the resolution.
     * @return JComboBox for the resolution selection
     */
    public JComboBox<String> getResolutionComboBox() {
        return this.resolutionComboBox;
    }

    public JComboBox<Integer> getThreadsProWorkerCombobox(){
        return this.threadsProWorkerComboBox;
    }

    /**
     * return the text field used to set coordinate for x axis
     * @return JTextField for the x axis
     */
    public JTextField getXPosition() {
        return xPosField;
    }

    /**
     *  return the text field used to set coordinate for y axis
     *  @return JTextField for the y axis
     */
    public JTextField getYPosition() {
        return yPosField;
    }


    /**
     * hides or sets visible the play button.
     * @param visible
     */
    public void playButtonSetVisiblity(boolean visible){
        playBtn.setVisible(visible);
    }

    /**
     * sets text of the message for the last job status
     * @param text
     */
    public void jobStatusLblSetText(String text){
        jobStatusLbl.setText(text);
    }

    public void workersCountLblSetText(String text){
        cntWorkersLbl.setText(text);
    }

    /**
     * Initializes the main application window (JFrame) and sets up the graphical user interface for the Mandelbrot application.
     */
    public void initView() {
        frame = new JFrame("Mandelbrot Gruppe 1");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(5, 4, 2, 2));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10, 10, 10));
        
        panel.add(new JLabel("Auflösung: "));
        
        resolutionComboBox = new JComboBox<String>(resolutions);
        panel.add(resolutionComboBox);

        panel.add(new JLabel("Zoomfaktor: "));
        panel.add(zoomSpinner);

        panel.add(new JLabel("Stufenanzahl: "));
        numberOfStepsSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 200, 1));
        panel.add(numberOfStepsSpinner);
        

        cntWorkersLbl = new JLabel("Anzahl Worker: ");
        panel.add(cntWorkersLbl);
        workerSpinner = new JSpinner(new SpinnerNumberModel(4,1,16,2));
        panel.add(workerSpinner);

        panel.add(new JLabel("Iterationsanzahl: "));
        iterationSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, 10000, 1));
        panel.add(iterationSpinner);

        panel.add(new JLabel("Threads pro worker"));
        Integer[] cmbVals = {1, 2, 3, 4};
        threadsProWorkerComboBox = new JComboBox<Integer>(cmbVals);
        panel.add(threadsProWorkerComboBox);

        panel.add(new JLabel("Zoompunkt: (x, y) "));

        xPosField = new JTextField("-0.34837308755059104",20);
        panel.add(xPosField);
        yPosField = new JTextField("-0.6065038451823017",20);
        panel.add(yPosField);
        
        execBtn = new JButton("Ausführen");
        panel.add(execBtn);
        
        jobStatusLbl = new JLabel();
        jobStatusLbl.setForeground(Color.BLUE);
        panel.add(jobStatusLbl);

        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        playBtn = new JButton("Abspielen");
        playBtn.setVisible(false);
        panel.add(playBtn);
        
        // Change Listeners
        numberOfStepsSpinner.addChangeListener(this.presenter);
        workerSpinner.addChangeListener(this.presenter);
        iterationSpinner.addChangeListener(this.presenter);
        
        // Action Listeners
        MandelbrotTextClickListener resetTextFieldListener = new MandelbrotTextClickListener(this); // yes ik, i am running out of naming ideas
        xPosField.addMouseListener(resetTextFieldListener);
        yPosField.addMouseListener(resetTextFieldListener);
        resolutionComboBox.addActionListener(this.presenter);
        threadsProWorkerComboBox.addActionListener(this.presenter);
        execBtn.addActionListener(this.presenter);
        playBtn.addActionListener(this.presenter);
        this.addMouseListener(this.mouseListener);
        this.addMouseWheelListener(this.mouseWheelListener);
        zoomSpinner.addChangeListener(this.presenter);

        frame.add(panel, BorderLayout.NORTH);
        frame.add(this);
        
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