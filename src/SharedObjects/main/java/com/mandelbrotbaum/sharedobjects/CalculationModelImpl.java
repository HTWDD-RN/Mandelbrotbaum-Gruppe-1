package com.mandelbrotbaum.sharedobjects;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class CalculationModelImpl extends UnicastRemoteObject implements CalculationModel {

    public CalculationModelImpl() throws RemoteException {
        super();
    }

        public int[][] calculateRange(int wPx, int hPx, double wR, double hR, double x0, double y0, int maxIterations) {
            int[][] a = new int[wPx][hPx];

            double yStep = hR / hPx;
            double xStep = wR / wPx;

            int max = 0;

            for (int y = 0; y < hPx; y++) {
                for (int x = 0; x < wPx; x++) {
                    int c = calculatePoint(x0 + x * xStep, y0 + y * yStep, maxIterations);

                    //debug
                    if(y == (hPx - 1)){
                        c = 2;
                    }
                    //end debug

                    a[x][y] = c;
                    if (c > max) {
                        max = c;
                    }
                }
            }

            return a;
        }

        public int calculatePoint(double re, double im, int maxIterations) {
            double x = 0.0;
            double y = 0.0;
            int iterations = 0;
            do {
                double xnew = x * x - y * y + re;
                double ynew = 2 * x * y + im;
                x = xnew;
                y = ynew;
                iterations++;
                if (iterations == maxIterations)
                    return iterations;
            } while (x <= 2 && y <= 2);
            return iterations;
        }        

}