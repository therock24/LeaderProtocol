/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderprotocol1;

import static java.lang.Integer.max;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Vitor
 */
public class process 
{
    private static final int alpha = 10; // min number of processes that do not crash
    private static final int t_unit = 10;
    private final int pid; // process id (java process id? )
    private ArrayList<Integer> members; // contains all known pids
    private Map<Integer,Integer> timer; // timer to check if link is timely
    private Map<Integer,Integer> timeout; // timeout value increased when timer expires
    private ArrayList<Integer> silent; // contains all processes that timer expired since last reset
    private ArrayList<Integer> to_reset; // list of processes that will need to reset timer
    private Map<Integer,Integer> susp_level; // level of suspicion that process j crashed 
    private Map<Integer,ArrayList<Integer>> suspected_by; // list of processes that suspect process j
    private int sn; // local seq number of message
    private Map<Integer,message> state; // last message received by process k
    
    
    
    public process(int pid)
    {
            this.pid = pid;
            members = new ArrayList();
            timer = new HashMap();
            timeout = new HashMap();
            silent = new ArrayList();
            to_reset = new ArrayList();
            susp_level = new HashMap();
            suspected_by = new HashMap();
            state = new HashMap();
            state.put(this.pid,new message());
            sn=0;
    }
    
    public void task1()
    {
        //step 01
        sn++;
   
        //step 02
        for (int j : silent)
        {
            ArrayList<Integer> list = suspected_by.get(j);
            if(list == null)
            {
                list = new ArrayList();
            }
            list.add(this.pid);
            suspected_by.put(j,list);
        }
        
        //step 03
        for(int j : members)
        {
            if (suspected_by.get(j).size() >= this.alpha)
            {
                //step 04
                susp_level.put(j,susp_level.get(j)+1);
            }
            suspected_by.put(j,new ArrayList());
        }
        
        //step 05
        message aux = state.get(this.pid);
        aux.k = this.pid;
        aux.snk = this.sn;
        aux.susp_level.putAll(this.susp_level);
        aux.silent = (ArrayList<Integer>)this.silent.clone();
        state.put(this.pid,aux);
        
        //step 06
        //broadcast(state)
        
        //step 07
        for(int j : this.to_reset)
        {
            this.timer.put(j, this.timeout.get(j));
        }
        this.to_reset = new ArrayList();
       
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
        }
        return leader;
    }
    
    public void updateTimeout(int j)
    {
        //step 08
        timeout.put(j,timeout.get(j)+1);
        silent.add(j);
    }
    
    public int processMessage(message msg)
    {
        //step 09
        //perguntar ao prof nao sei quando
        if(msg.snk <= this.state.get(msg.k).snk)
        {
             return 0;
        }
        
        //step 10
        
        
        //step 11
        if(this.members.contains(msg.k))
        {   
            this.state.put(msg.k,msg);
            
            //step12
            //stop timer
            this.to_reset.add(msg.k);
            this.silent.remove(msg.k);
            
        }
        else
        {
            //step13
            this.state.put(msg.k,msg);
            
            //step14/15/16
            this.susp_level.put(msg.k,0);
            this.suspected_by.put(msg.k,new ArrayList());
            this.timeout.put(msg.k,this.t_unit);
            this.timer.put(msg.k,this.timeout.get(msg.k));
            this.members.add(msg.k);
            this.to_reset.add(msg.k);
                    
        }
        
        //step18
        for(int l : msg.susp_level.keySet())
        {
            this.susp_level.put(l,max(this.susp_level.get(l),msg.susp_level.get(l)));
        }
        
        //step 19
        for(int l : msg.silent )
        {
            ArrayList<Integer> aux = this.suspected_by.get(l);
            aux.add(msg.k);
            this.suspected_by.put(l,aux);
        }
        return 1;
    }
}
    