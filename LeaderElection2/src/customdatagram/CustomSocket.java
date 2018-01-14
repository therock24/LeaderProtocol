/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customdatagram;
 
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import leaderprotocol2.message;

/**
 *
 * @author Vitor
 */

public class CustomSocket 
{
    private static int DELAY_MAX;   // delay in miliseconds
    private static double LOSS_PROB;   // probability message is lost
    private static int pid;        
    private static final int port = 6789;
    private static final int MAX_PROC = 30;
    private static final int buffersize = (MAX_PROC*(MAX_PROC-1) + MAX_PROC + 2)*8;
    private DatagramSocket dsocket;
    private MulticastSocket msocket;
    private InetAddress group;
    private DatagramPacket hello;
    public SocketStats Stats;

    public CustomSocket(int pid, int delay, double lossprob) 
    {
        try 
        {
            this.dsocket = new DatagramSocket();
            this.msocket = new MulticastSocket(port);
            this.group = InetAddress.getByName("224.0.0.1");
            this.Stats = new SocketStats(pid);
            this.pid = pid;
            msocket.joinGroup(group);
            System.out.println("delay = " + delay + " lossprob = " + lossprob);
            DELAY_MAX = delay;
            LOSS_PROB = lossprob;
            
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(CustomSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }   
    
            
        
        public int register(int pid) throws InterruptedException 
        {
           String msg = "hello i am " + pid ;
           RxRunnable rxthread = new RxRunnable();
           rxthread.msocket=this.msocket;
           new Thread(rxthread).start();
           hello = new DatagramPacket(msg.getBytes(),msg.length(),group,msocket.getLocalPort());
           try 
           {
               msocket.send(hello);
           } 
           catch (IOException ex) 
           {
               Logger.getLogger(CustomSocket.class.getName()).log(Level.SEVERE, null, ex);
           }
           

           return 0;
        }
        
        public int broadcast(message msg)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos;
            try 
            {
                oos = new ObjectOutputStream(baos);
                oos.writeObject(msg);
                byte[] data = baos.toByteArray();
                msocket.send(new DatagramPacket(data, data.length, group, port));
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(CustomSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
            
    
            return 1;
        }
        
        public message receive()
        {
            byte[] buffer = new byte[buffersize];
            double delay = DELAY_MAX*Math.random();
            message msg = null;
            
            // Randomly discard messages
            
                try
                {
                    msocket.receive(new DatagramPacket(buffer, buffersize, group, port));
                    //System.out.println("Datagram received!");
                    if(Math.random() >= LOSS_PROB)
                    {

                        // sleep for a random amount of time
                        TimeUnit.MILLISECONDS.sleep((int)(delay));


                        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                        ObjectInputStream ois = new ObjectInputStream(bais);
                        Object readObject = ois.readObject();
                        if (readObject instanceof message) 
                        {
                            msg = (message) readObject;
                            //discard its own messages
                            if(msg.k == this.pid)
                            {
                                msg = null;
                            }
                            //System.out.println("Message is: k = " + msg.k + " , snk = " + msg.snk);
                        } 
                        else 
                        {
                            //System.out.println("The received object is not of type message!");
                            msg = null;
                        }
                        Stats.update(false,delay);
                   }
                   else
                   {
                       Stats.update(true,0);
                   }
                    
                }
                catch (IOException | ClassNotFoundException | InterruptedException ex ) 
                {
                   Logger.getLogger(CustomSocket.class.getName()).log(Level.SEVERE, null, ex);
                }    
                return msg;
              
        }
          
}