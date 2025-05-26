package com.mandelbrotbaum.server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Server {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099); // Start RMI registry

            MasterImpl master = new MasterImpl();
            Naming.rebind("Master", master); // Bind the master object

            System.out.println("Master-Server gestartet");
        } catch (Exception e) {
            System.out.println("Ausnahme: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
