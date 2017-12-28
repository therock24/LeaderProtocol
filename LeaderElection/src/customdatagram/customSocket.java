/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customdatagram;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vitor
 */

public class customSocket 
{
    private DatagramSocket dsocket;
    private MulticastSocket msocket;
    private InetAddress group;
    private DatagramPacket hello;

    public customSocket() 
    {
        try {
            this.dsocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(customSocket.class.getName()).log(Level.SEVERE, null, ex);
        }  
        try {
            this.msocket = new MulticastSocket(6789);
        } catch (IOException ex) {
            Logger.getLogger(customSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.group = InetAddress.getByName("224.0.0.1");
        } catch (UnknownHostException ex) {
            Logger.getLogger(customSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            msocket.joinGroup(group);
        } catch (IOException ex) {
            Logger.getLogger(customSocket.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    
        public int register(int pid) throws InterruptedException 
        {
           String msg = "hello i am" + pid ;
           hello = new DatagramPacket(msg.getBytes(),msg.length(),group,msocket.getLocalPort());
           try {
               msocket.send(hello);
           } catch (IOException ex) {
               Logger.getLogger(customSocket.class.getName()).log(Level.SEVERE, null, ex);
           }
           byte[] buf = new byte[1000];

           DatagramPacket recv = new DatagramPacket(buf,buf.length);
           try {
               msocket.receive(recv);
               sleep(5);
               msocket.receive(recv);
           } catch (IOException ex) {
               Logger.getLogger(customSocket.class.getName()).log(Level.SEVERE, null, ex);
           }
               System.out.println("received " + new String(recv.getData(),0,recv.getLength()) +" ;" );

           return 0;
        }
          
}
