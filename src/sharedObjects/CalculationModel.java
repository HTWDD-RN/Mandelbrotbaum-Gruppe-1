package sharedObjects;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CalculationModel extends Remote {
    int[][] calculateRange(int x, int y, int width, int height, double zoom, int maxIterations) throws RemoteException;
}