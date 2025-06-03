package com.mandelbrotbaum.worker;

import com.mandelbrotbaum.sharedobjects.*;

import java.rmi.Naming;

public class WorkerMain {
    public static void main(String[] args) {
        String serverHost = "localhost:1099";
        if(args.length !=1){
            System.out.println(("Argument missing.\n"
                             + "Command: <Name or IP of Server-Host>[:portNr]\n"
                             + "assuming host name is: " + serverHost));
            
        }
        else{
            serverHost = args[0];
        }

        MasterInterface master = null;
        WorkerImpl worker = null;
        int idOnMaster = 0;
        try {
            master = (MasterInterface) Naming.lookup("rmi://" + serverHost + "/Master");
            worker = new WorkerImpl(master, serverHost);
            idOnMaster = master.registerWorker(worker);

            System.out.println("Worker registered with id: " + idOnMaster);
        } catch (Exception e) {
            e.printStackTrace();
            try{
                worker = new WorkerImpl(null, serverHost);
            }
            catch(Exception e2){}
        }

        System.out.println("Number of CPU Cores: " + Runtime.getRuntime().availableProcessors());
       
    }
}
