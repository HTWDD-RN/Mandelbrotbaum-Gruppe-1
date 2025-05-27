package com.mandelbrotbaum.worker;

import com.mandelbrotbaum.sharedobjects.*;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import com.mandelbrotbaum.sharedobjects.CalculationModel;
import com.mandelbrotbaum.sharedobjects.CalculationModelImpl;

public class WorkerImpl extends UnicastRemoteObject implements Worker {

    public WorkerImpl() throws RemoteException {
        super();
    }

    @Override
    public int[][] compute(int wPx, int hPx, double wR, double hR, double x0, double y0, int maxIterations) throws RemoteException {
        CalculationModel calculationModel = new CalculationModelImpl();
        return calculationModel.calculateRange(wPx,hPx,wR,hR,x0,y0,maxIterations);
    }
}
