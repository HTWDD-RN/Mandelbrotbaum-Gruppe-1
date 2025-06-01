package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.mandelbrotbaum.sharedobjects.CalculationModelImpl;
import com.mandelbrotbaum.sharedobjects.MasterInterface;

public class Model {

    private int height;
    private int width;
    private String masterHostName;
    private BufferedImage _image;
    private MasterInterface _master;

    /**
     * jobStatus:
     * value -2 - RMI-Connection to Master failed
     * value -1 - job in progress or "no job"
     * value =0 - last job failed
     * value >0 - last job successfully done in nanoseconds
     */
    private long jobStatus = -1; 
    public boolean changedJobStatus = false;

    public int cntWorkers = 0;
    public boolean changedCntWorkers = true;

    public boolean playButtonVisible = false;
    public boolean changedPlayButtonVisiblity = false;

    public boolean changedImage = false;

    private boolean isCalculationRunning = false;

    private int nextAnimationFrameNr = 0;
    public boolean isAnimationRunning = false;
    private double _lastJobPointX = 0;
    private double _lastJobPointY = 0;
    private double _lastJobZoomFactor = 1;
    private int _lastJobWidth = 1024;
    private int _lastJobHeight = 768;
    private int _lastJobSturrenanzahl = 0;
    private long _lastJobRemainingFrames = 0;

    private String errorMsg01 = "";
    private String errorMsg02 = "";

    /**
     * This method is called asynchronous from UpdateRequest.run()
     * which is periodically invoked with help of Ticker.
     * The method queries Master about the status of the current Job.
     * If the Job is done, this method sets flags which are also handled
     * in UpdateRequest.run()
     * 
     * achtung: getMaster().getCalculationStatus() is different from jobStatus
     *   - getCalculationStatus below 0 means: job running, remaining frames nr.
     *   - jobStatus = -1 means "connection problem"
     *   - 0 means (for both): job failure;
     */
    public synchronized void checkCalculation(){
        if(isCalculationRunning){
            try{
                long stat = getMaster().getCalculationStatus();
                if(stat >= 0){
                    jobStatus = stat;
                    isCalculationRunning = false;
                    if(stat > 0){
                        playButtonVisible = true;
                    }
                    else{
                        playButtonVisible = false;
                    }
                    changedImage = true;
                    changedPlayButtonVisiblity = true;
                    changedJobStatus = true;
                }
                else{
                    if(stat != _lastJobRemainingFrames){
                        _lastJobRemainingFrames = stat;
                        changedJobStatus = true;
                    }
                }
            }
            catch(Exception e){
                if(errorMsg01.isEmpty()){
                    System.out.println("Ausnahme in Model.checkCalculation()");
                }
                isCalculationRunning = false;
                jobStatus = 0;
                changedJobStatus = true;
            }
        }
        else{
            if(isAnimationRunning){
                changedImage = true;
                if(nextAnimationFrameNr > (_lastJobSturrenanzahl-1)){
                    //this happenes if the last frame from previous animation
                    //is displayed on the screen and the user pressed Abspielen-button
                    //to play animation again
                    int dummy = 5;
                    //nextAnimationFrameNr = 0;
                }
            }
            int cnt = 0;
            try{
                cnt = getMaster().getWorkerCount();
            }
            catch(Exception e){
                if(errorMsg01.isEmpty()){
                    System.out.println("Ausnahme in Model.checkCalculation(): getWorkersCount()");
                }
            }
            if(cnt != cntWorkers){
                cntWorkers = cnt;
                changedCntWorkers = true;
            }
        }
    }

    public MasterInterface getMaster(){
        if(this._master == null){
            try {
                MasterInterface m = (MasterInterface) Naming.lookup("rmi://" + masterHostName + "/Master"); //Stub
                setMaster(m);
                errorMsg01 = "";
            }
            catch(Exception e){
                String errText = "Ausnahme in Model.getMaster(): ";
                if(!errText.equals(errorMsg01)){
                    errorMsg01 = errText;
                    System.out.println(errText + e);
                }
                setMaster(null);
                jobStatus = -2;
                changedJobStatus = true;
                changedImage = true;
            }
        }
        return _master;
    }
    
    public Model(int width, int height, String masterHost) {
        this.width = width;
        this.height = height;
        this.masterHostName = masterHost;
        _image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setFrameSize(int width, int height){
            this.width = width;
            this.height = height;
            _image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            //resetting the frames on the server
            submitJob(1, 1, 0, 0, 1, 1, 1, false);
    }

    public synchronized BufferedImage getImage() {
        return _image;
    }

    public String jobStatusText(){
        String a = "";
        
        if(jobStatus == -2){
            a = "RMI-Connection to Master failed.";
        }
        else if(jobStatus == 0){
            a = "Job failed.";
        }
        else if(jobStatus > 0){
            a = String.format("Job done. Execution time: %.5f s.", (jobStatus / 1000000000.0));
        }
        else if(jobStatus == -1 && isCalculationRunning){
            a = "Job is running. remaining: " + _lastJobRemainingFrames;
        }
        else{
            int dummy = 5;
        }
          LocalDateTime date = LocalDateTime.now();
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
          String timeStr = date.format(formatter);
          
        return timeStr + " " + a;
    }

    public void setMaster(MasterInterface m){
        if(m == null){
            jobStatus = -2;
            changedJobStatus = true;
            changedImage = true;
            _master = null;
        }
        else{
            _master = m;
            jobStatus = -1;
            changedJobStatus = true;
        }
    }

    public synchronized void drawMandelbrot () {
        if((jobStatus == 0 || jobStatus == -2) || (jobStatus == -1 && !isCalculationRunning)){
            return;
        }

        /* DEBUG
        int color = Color.GREEN.getRGB();
        long i = System.currentTimeMillis() % 3;
        if(i == 0) color = Color.BLUE.getRGB();
        else if(i == 1) color = Color.RED.getRGB();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                getImage().setRGB(x, y, color);
            }
        }
        */

        int[][] a = null;
        
        try{
            a = getMaster().peekFrame(nextAnimationFrameNr);
        }
        catch(Exception e){
            if(!(nextAnimationFrameNr == _lastJobSturrenanzahl)){
                System.out.println("Ausnahme in Model.drawMandelbrot: " + e.getMessage());
            }
        }
        if (a != null){
            for(int x = 0; x < width; x++){
                for(int y = 0; y < height; y++){
                    getImage().setRGB(x, y, a[x][y]);
                }
            }
            if(isAnimationRunning){
                nextAnimationFrameNr += 1;
            }
        }
        else{
            //I don't reset the nextAnimationFrame because 
            //the user wants to click on the last image to get positions
            //nextAnimationFrameNr = 0; 
            isAnimationRunning = false;
        }
    }

    public double getLastAnimationFrameXpxToXreal(int pX){
        //Each frame in Job is one multiplication with zoomFaktor
        //first frame is not zoomed (zoom=1)
        //therefore Frame n has been zommed to zoomFaktor^n
        int frameNr = nextAnimationFrameNr;
        double frameZoom = Math.pow(_lastJobZoomFactor, frameNr);

        if(nextAnimationFrameNr > 0) frameNr -= 1; //On the user's screen is the previous frame displayed.
        double realWidth = _lastJobWidth * frameZoom;
        double realOffset = pX * frameZoom;

        return _lastJobPointX - (realWidth/2) + realOffset;
    }

    public double getLastAnimationFrameYpxToYreal(int pY){
        //Each frame in Job is one multiplication with zoomFaktor
        //first frame is not zoomed (zoom=1)
        //therefore Frame n has been zommed to zoomFaktor^n
        int frameNr = nextAnimationFrameNr;
        double frameZoom = Math.pow(_lastJobZoomFactor, frameNr);

        if(nextAnimationFrameNr > 0) frameNr -= 1; //On the screen is the previous frame displayed.
        double realHeight = _lastJobHeight * frameZoom;
        double realOffset = pY * frameZoom;

        return _lastJobPointY - (realHeight/2) + realOffset;
    }


    public void submitJob(int stuffenanzahl,
                          int iterationsanzahl,
                          double zoompunktX,
                          double zoompunktY,
                          double zoomFaktor,
                          int anzWorker,
                          int anzThreadsProWorker,
                          boolean divideSingleFrame)
    {
            jobStatus = -1;
            isCalculationRunning = true;

            Thread thread = new Thread(){
                public void run(){
                    try{
                        if(getMaster() != null){
                            getMaster().executeJob(stuffenanzahl, iterationsanzahl, width, height, zoompunktX, zoompunktY, zoomFaktor, anzWorker, anzThreadsProWorker, divideSingleFrame);
                            _lastJobPointX = zoompunktX;
                            _lastJobPointY = zoompunktY;
                            _lastJobZoomFactor = zoomFaktor;
                            _lastJobWidth = width;
                            _lastJobHeight = height;
                            _lastJobSturrenanzahl = stuffenanzahl;
                        }
                        else{
                            jobStatus = -2;
                            isCalculationRunning = false;
                            changedJobStatus = true;
                            
                        }
                    }
                    catch(Exception e){
                        setMaster(null);
                        jobStatus = 0; //this is already done in setMaster(null)
                        System.out.println("Ausnahme in Model.submitJob(): " + e.getMessage());
                        isCalculationRunning = false;
                    }
                    changedImage = true;
                }};
            thread.start();
            changedJobStatus = true;
            playButtonVisible = false;
            changedPlayButtonVisiblity = true;
            changedImage = true;
            nextAnimationFrameNr = 0;
    }

    public void playButtonPressed(){
        isAnimationRunning = !isAnimationRunning;
        if(isAnimationRunning){
            changedImage = true;
            if(nextAnimationFrameNr > _lastJobSturrenanzahl - 1){
                //the animation was stoppt last time on the last frame
                //now it will stop immediately. therefore resetting nextAnimationFrameNr
                nextAnimationFrameNr = 0;
            }
        }
    }
}
