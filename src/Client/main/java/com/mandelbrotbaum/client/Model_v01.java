package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.mandelbrotbaum.sharedobjects.CalculationModelImpl;

public class Model_v01 {
    private BufferedImage image;

    public int MAX_ITERATIONS = 1000;
    public double zoomFaktor = 0.8;
    public boolean isStopped = true;
    private ArrayList<MandelbrotWorker> _workerList = new ArrayList<>();
    public void setWorkerCount(int cnt){
        if(cnt > _workerList.size()){
            for(int i= _workerList.size() + 1; i<= cnt; i++){
                _workerList.add(null);
            }
        }
        else if(cnt < _workerList.size()){
            for(int i=_workerList.size() - 1; i> cnt -1 ; i--){
                _workerList.set(i, null);
                _workerList.remove(i);
            }
        }
    }

    private int paletteSize = 25;
    private double maxZoom = 1.5;
    private double minZoom = 0.000000000000000000000000000000000000000000000000000001;
    private double _centerXfloat;
    public void setCenterXfloat(int centerXpx){
        if(centerXpx < width){
            _centerXfloat = lastFrame.convertXfromPxToFloat(centerXpx);
        }
    }
    public double getCenterXfloat(){
        return _centerXfloat;
    }

    private double _centerYfloat;
    public void setCenterYfloat(int centerYpx){
        if(centerYpx < height){
            _centerYfloat = lastFrame.convertYfromPxToFloat(centerYpx);
        }
    }
    public double getCenterYfloat(){
        return _centerYfloat;
    }
 
    private int height;
    private int width;
    private double startHeightFloat = 2;
    private double startWidthFloat = 3;
    public int dbgRepaintAnz = 0;
    public int dbgDrawAnz = 0;
    private int cacheSize = 10; //how many frames are cached
    private int stripSize = 50; //how many lines one worker have to process at once
    private Queue<MandelbrotFrame> frameCache = new LinkedList<MandelbrotFrame>();
    private MandelbrotFrame lastFrame;
    public double getZoom(){
        return lastFrame.zoom;
    }
    private WorkAssigner wa;

    public boolean changedImage = false;
    public boolean changedJobStatus = false;
    public boolean changedPlayButtonVisiblity = false;

    public boolean playButtonVisible = true;
    public String jobStatusText = "";

    public Model_v01(int width, int height) {
        this.width = width;
        this.height = height;

        //original Zoompunkt
        //this._centerXfloat = -0.34837308755059104;
        //this._centerYfloat = -0.6065038451823017;

        //Zoompunkt v02
        this._centerXfloat = 0.25042526285342437;
        this._centerYfloat = -1.4001621073583808E-5;

        this.setWorkerCount(16);

        //align mandelbrot-frame-size with pixel-frame-size
        double wFaktor = width / startWidthFloat;
        double hFaktor = height / startHeightFloat;
        if (wFaktor < hFaktor) {
            startHeightFloat = height / wFaktor;
        } else {
            startWidthFloat = width / hFaktor;
        }

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        //first frame on zoom=1;
        lastFrame = new MandelbrotFrame(width, height);
        lastFrame.setCenterXfloat(_centerXfloat);
        lastFrame.setCenterYfloat(_centerYfloat);
        lastFrame.zoom = 1;
        lastFrame.widthFloat = startWidthFloat;
        lastFrame.heightFloat = startHeightFloat;
        lastFrame.matrix = new int[width][height];

        frameCache.add(lastFrame);
        init_WorkAssigner();
        wa.newWork(lastFrame);

        
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
        //get the first frame from the beginning of the queue
        MandelbrotFrame f = frameCache.peek();

        if(!f.isReady) {
            if(f.inProgress){
                return;
            }
        }

        //no need to repaint 
        if(!(f.zoom == lastFrame.zoom && 
           f._centerXfloat == lastFrame._centerXfloat && 
           f._centerYfloat == lastFrame._centerYfloat && dbgRepaintAnz > 1)){;

            if(frameCache.size()>1){
                f = frameCache.poll();
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int iterations = f.matrix[x][y];

                    // dbgStats.set(iterations, dbgStats.get(iterations)+1);

                    if (iterations == MAX_ITERATIONS) {
                        int fillCol = Color.BLACK.getRGB();
                        image.setRGB(x, y, fillCol);
                    } else {
                        int fillCol = getRgbFromInt(iterations, paletteSize);
                        image.setRGB(x, y, fillCol);
                        if(fillCol > 1){
                            int dummy = 5;
                        }
                    }
                }
            }
            //System.out.println("Model.drawMangelbrot(): frame gezeichnet, zoom=" + f.zoom + "; dbgDrawAnz=" + dbgDrawAnz);
            dbgDrawAnz += 1;
        }


        //now put frames to fill the queue
        if(!isStopped || dbgRepaintAnz < 2){
            if(dbgRepaintAnz < 2){
                if(init_NextFrame()){
                    wa.newWork(lastFrame);
                }
            }
            else if(!isStopped){
                while (frameCache.size() < cacheSize) {
                    if(init_NextFrame()){
                        wa.newWork(lastFrame);
                    }   
                    else{
                        break;
                    }
                }
            }
        }
        
    }

    public void init_WorkAssigner(){
        //TODO: ...
        wa = new WorkAssigner();
    }

    public boolean init_NextFrame(){
        boolean out = false;
        MandelbrotFrame f = new MandelbrotFrame(width, height);
        f.zoom = lastFrame.zoom * zoomFaktor;
        if(f.zoom > minZoom && f.zoom < maxZoom && frameCache.size() < cacheSize){
            
            f.heightPx = height;
            f.widthPx = width;
            f.heightFloat = lastFrame.heightFloat * zoomFaktor;
            f.widthFloat = lastFrame.widthFloat * zoomFaktor;
            f.zoom = lastFrame.zoom * zoomFaktor;
            f.setCenterXfloat(_centerXfloat);
            f.setCenterYfloat(_centerYfloat);
            frameCache.add(f);
            lastFrame = f;
            out = true;
        }
        else{
            //last frame remains on the screen and no new frames required
        }
        return out;
    }

    /**
     * Simple placeholder implementation for coloring the Mandelbrot pixels.
     * 
     * @param colValue
     *                    some value within a range from 0 to paletteSize.
     *                    colValue = 0 means white,
     *                    colValue >= paletteSize-1 means black.
     * @param paletteSize
     *                    desired total number of colors
     * @return
     *         RGB value of the mapped color.
     *         number of levels for each of R, G, B components
     *         are calculated by dividing paletteSize by 3,
     */
    public int getRgbFromInt(int colValue, int paletteSize ){
        colValue = colValue % paletteSize;

        float r=1, g=1, b=1; // white color means all 1, black - all 0
        float levelsCnt = (float)paletteSize / 3;

        // inverting
        colValue = paletteSize - colValue; 
        

        if(colValue <= levelsCnt*1){ // red part
            g = (float)colValue / levelsCnt;
            r = 0;
            b = 0;
        }
        else if(colValue <= levelsCnt*2){ // green part
            g = 1;
            r = ((float)colValue - levelsCnt) / levelsCnt;
            b = 0;
        }
        else{ // blue part
            g = 1;
            r = 1;
            b = ((float)colValue - levelsCnt*2) / levelsCnt;
        }
    
        Color c = new Color(1,1,1);
        try{
            c = new Color(r, g, b);
        }
        catch(Exception e){
        }
        
        return c.getRGB();
    }

 
    public class MandelbrotFrame{
        private int widthPx;
        private int heightPx;
        public boolean isReady = false;
        public boolean inProgress = false;
        public double widthFloat;
        public double heightFloat;
        public double zoom;
        public int centerXpx;
        public int centerYpx;
        private double _centerXfloat;
        private double _centerYfloat;

        public int[][] matrix;

        public MandelbrotFrame(int width, int height){
            matrix = new int[width][height];
        }

        public double getCenterXfloat(){
            return _centerXfloat;
        }
        public void setCenterXfloat(double x){
            _centerXfloat = x;
        }

        public double getCenterYfloat(){
            return _centerYfloat;
        }
        public void setCenterYfloat(double y){
            _centerYfloat = y;
        }

        public double convertXfromPxToFloat(int x){
            if(x >= width){
                x = width - 1;
            }
            else if(x < 0){
                x = 0;
            }
            double step = widthFloat / width;
            double out = _centerXfloat - (widthFloat/2) + step*x;
            return out;
        }
        public double convertYfromPxToFloat(int y){
            if(y >= height){
                y = height - 1;
            }
            else if(y < 0){
                y = 0;
            }
            double step = heightFloat / height;
            double out = _centerYfloat - (heightFloat/2) + step*y;
            return out;
        } 
    }

    public class MandelbrotWorker extends Thread { //Worker threads do the Mandelbrot-calculations
        private MandelbrotFrame f;
        public MandelbrotWorker(MandelbrotFrame frame){
            f = frame;
        }

        public void run(){
            int stripPx = stripSize;

            double xEcke = f.convertXfromPxToFloat(0);
            double yEcke = f.convertYfromPxToFloat(0);
            double hStrip = f.heightFloat * stripSize / f.heightPx;


            int lastRow = 0;
            int cnt = 0;

            while (lastRow < height && stripPx != 0) {
                if ((lastRow + stripPx) > height) {
                    stripPx = height - lastRow - 1;
                }

                //instead of the next line, the RMI Procedure must be called.
                int[][] cR = new int[width][stripPx];

                try{
                CalculationModelImpl calcModel = new CalculationModelImpl();
                cR = calcModel.calculateRange(width, stripPx, f.widthFloat, hStrip, xEcke, yEcke + hStrip * cnt, MAX_ITERATIONS, "keine_name");
                }
                catch(RemoteException e){
                    //ToDo: Fehlermeldung "Verbindung zu RMI fehlgeschlagen" in dem GUI
                }

                for (int y = 0; y < stripPx; y++) {
                    for (int x = 0; x < width; x++) {
                        int iterations = cR[x][y];
                        f.matrix[x][cnt * stripSize + y] = iterations;
                    }
                }

                lastRow = lastRow + stripPx;
                cnt++;
            }

            f.isReady = true;
        }
        
    }

    //this class should be a thread, that waits for a free worker and assigns a work to it
    public class WorkAssigner{ 
        
        public void newWork(MandelbrotFrame f){
            //Instant tStart = Instant.now();
            boolean found = false;
            //do{
                for(int i = 0; i<_workerList.size(); i++){
                    MandelbrotWorker w = _workerList.get(i);
                    if(w == null || !w.isAlive()){
                        found = true;
                        MandelbrotWorker t = new MandelbrotWorker(f);
                        _workerList.set(i, t);
                        f.inProgress = true;
                        t.start();
                        break;
                    }
                }
            //}
            //while(!found || Duration.between(tStart, Instant.now()).getSeconds() < 5);

            if(!found){
                //we didn't get a worker, but the frame can't stay as undone
                f.isReady = true;
            }
        }


    }



}

