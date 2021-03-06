/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderprotocol2;

import customdatagram.CustomSocket;
import static java.lang.Integer.max;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vitor
 */
public class process 
{
    private static int t_unit;
    private static int eta;
    private final int pid; // process id (java process id? )
    private boolean next_period;
    private CopyOnWriteArrayList<Integer> members; // contains all known pids
    private CopyOnWriteArrayList<Integer> contenders; // contains all pids of leader candidates
    private ConcurrentMap<Integer,Timer> timer; // timer to check if link is timely
    private ConcurrentMap<Integer,Integer> timeout; // timeout value increased when timer expires
    private int silent; // contains all processes that timer expired since last reset
    private CopyOnWriteArrayList<Integer> to_reset; // list of processes that will need to reset timer
    private ConcurrentMap<Integer,Integer> susp_level; // level of suspicion that process j crashed 
    private ConcurrentMap<Integer,ArrayList<Integer>> suspected_by; // list of processes that suspect process j
    private int hbc; // number of periods that i was considered leader
    private ConcurrentMap<Integer,Integer> last_stop_leader; // greatest hbc value received from k
    private CustomSocket csocket;
    private Thread RxThread, TxThread, PtThread;
    public boolean rxflag, txflag, ptflag;
    private message lastrcv, lastsent;

    
    
    
    public process(int pid, int delay, double lossprob, int eta, int t_unit)
    {
            this.pid = pid;
            members = new CopyOnWriteArrayList();
            timer = new ConcurrentHashMap();
            timeout = new ConcurrentHashMap();
            silent=0;
            to_reset = new CopyOnWriteArrayList();
            suspected_by = new ConcurrentHashMap();
            last_stop_leader = new ConcurrentHashMap();
            susp_level = new ConcurrentHashMap();
            contenders = new CopyOnWriteArrayList();
            csocket = new CustomSocket(pid,delay,lossprob);
            rxflag = true;
            txflag = true;
            ptflag = true;
            
            this.eta = eta*t_unit;
            this.t_unit = t_unit;
            
            //init
            members.add(pid);
            contenders.add(pid);
            hbc = 0;
            last_stop_leader.put(pid,0);
            susp_level.put(pid,0);
            
    }
    
    public void task1()
    {
            //step 02
            if(!(this.next_period))
            {
                next_period=true;
                this.hbc+=1;
            }
            message msg = new message(message.HEARTBEAT,this.pid,this.susp_level.get(this.pid),0,this.hbc);
                
            //step 03
            csocket.broadcast(msg);
            lastsent = msg;
    }
    
    public int leader()
    {
        int min = 999999;
        int leader = 0;
        
        //System.out.println("contenders = " + contenders);
        for(int j : contenders)
        {
            //System.out.println("susp_level " + j + " = " + susp_level.get(j));
            if(susp_level.get(j) < min )
            {   
                min = susp_level.get(j);
                leader = j;
            }
            else if(susp_level.get(j) == min && j < leader)
            {
                leader = j;
            }
        }
        //System.out.println("leader = " + leader);
        return leader;
    } 
    
    public int timerExpired(int j)
    {
        //step 05
        timeout.put(j,timeout.get(j)+1);
        message msg = new message(message.SUSPICION,this.pid,this.susp_level.get(this.pid),j,0);
        csocket.broadcast(msg);
        lastsent = msg;
        
        //step 06
        //System.out.println("timer expired");
        contenders.remove((Object)j);
        
        return 1;
    }
    
    private Timer create_timer(int j)
    {
        Timer t_aux = new Timer();
        
        TimerTask task = new TimerTask() 
        {
            @Override
            public void run() 
            {
               timerExpired(j);
            }
        };
        if(this.timeout.get(j) != null)
        {
            t_aux.schedule(task,this.timeout.get(j));
        }
        
        return t_aux;
    }
    
    public int processMessage(message msg)
    {
        lastrcv = msg;
        //step 07
        if(!this.members.contains(msg.k))
        {   
            //step 08/09/10
            this.members.add(msg.k);
            this.susp_level.put(msg.k,0);
            this.last_stop_leader.put(msg.k,0);
            this.timeout.put(msg.k,eta);       
        }
        
        //step 11
        this.susp_level.put(msg.k,max(this.susp_level.get(msg.k),msg.sl_k));
        
        //step 12
        if(msg.tag_k == msg.HEARTBEAT && this.last_stop_leader.get(msg.k) <  msg.hbc_k)
        {
            Timer t_aux = this.timer.get(msg.k);
            if(t_aux != null)
            {
                t_aux.cancel();
                t_aux.purge();
            }    
            this.timer.put(msg.k,create_timer(msg.k));
            if(!contenders.contains(msg.k))
            {
                this.contenders.add(msg.k);
            }
        }
        
        //step 14
        else if(msg.tag_k == msg.STOP_LEADER  && this.last_stop_leader.get(msg.k) <  msg.hbc_k)
        {
            this.last_stop_leader.put(msg.k,msg.hbc_k);
            Timer t_aux = this.timer.get(msg.k);
            if(t_aux != null)
            {
                t_aux.cancel();
                t_aux.purge();
            }    
            
            if(contenders.contains(msg.k))
            {  
                contenders.remove((Object)msg.k);
            }
        }
        
        //step 17
        else if(msg.tag_k == msg.SUSPICION && msg.silent_k == this.pid )
        {
            //System.out.println("increasing susp lee of " + msg.k);
            this.susp_level.put(this.pid,this.susp_level.get(this.pid)+1);
        }
        return 1;
    }
    
    
    
    public int start()
    {
         TxThread = new Thread(new Runnable() 
        {
            @Override
            public void run() 
            {
                //System.out.println("Tx started!");
                while(txflag)
                {
                    try 
                    {
                        next_period = false;

                        //step 01
                        while(leader() == pid)
                        {
                            task1();
                            Thread.sleep(eta);
                        }

                        //step 04
                        if(next_period)
                        {
                            message msg = new message(message.STOP_LEADER,pid,susp_level.get(pid),0,hbc);
                            //System.out.println("sending stop leader");
                            //step 04
                            csocket.broadcast(msg);
                            lastsent = msg;
                        }
                        Thread.sleep(10);
                    } 
                    catch (InterruptedException ex) 
                    {
                        Logger.getLogger(process.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        TxThread.start();
        RxThread = new Thread(new Runnable() 
        {
            @Override
            public void run() 
            {
                //System.out.println("Waiting for data to receive.. ");
                while(rxflag)
                {
                    message msg = csocket.receive();
                    if(msg != null)
                    {
                        processMessage(msg);
                        //System.out.println("recv " + msg.tag_k + " leader = " + leader() + " my.pid = " + pid + "");
                    }
                }
            }
        });
        RxThread.start();  
        PtThread = new Thread(new Runnable() 
        {
            @Override
            public void run() 
            {
                int time = 0;
                while(ptflag)
                try 
                {
                    System.out.println(pid + ": PID: "+ pid+ " Leader :" + leader());
                    System.out.println(pid + ": Contenders :" + contenders);
                    for(int j : members)
                    {
                        System.out.println(pid + ": pid: " +  j +  " susp_level " + susp_level.get(j));
                    }
                    String lastmsg;
                    if(leader() == pid && lastsent != null)
                    {
                        switch(lastsent.tag_k)
                        {
                            case 1:
                                lastmsg = "HEARTBEAT";
                                break;
                            case 2:
                                lastmsg = "STOP_LEADER";
                                break;
                            case 3:
                                lastmsg = "SUSPICION";
                                break;
                            default:
                                lastmsg = "none";
                                break;
                        }
                        System.out.println(pid + ": Last msg sent: " + lastmsg );
                    }
                    else if (lastrcv != null)
                    {
                        switch(lastrcv.tag_k)
                        {
                            case 1:
                                lastmsg = "HEARTBEAT";
                                break;
                            case 2:
                                lastmsg = "STOP_LEADER";
                                break;
                            case 3:
                                lastmsg = "SUSPICION";
                                break;
                            default:
                                lastmsg = "none";
                                break;
                        }
                         System.out.println(pid + ": Last msg received: " + lastmsg + " from pid " + lastrcv.k );                    
                    }
                   
                    System.out.println(pid + ": Time Spent: " + time + " seconds");
                    System.out.println("");
                    Thread.sleep(1000);
                    time++;
                } 
                catch(InterruptedException ex) 
                {
                    Logger.getLogger(process.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        PtThread.start();
    
    return 1;
    }
    
    public int stop()
        {
            RxThread.stop();
            TxThread.stop();
            PtThread.stop();
            //rxflag = false;
            //txflag = false;
            //ptflag = false;
            return 1;
        }
    
     public void outputStats()
        {
            csocket.Stats.saveStats();
        }
    
   
}
