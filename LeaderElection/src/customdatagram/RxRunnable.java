/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customdatagram;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vitor
 */
public class RxRunnable implements Runnable {

    public MulticastSocket msocket;
    
    public void run() 
    {
        byte[] buf = new byte[1000];
        DatagramPacket recv = new DatagramPacket(buf,buf.length);
        System.out.println("Hello from a thread!");
        while(true)
        {
            try {
                msocket.receive(recv);
            } catch (IOException ex) {
                System.out.println("Failed to receive!");
            }
            System.out.println("received " + new String(recv.getData(),0,recv.getLength()) +" ;" );
        }
        
    }

}
