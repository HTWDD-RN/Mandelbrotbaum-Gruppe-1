package com.mandelbrotbaum.server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.mandelbrotbaum.sharedobjects.CalculationModel;
import com.mandelbrotbaum.sharedobjects.CalculationModelImpl;

public class Server {
    public static void main(String[] args) {
        try {

            LocateRegistry.createRegistry(1099);

            CalculationModel calculationModel = new CalculationModelImpl();
            Naming.rebind("CalculationModel", calculationModel);
            System.out.println("CalculationModel-Server gestartet");
        } catch (Exception e) {
            System.out.println("Ausnahme: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
