package com.mandelbrotbaum.sharedobjects;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CalculationModel extends Remote {
    int[][] calculateRange(int wPx, int hPx, double wR, double hR, double x0, double y0, int maxIterations) throws RemoteException;
}