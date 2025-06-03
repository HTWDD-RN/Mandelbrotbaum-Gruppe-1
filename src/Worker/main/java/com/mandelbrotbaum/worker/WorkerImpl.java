package com.mandelbrotbaum.worker;

import com.mandelbrotbaum.sharedobjects.*;

import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class WorkerImpl extends UnicastRemoteObject implements Worker {

    private MasterInterface _master;
    private String masterHostName = "";
    private String errorMsg01 = "";
    private boolean isCalculationRunning = false;
    private int idOnMaster = 0;

    private final Ticker ticker;
    private List<Future<int[][]>> futures;
    

    public WorkerImpl(MasterInterface master, String masterHost) throws RemoteException {
        super();
        _master = master;
        masterHostName = masterHost;
        futures = new ArrayList<>();
        this.ticker = new Ticker(this);
        this.ticker.setDaemon(true);
        this.ticker.start();
    }

    public void setMaster(MasterInterface m){
        _master = m;
    }
    public MasterInterface getMaster(){
        if(this._master == null){
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String timeStr = date.format(formatter);

            System.out.println(timeStr + " Looking for Master on: " + "rmi://" + masterHostName + "/Master");
            try {
                MasterInterface m = (MasterInterface) Naming.lookup("rmi://" + masterHostName + "/Master"); //Stub
                setMaster(m);
                idOnMaster = m.registerWorker(this);
                errorMsg01 = "";
                System.out.println(timeStr + " Master found. this registered with id: " + idOnMaster);
            }
            catch(Exception e){
                String errText = "Ausnahme in getMaster(): ";
                if(!errText.equals(errorMsg01)){
                    errorMsg01 = errText;
                    System.out.println(errText + e);
                }
                setMaster(null);
            }
        }
        return _master;
    }

    public synchronized void checkMaster(){
        if(!isCalculationRunning){
            if(getMaster() != null){
                try{
                    getMaster().getWorkerCount(); //pinging the master
                }
                catch(RemoteException e){
                    setMaster(null);
                }
            }
        }
    }


    @Override
    public int[][] compute(int wPx, int hPx, double wR, double hR, double x0, double y0, int maxIterations, int threadsCnt, String workerName) throws RemoteException {
        isCalculationRunning = true;

        int sliceHeightPx = hPx / threadsCnt;

        int[][] finalImage = new int[wPx][hPx];

        ExecutorService executor = Executors.newFixedThreadPool(threadsCnt);
        futures = new ArrayList<>();

        for (int i = 0; i < threadsCnt; i++) {
            int startY = i * sliceHeightPx;
            int endY = (i == threadsCnt - 1) ? hPx : (i + 1) * sliceHeightPx;
            int finalSliceHeightPx = endY-startY;

            double stepY = hR/hPx;
            double heightR = stepY*finalSliceHeightPx;
            double finalStartY = startY*stepY;

            String threadName = "thread_" + i;
            if(i == 0){
                threadName = workerName + " " + threadName;
            }
            else{
                threadName = "        " + threadName;
            }
            final String tn = new String(threadName);

            Future<int[][]> future = executor.submit(() -> {
                CalculationModel calculationModel = new CalculationModelImpl();
                int[][] result = calculationModel.calculateRange(wPx, finalSliceHeightPx, wR, heightR, x0, y0 + finalStartY,maxIterations, tn);
                return result;
            });

            futures.add(future);
        }

        for (int i = 0; i < futures.size(); i++) {
            try {
                int startY = i * sliceHeightPx;
                int[][] workerSlice = futures.get(i).get();

                for (int x = 0; x < wPx; x++) {
                    for (int y = 0; y < workerSlice[0].length; y++) {
                        finalImage[x][startY + y] = workerSlice[x][y];
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        isCalculationRunning = false;
        return finalImage;
    }

    @Override
    public boolean ping() {
       return true;
    }
}
