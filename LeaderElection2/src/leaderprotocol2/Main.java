/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderprotocol2;
 
import customdatagram.CustomSocket;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vitor
 */
public class Main {
    
     public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
         process p1 = new process();
         p1.start();
         sleep(90000);  
         p1.stop();
         p1.outputStats();
         System.exit(0);
       
    }
}
