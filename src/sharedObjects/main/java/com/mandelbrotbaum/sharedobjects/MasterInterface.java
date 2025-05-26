package com.mandelbrotbaum.sharedobjects;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterInterface extends Remote {
    void registerWorker(Worker worker) throws RemoteException;
    int getWorkerCount() throws RemoteException;
    int[][] startCalculation(int workerCount, int width, int height, double zoom, double xOffset, double yOffset, int maxIterations) throws RemoteException;

}
