package com.mandelbrotbaum.Worker;

import sharedobjects.*;

import java.rmi.Naming;

public class WorkerMain {
    public static void main(String[] args) {
        try {
            WorkerImpl worker = new WorkerImpl();

            MasterInterface master = (MasterInterface) Naming.lookup("rmi://localhost:1099/Master");
            master.registerWorker(worker);

            System.out.println("Worker registered.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
