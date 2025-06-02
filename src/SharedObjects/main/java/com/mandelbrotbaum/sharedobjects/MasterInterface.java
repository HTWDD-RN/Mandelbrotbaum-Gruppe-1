package com.mandelbrotbaum.sharedobjects;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterInterface extends Remote {
    int registerWorker(Worker worker) throws RemoteException;
    int getWorkerCount() throws RemoteException;
    //int[][] startCalculation(int workerCount, int width, int height, double zoom, double xOffset, double yOffset, int maxIterations) throws RemoteException;
    void executeJob(int stuffenanzahl,
                          int iterationsanzahl,
                          int widthPx,
                          int heightPx,
                          double zoompunktX,
                          double zoompunktY,
                          double zoomFaktor,
                          int anzWorker,
                          int anzThreadsProWorker,
                          boolean divideSingleFrame ) throws RemoteException;
    long getCalculationStatus() throws RemoteException; // -1 means: Error, 0 means: not ready(or not started), >0 means: done with nanoseconds execution time.
    int[][] peekFrame(int index) throws RemoteException;
}
