/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderprotocol2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Vitor
 */
public class process {
    
    
    private static final int alpha = 10; // min number of processes that do not crash
    private static final int t_unit = 10;
    private final int pid; // process id (java process id? )
    private ArrayList<Integer> members; // contains all known pids
    private ArrayList<Integer> contenders; // contains all pids of leader candidates
    private Map<Integer,Integer> timer; // timer to check if link is timely
    private Map<Integer,Integer> timeout; // timeout value increased when timer expires
    private int silent; // contains all processes that timer expired since last reset
    private ArrayList<Integer> to_reset; // list of processes that will need to reset timer
    private int susp_level; // level of suspicion that process j crashed 
    private Map<Integer,ArrayList<Integer>> suspected_by; // list of processes that suspect process j
    private int hbc; // number of periods that i was considered leader
    private Map<Integer,Integer> last_stop_leader; // greatest hbc value received from k
    
    
    
    public process(int pid)
    {
            this.pid = pid;
            members = new ArrayList();
            timer = new HashMap();
            timeout = new HashMap();
            silent=0;
            to_reset = new ArrayList();
            suspected_by = new HashMap();
            //init
            members.add(pid);
            contenders.add(pid);
            hbc = 0;
            susp_level=0;
    }
    
     public int broadcast(message msg)
    {
        return 0;
    }
    
    public void task1()
    {
        boolean next_period = false;
        //step 01
            while(leader() == this.pid)
            {
                //step 02
                if(!next_period)
                {
                    next_period=true;
                    this.hbc+=1;
                }
                message msg= new message(1,this.pid,this.susp_level,0,this.hbc);
                
                //step 03
                broadcast(msg);
            }
            if(next_period)
            {
                message msg = new message(2,this.pid,this.susp_level,0,this.hbc);
                //step 04
                broadcast(msg);
            }
    }
    
    public int leader()
    {
        int leader = 0;
        
        return leader;
    }
    
    public int timerExpired(int j)
    {
        timeout.put(j,timeout.get(j)+1);
        message msg = new message(3,this.pid,this.susp_level,j,0);
        broadcast(msg);
        //contenders.
        return 1;
    }
    
    public int processMessage(message msg)
    {
        return 1;
    }
   
}
