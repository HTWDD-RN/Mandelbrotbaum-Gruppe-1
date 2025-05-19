package sharedObjects;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class CalculationModelImpl extends UnicastRemoteObject implements CalculationModel {

    public CalculationModelImpl() throws RemoteException {
        super();
    }
    public int[][] calculateRange(int x, int y, int width, int height, double zoom, int maxIterations) {
        int[][] iterationDepth = new int[width][height];

        for (int px = 0; px < width; px++) {
            for (int py = 0; py < height; py++) {

                double x0 = (x + px) / zoom;
                double y0 = (y + py) / zoom;
                
                double cx = 0.0;
                double cy = 0.0;
                int iteration = 0;

                while (cx * cx + cy * cy <= 4.0 && iteration < maxIterations) {
                    double xNew = cx * cx - cy * cy + x0;
                    double yNew = 2 * cx * cy + y0;
                    cx = xNew;
                    cy = yNew;
                    iteration++;
                }

                iterationDepth[px][py] = iteration;
            }
        }
        return iterationDepth;
    }
}