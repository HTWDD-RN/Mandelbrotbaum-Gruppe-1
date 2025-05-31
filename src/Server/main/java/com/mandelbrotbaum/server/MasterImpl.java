package com.mandelbrotbaum.server;

import com.mandelbrotbaum.sharedobjects.*;
import com.mandelbrotbaum.worker.*;

import java.awt.Color;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MasterImpl extends UnicastRemoteObject implements MasterInterface {

    private final List<Worker> workers;

    private boolean isCalculationRunning = false;
    private long jobStatus = 0;
    private int[][][] frames;
    private int MAX_ITERATIONS;
    private int paletteSize = 25;

    public MasterImpl() throws RemoteException {
        super();
        workers = new CopyOnWriteArrayList<>();
    }

    @Override
    public void registerWorker(Worker worker) throws RemoteException {
        workers.add(worker);
        System.out.println("New Worker registered. Worker count: " + workers.size());
    }

    @Override
    public int getWorkerCount() throws RemoteException {
        int a = workers.size();
        if(!isCalculationRunning){
            for(int i = a - 1; i>=0; i--){
                boolean ok = false;
                try{
                    ok = workers.get(i).ping();
                }
                catch(Exception e){}
                if(!ok) workers.remove(i);
            }
            a = workers.size();
        }
        return a;
    }

    public int[][] startCalculation(int workerCount, int width, int height, double zoom, double xTopLeftCorner, double yTopLeftCorner, int maxIterations) throws RemoteException {
        int actualWorkerCount = Math.min(workerCount, workers.size());
        int sliceHeight = height / actualWorkerCount;

        int[][] finalImage = new int[width][height];

        ExecutorService executor = Executors.newFixedThreadPool(actualWorkerCount);
        List<Future<int[][]>> futures = new ArrayList<>();

        for (int i = 0; i < actualWorkerCount; i++) {
            int startY = i * sliceHeight;
            int endY = (i == actualWorkerCount - 1) ? height : (i + 1) * sliceHeight;
            Worker worker = workers.get(i);

            int finalStartY = startY;
            int finalEndY = endY;

            double widthR = width*zoom;
            double heightR = sliceHeight*zoom;
            double stepY = heightR / sliceHeight;

            String workerName = "Worker_" + i;
            Future<int[][]> future = executor.submit(() -> {
                return worker.compute(width, sliceHeight, widthR, heightR, xTopLeftCorner, yTopLeftCorner + finalStartY*stepY, maxIterations, workerName );
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

    @Override
    public long getCalculationStatus() {
        return jobStatus;
    }

    @Override
    public void executeJob(int stuffenanzahl, int iterationsanzahl, int widthPx, int heightPx, double zoompunktX, double zoompunktY,
            double zoomFaktor, int anzWorker, int anzThreadsProWorker, boolean divideSingleFrame)
            throws RemoteException {
        if(isCalculationRunning){
            System.out.println("another job is running");
            return;
        }
        if(workers.size() == 0 || anzWorker == 0) {
            jobStatus = -1;
            return;
        }

        MAX_ITERATIONS = iterationsanzahl;

        System.out.println("Master: start executeJob()");

        isCalculationRunning = true;
        jobStatus = 0;
        long tStart = System.nanoTime();


        frames = new int[stuffenanzahl][widthPx][heightPx];
        if(divideSingleFrame){
            double zoom = 1;
            for(int i = 0; i<frames.length; i++){
                double widthR = widthPx*zoom;
                double heightR = heightPx*zoom;
                frames[i] = startCalculation(anzWorker, widthPx, heightPx, zoom, zoompunktX - widthR/2, zoompunktY - heightR/2, iterationsanzahl);
                zoom *= zoomFaktor;
            }
        }
        else{
            int actualWorkerCount = Math.min(anzWorker, workers.size());
            ExecutorService executor = Executors.newFixedThreadPool(actualWorkerCount);
            List<Future<int[][]>> futures;
            int remainingFramesCnt = frames.length;
            double frameZoom = 1;
            while (remainingFramesCnt > 0) {
                futures = new ArrayList<>();
                for (int i = 0; i < actualWorkerCount; i++) {
                    if(i >= remainingFramesCnt){
                        break;
                    }
                    Worker worker = workers.get(i);
                    String workerName = "Worker_" + i;
                    double widthR = widthPx*frameZoom;
                    double heightR = heightPx*frameZoom;
                    Future<int[][]> future = executor.submit(() -> {
                        return worker.compute(widthPx, heightPx, widthR, heightR,  zoompunktX - widthR/2, zoompunktY - heightR/2, iterationsanzahl, workerName );
                    });
                    futures.add(future);
                    remainingFramesCnt -= 1;
                    frameZoom = frameZoom * zoomFaktor;
                }

                for (int i = 0; i < futures.size(); i++) {
                    try {
                        int[][] workerFrame = futures.get(i).get();
                        frames[frames.length - 1 - remainingFramesCnt + i] = workerFrame;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            executor.shutdown();
        }
    

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

}