package com.mandelbrotbaum.worker;

import com.mandelbrotbaum.sharedobjects.*;

import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class WorkerImpl extends UnicastRemoteObject implements Worker {

    private MasterInterface _master;
    private String masterHostName = "";
    private String errorMsg01 = "";
    private boolean isCalculationRunning = false;

    public WorkerImpl(MasterInterface master, String masterHost) throws RemoteException {
        super();
        _master = master;
        masterHostName = masterHost;
        Thread t = new Thread(){
                public void run(){
                    
                    while(!isInterrupted()) {
                        try{
                                if(!isCalculationRunning){
                                    if(getMaster() != null){
                                        getMaster().getWorkerCount(); //pinging the master
                                    }
                                }
                                Thread.sleep(1000);
                        }
                        catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        catch(Exception e){
                            setMaster(null);
                        }
                    }
                }};
        t.start();
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
                m.registerWorker(this);
                errorMsg01 = "";
                System.out.println(timeStr + " Master found. this registered.");
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


    @Override
    public int[][] compute(int wPx, int hPx, double wR, double hR, double x0, double y0, int maxIterations, String workerName) throws RemoteException {
        isCalculationRunning = true;
        CalculationModel calculationModel = new CalculationModelImpl();
        isCalculationRunning = false;
        return calculationModel.calculateRange(wPx,hPx,wR,hR,x0,y0,maxIterations, workerName);
    }

    @Override
    public boolean ping() {
       return true;
    }
}
