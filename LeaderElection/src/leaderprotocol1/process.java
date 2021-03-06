/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderprotocol1;

import customdatagram.CustomSocket;
import static java.lang.Integer.max;
import java.lang.management.ManagementFactory;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 *
 * @author Vitor
 */
public class process 
{  
    private static int alpha; // min number of processes that do not crash
    private static int t_unit; // time unit in miliseconds
    private static int eta;
    public final int pid; // process id (java process id? )
    private CopyOnWriteArrayList<Integer> members; // contains all known pids
    private ConcurrentMap<Integer,Timer> timer; // timer to check if link is timely
    private ConcurrentMap<Integer,Integer> timeout; // timeout value increased when timer expires
    private CopyOnWriteArrayList<Integer> silent; // contains all processes that timer expired since last reset
    private CopyOnWriteArrayList<Integer> to_reset; // list of processes that will need to reset timer
    private ConcurrentMap<Integer,Integer> susp_level; // level of suspicion that process j crashed 
    private ConcurrentMap<Integer,CopyOnWriteArrayList<Integer>> suspected_by; // list of processes that suspect process j
    private int sn; // local seq number of message
    private ConcurrentMap<Integer,message> state; // last message received by process k
    private CustomSocket csocket;
    private Thread RxThread, TxThread, PtThread;
    public boolean rxflag, txflag, ptflag, t_alloc;
 
    
    
    public process(int pid,int delay, double lossprob, int eta, int t_unit, int alpha)
    {
            this.pid = pid;
            members = new CopyOnWriteArrayList();
            timer = new ConcurrentHashMap();
            timeout = new ConcurrentHashMap();
            silent = new CopyOnWriteArrayList();
            to_reset = new CopyOnWriteArrayList();
            susp_level = new ConcurrentHashMap();
            suspected_by = new ConcurrentHashMap();
            state = new ConcurrentHashMap();
            csocket = new CustomSocket(pid, delay, lossprob);
            message aux = new message();
            
            this.eta = eta*t_unit;
            this.t_unit = t_unit;
            this.alpha = alpha;
            
            
            // init
            sn=0;
            aux.k = this.pid;
            aux.snk = this.sn;
            aux.silent = this.silent;
            aux.susp_level = this.susp_level;
            state.put(this.pid,aux);
            members.add(this.pid);
            susp_level.put(this.pid,0); 
            
            
            rxflag = true;
            txflag = true;
            ptflag = true;
            //t_alloc = false;
    }
    
    public void task1()
    {
        //step 01
        sn++;
   
        //step 02
        for (int j : silent)
        {
            CopyOnWriteArrayList<Integer> list = suspected_by.get(j);
            if(list == null)
            {
                list = new CopyOnWriteArrayList();
            }
            list.add(this.pid);
            suspected_by.put(j,list);
        }
        
        //step 03
        CopyOnWriteArrayList alaux;
        for(int j : members)
        {
            alaux = suspected_by.get(j);
            if(alaux != null)
            {
                if (alaux.size() >= this.alpha)
                {
                    //step 04
                    susp_level.put(j,susp_level.get(j)+1);
                }
                suspected_by.put(j,new CopyOnWriteArrayList());
            }
            
        }
        
        //step 05
        message aux = state.get(this.pid);
        aux.k = this.pid;
        aux.snk = this.sn;
        aux.susp_level.putAll(this.susp_level);
        aux.silent = (CopyOnWriteArrayList<Integer>)this.silent.clone();
        state.put(this.pid,aux);
        
        //step 06
        this.csocket.broadcast(aux);
        
        //step 07
        for(int j : this.to_reset)
        {
            Timer t_aux = this.timer.get(j);
            if(t_aux != null)
            {
                t_aux.cancel();
                t_aux.purge();
            }
            /*while(t_alloc)
            {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(process.class.getName()).log(Level.SEVERE, null, ex);
                }
            }*/
            if(this.timeout.get(j) != null)
                this.timer.put(j,create_timer(j));
        }
        this.to_reset = new CopyOnWriteArrayList();
       
        //complete
    }
 
    
    public int leader()
    {
        int min = 999999;
        int leader = 0;
        
        
        for(int j : members)
        {
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
        
        return leader;
    } 
        
    public void updateTimeout(int j)
    {
        //step 08
        timeout.put(j,timeout.get(j)+1);
        
        if(!silent.contains(j))
            silent.add(j);
    }
    
    
    public int processMessage(message msg)
    {

            //step 11
            if(this.members.contains(msg.k))
            {   
                //step 09
                if(msg.snk <= this.state.get(msg.k).snk)
                {
                     return 0;
                }
                this.state.put(msg.k,msg);

                //step12
                //stop timer
                this.to_reset.add(msg.k);
                if(this.silent.contains(msg.k))
                {
                    this.silent.remove((Object)msg.k);
                } 
                Timer t_aux = this.timer.get(msg.k);
                if(t_aux != null)
                {
                    t_aux.cancel();
                }

            }
            else
            {
                //step13
             
                this.state.put(msg.k,msg);

                //step14/15/16
                //System.out.println("putting 0 in" + msg.k + " ");
                this.susp_level.put(msg.k,0);
                this.suspected_by.put(msg.k,new CopyOnWriteArrayList());
                this.timeout.put(msg.k,this.eta);
                this.timer.put(msg.k,create_timer(msg.k));
                this.members.add(msg.k);
                this.to_reset.add(msg.k);
                

            }

            //System.out.println("keyset = " + msg.susp_level.keySet());
            //step18
            for(int l : msg.susp_level.keySet())
            {
                if(this.susp_level.get(l) != null)
                {
                    this.susp_level.put(l,max(this.susp_level.get(l),msg.susp_level.get(l)));
                }
                this.susp_level.put(l,max(0,msg.susp_level.get(l)));   
            }

            //step 19
            for(int l : msg.silent )
            {
                CopyOnWriteArrayList<Integer> aux = this.suspected_by.get(l);
                if(this.suspected_by.get(l) != null)
                {
                    aux.add(msg.k);
                    this.suspected_by.put(l,aux); // alocar?
                }  
            }
            return 1;
    }

    public int start()
    {
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
                    //System.out.println("recv count = " + sn);
                    //System.out.println("leader = " + leader() + " my.pid = " + pid + "");
                    //System.out.println("timeout leader = " + timeout.get(leader()));
                }
            }
            System.out.println("Stop rx thread pid " + pid);
        }
    });
    RxThread.start();  
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
                    Thread.sleep(eta);
                } 
                catch (InterruptedException ex) 
                {
                    System.out.println("exception in txthread pid " + pid);
                }
                //t_alloc = true;
                task1();
                //t_alloc = false;
                //System.out.println(" ");
                //System.out.println("Seq num: " + sn);
            }
            System.out.println("Stop tx thread pid " + pid);
        }
 
    });
    TxThread.start();
    PtThread = new Thread(new Runnable() 
        {
            @Override
            public void run() 
            {
                int time = 0;
                while(ptflag)
                try 
                {
                    System.out.println(pid +": PID: " + pid + " Leader :" + leader());
                    System.out.println(pid +": Silent :" + silent);
                    for(int j : members)
                    {
                        System.out.println(pid + ": pid: " +  j +  " susp_level " + susp_level.get(j));
                    }
                    System.out.println("");
                    System.out.println(pid + ": Time Spent: " + time + " seconds");
                    System.out.println(pid + ":---------------------------");
                    Thread.sleep(1000);
                    time++;
                } 
                catch(InterruptedException ex) 
                {
                    //Logger.getLogger(process.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("exception in pthread pid " + pid);
                }
                //System.out.println("exiting ptthread pid "+pid);
            }
        });
        PtThread.start();
    
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
                updateTimeout(j);
            }
        };
        
        t_aux.schedule(task,this.timeout.get(j));
        
        return t_aux;
    }
    
    public int stop()
            {
                //RxThread.interrupt();
                //RxThread.interrupt();
                //PtThread.interrupt();
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
    