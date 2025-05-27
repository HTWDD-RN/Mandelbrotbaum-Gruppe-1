package com.mandelbrotbaum.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Queue;

import com.mandelbrotbaum.sharedobjects.CalculationModelImpl;

public class Model {
    private final BufferedImage image;

    int MAX_ITERATIONS = 1000;
    int paletteSize = 25;
    double zoomFaktor = 0.8;
    double maxZoom = 1;
    double minZoom = 0.000000000000000000000000000000000000000000000000000001;
    double _centerXfloat;
    double _centerYfloat;
 
    private int height;
    private int width;
    private double startHeightFloat = 2;
    private double startWidthFloat = 3;
    public int dbgReadyAnz = 0;
    private int cacheSize = 10; //how many frames are cached
    private int stripSize = 50; //how many lines one worker have to process at once
    private Queue<MandelbrotFrame> frameCache = new LinkedList<MandelbrotFrame>();
    private MandelbrotFrame lastFrame;
    private WorkAssigner wa;

    public Model(int width, int height) {
        this.width = width;
        this.height = height;
        this._centerXfloat = -0.34837308755059104;
        this._centerYfloat = -0.6065038451823017;

        //align mandelbrot-frame-size with pixel-frame-size
        double wFaktor = width / startWidthFloat;
        double hFaktor = height / startHeightFloat;
        if (wFaktor < hFaktor) {
            startHeightFloat = height / wFaktor;
        } else {
            startWidthFloat = width / hFaktor;
        }

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

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

        //caching some frames before view starts tu use them
        for(int i=0; i<cacheSize; i++)
        {
            init_NextFrame();
            wa.newWork(lastFrame);
        }
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

        if(!f.isReady || dbgReadyAnz < cacheSize - 2) return;

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

        //now put one frame to the end of the queue
        init_NextFrame();
        wa.newWork(lastFrame);
        
    }

    public void init_WorkAssigner(){
        //TODO: ...
        wa = new WorkAssigner();
    }

    public void init_NextFrame(){
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
        }
        else{
            //last frame remains on the screen and no new frames required
        }
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
                cR = calcModel.calculateRange(width, stripPx, f.widthFloat, hStrip, xEcke, yEcke + hStrip * cnt, MAX_ITERATIONS);
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
            dbgReadyAnz += 1;
        }
        
    }

    public class WorkAssigner{ //this thread waits for a free worker and assigns a work to it
        
        public void newWork(MandelbrotFrame f){
            MandelbrotWorker t = new MandelbrotWorker(f);
            t.start();
        }


    }



}

