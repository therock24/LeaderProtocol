/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderprotocol1;

import customdatagram.customSocket;
import static java.lang.Thread.sleep;
import java.lang.management.ManagementFactory;

/**
 *
 * @author Vitor
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
         int pid = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
         System.out.println("pid = " + pid);
         customSocket socket = new customSocket();
         sleep(5000);
         socket.register(pid);
    }
    
}
