package com.mandelbrotbaum.server;

import com.mandelbrotbaum.sharedobjects.*;
import com.mandelbrotbaum.worker.*;

import java.awt.Color;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MasterImpl extends UnicastRemoteObject implements MasterInterface {

    private final List<Worker> workers;
    
    //Key - hashcode of the worker, value: count of ping failures;
    private HashMap<Integer, Integer> workersErrorCount = new HashMap<>();

    private final Ticker ticker;

    private boolean isCalculationRunning = false;
    private boolean isCalculationCanceled = false;
    private long jobStatus = -1;
    public synchronized void incrementJobStatus(int inc){
        jobStatus += inc;
        //0 - means error. the logic of the program prevents incrementing to 0.
        //but sometimes somehow it happenes. -1 means: "one frame remaining".
        //for now I let it here:
        if(jobStatus == 0) jobStatus = -1; 
    }
    private int[][][] frames;
    private int MAX_ITERATIONS;
    private int paletteSize = 25;

    public MasterImpl() throws RemoteException {
        super();
        workers = new CopyOnWriteArrayList<>();
        this.ticker = new Ticker(this);
        this.ticker.setDaemon(true);
        this.ticker.start();
    }

    @Override
    public int registerWorker(Worker worker) throws RemoteException {
        boolean found = false;
        for(int i = 0; i<workers.size(); i++){
            if(workers.get(i).hashCode() == worker.hashCode()){
                found = true;
                break;
            }
        }
        if(!found){
            workers.add(worker);
            workersErrorCount.put(worker.hashCode(), 0);
            System.out.println("New Worker registered: " + worker.hashCode() + ". Worker count: " + workers.size());
        }
        else{
            System.out.println("Worker trying to register, but it is already registered: " + worker.hashCode() + ". Worker count: " + workers.size());
        }
        return worker.hashCode();
    }

    @Override
    public int getWorkerCount() throws RemoteException {
        int a = workers.size();
        return a;
    }

    public synchronized void checkWorkers(){
        if(!isCalculationRunning){
            int a = workers.size();
            for(int i = a - 1; i>=0; i--){
                boolean ok = false;
                try{
                    ok = workers.get(i).ping();
                }
                catch(Exception e){
                    //System.out.println(e.getMessage());
                }
                if(!ok) {
                    int hash = workers.get(i).hashCode();
                    int eCnt = workersErrorCount.get(hash);
                    workersErrorCount.put(hash, eCnt + 1); //incrementing errors
                    if (eCnt > 10){
                        workers.remove(i);
                        workersErrorCount.remove(hash);
                        System.out.println(getTimeStr() + " one worker removed (" + hash + "). Remaining: " + workers.size());
                    }
                }
                else{
                    workersErrorCount.put(workers.get(i).hashCode(), 0); //resetting errors
                }
            }
        }
    }

    public int[][] startCalculation(int workerCount, int width, int height, double zoom, double xTopLeftCorner, double yTopLeftCorner, int threadsCnt, int maxIterations) throws RemoteException {
        int actualWorkerCount = Math.min(workerCount, workers.size());
        int sliceHeight = height / actualWorkerCount;

        int[][] finalImage = new int[width][height];

        ExecutorService executor = Executors.newFixedThreadPool(actualWorkerCount);
        List<Future<int[][]>> futures = new ArrayList<>();

        for (int i = 0; i < actualWorkerCount; i++) {
            int startY = i * sliceHeight;
            int endY = (i == actualWorkerCount - 1) ? height : (i + 1) * sliceHeight;
            int finalSliceHeight = endY-startY;

            Worker worker = workers.get(i);

            double widthR = width*zoom;
            double heightR = finalSliceHeight*zoom;
            double stepY = heightR / sliceHeight;
            double finalStartY = startY*stepY;

            String workerName = "Worker_" + i + "[" + worker.hashCode() + "]";
            Future<int[][]> future = executor.submit(() -> {
                int[][] result = worker.compute(width, finalSliceHeight, widthR, heightR, xTopLeftCorner, yTopLeftCorner + finalStartY, maxIterations, threadsCnt, workerName );
                return result;
            });

            futures.add(future);
        }

        for (int i = 0; i < futures.size(); i++) {
            try {
                int startY = i * sliceHeight;
                int[][] workerSlice = futures.get(i).get();

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < workerSlice[0].length; y++) {
                        finalImage[x][startY + y] = workerSlice[x][y];
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return finalImage;
    }

    public int[][][] startCalculationOneFramePerWorker(int width, int height, double[] zooms, double pointX, double pointY, int threadsCnt, int maxIterations){
        int[][][] finalImages = new int[zooms.length][width][height];
        if (zooms.length == 0){
            return finalImages;
        }

        ExecutorService executor = Executors.newFixedThreadPool(zooms.length);
        List<Future<int[][]>> futures = new ArrayList<>();

        for (int i = 0; i < zooms.length; i++) {
            Worker worker = workers.get(i);

            double widthR = width*zooms[i];
            double heightR = height*zooms[i];
            double xTopLeftCorner = pointX - widthR/2;
            double yTopLeftCorner = pointY - heightR/2;

            String workerName = "Worker_" + i + "[" + worker.hashCode() + "]";
            Future<int[][]> future = executor.submit(() -> {
                int[][] result = worker.compute(width, height, widthR, heightR, xTopLeftCorner, yTopLeftCorner, maxIterations, threadsCnt, workerName ); 
                incrementJobStatus(1);
                return result;
            });

            futures.add(future);
        }

        for (int i = 0; i < futures.size(); i++) {
            try {
                finalImages[i] = futures.get(i).get();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return finalImages;
    }

    @Override
    public long getCalculationStatus() {
        return jobStatus;
        //jobStatus >0 : done (number nanoseconds)
        //jobStatus = 0 : job failed
        //jobStatus = -0 : running, number of remaining frames

    }

    @Override
    public void executeJob(int stuffenanzahl, int iterationsanzahl, int widthPx, int heightPx, double zoompunktX, double zoompunktY,
            double zoomFaktor, int anzWorker, int anzThreadsProWorker, boolean divideSingleFrame)
            throws RemoteException {
        if(isCalculationRunning){
            //user pressed the button during calculation.
            //we suppose he/she wants to stop the current calculation.
            isCalculationCanceled = true;
            return;
        }
        if(workers.size() == 0 || anzWorker == 0) {
            jobStatus = 0;
            return;
        }

        MAX_ITERATIONS = iterationsanzahl;

        System.out.println("Master: start executeJob()");

        isCalculationRunning = true;
        jobStatus = stuffenanzahl*(-1);
        long tStart = System.nanoTime();


        frames = new int[stuffenanzahl][widthPx][heightPx];
        if(divideSingleFrame){
            double zoom = 1;
            if(zoompunktX == 100 && zoompunktY == 100){
                //for debugging purposes I need the screen to be always black
                //it means each pixel becomes the full iterations count.
                zoompunktX = 0;
                zoompunktY = 0;
                zoom = 0.00001;
            }
            for(int i = 0; i<frames.length; i++){
                double widthR = widthPx*zoom;
                double heightR = heightPx*zoom;
                if(!isCalculationCanceled){
                    frames[i] = startCalculation(anzWorker, widthPx, heightPx, zoom, zoompunktX - widthR/2, zoompunktY - heightR/2, anzThreadsProWorker, iterationsanzahl);
                }
                else{
                    frames[i] = new int[widthPx][heightPx];
                }
                incrementJobStatus(1);
                zoom *= zoomFaktor;
            }
        }
        else{
            //TODO: divide number of all frames by number of workers
            //and give multiple frames to each worker.
            //but for now: single frame to single worker.

            int actualWorkerCount = Math.min(anzWorker, workers.size());
            int i = 0;
            double z = 1;
            for(i = 0; i<frames.length; i+=actualWorkerCount){
                    if(i+actualWorkerCount > (frames.length-1)){
                        //case, when some frames are remaining on the end
                        //if frame.length=40, i=39 -> actualWorkerCount = 1
                        //   frame.length=40, i=38 -> actualWorkerCount = 2
                        actualWorkerCount = frames.length - i;
                    }
                if(!isCalculationCanceled){
                    double[] zooms = new double[actualWorkerCount];
                    for(int j = 0; j<actualWorkerCount; j++){
                        zooms[j] = z;
                        z = z*zoomFaktor;
                    }
                    int[][][] result = startCalculationOneFramePerWorker(widthPx, heightPx, zooms, zoompunktX, zoompunktY, anzThreadsProWorker, iterationsanzahl);
                    for(int j = 0; j<actualWorkerCount; j++){
                        frames[i+j] = result[j];
                    }
                }
                else{
                    for(int j = 0; j<actualWorkerCount; j++){
                        frames[i+j] = new int[widthPx][heightPx];
                    }
                }
            }
        }
    
        isCalculationCanceled = false;
        long tEnd = System.nanoTime();
        jobStatus = tEnd - tStart;
        isCalculationRunning = false;

        System.out.println("Master: end executeJob()");
    }

    @Override
    public int[][] peekFrame(int index) throws RemoteException {
        int[][] a = null;
        if(frames.length < (index + 1)){
            //...
        }
        else{
            int width = frames[0].length;
            int height = frames[0][0].length;
            a = new int[width][height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int iterations = frames[index][x][y];

                    // dbgStats.set(iterations, dbgStats.get(iterations)+1);

                    if (iterations == MAX_ITERATIONS) {
                         a[x][y] = Color.BLACK.getRGB();
                    } else {
                        a[x][y] = getRgbFromInt(iterations, paletteSize);
                    }
                }
            }
        }
        return a;
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
            if(g > 1) g = 1;
            r = 0;
            b = 0;
        }
        else if(colValue <= levelsCnt*2){ // green part
            g = 1;
            r = ((float)colValue - levelsCnt) / levelsCnt;
            if(r > 1) r = 1;
            b = 0;
        }
        else{ // blue part
            g = 1;
            r = 1;
            b = ((float)colValue - levelsCnt*2) / levelsCnt;
            if(b > 1) b = 1;
        }

        Color c = new Color(1,1,1);
        try{
            c = new Color(r, g, b);
        }
        catch(Exception e){
            int dummy = 5;
        }
        int result = c.getRGB();
        return result;
    }

    public String getTimeStr(){
          LocalDateTime date = LocalDateTime.now();
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
          String timeStr = date.format(formatter);
          return timeStr;
    }

}