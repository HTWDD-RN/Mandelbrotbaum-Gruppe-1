package com.mandelbrotbaum.sharedobjects;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Worker extends Remote {
    int[][] compute(int wPx, int hPx, double wR, double hR, double x0, double y0, int maxIterations, String workerName) throws RemoteException;
    boolean ping() throws RemoteException;
}
