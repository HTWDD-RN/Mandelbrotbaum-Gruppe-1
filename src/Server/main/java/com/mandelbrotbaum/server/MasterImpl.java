package com.mandelbrotbaum.server;

import com.mandelbrotbaum.sharedobjects.*;
import com.mandelbrotbaum.worker.*;

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
        return workers.size();
    }

    @Override
    public int[][] startCalculation(int workerCount, int width, int height, double zoom, double xOffset, double yOffset, int maxIterations) throws RemoteException {
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

            Future<int[][]> future = executor.submit(() -> {
                return null;//worker.compute(); // TODO
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
}