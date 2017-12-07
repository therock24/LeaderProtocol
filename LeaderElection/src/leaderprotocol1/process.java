/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderprotocol1;

/**
 *
 * @author Vitor
 */
public class process 
{
    private static final int MAX_PROC = 500;
    private final int pid; // process id (java process id? )
    private int[] members; // contains all known pids
    private int[] timer; // timer to check if link is timely
    private int[] timeout; // timeout value increased when timer expires
    private int[] silent; // contains all processes that timer expired since last reset
    private int[] to_reset; // list of processes that will need to reset timer
    private int[] susp_level; // level of suspicion that process j crashed 
    private int[][] suspected_by; // list of processes that suspect process j
    private int sn; // local seq number of message
    private message[] state; // last message received by process k
    
    
    
    public process(int pid)
    {
            members = new int[MAX_PROC];
            timer = new int[MAX_PROC];
            timeout = new int[MAX_PROC];
            silent = new int[MAX_PROC];
            to_reset = new int[MAX_PROC];
            susp_level = new int[MAX_PROC];
            suspected_by = new int[MAX_PROC][MAX_PROC];
            state = new message[MAX_PROC];
            sn=0;
            this.pid = pid;
            
            
            for (int i = 0; i < MAX_PROC; i++) 
            {
                members[i] = 0;
                timer[i] = 0;
                timeout[i] = 0;
                silent[i] = 0;
                to_reset[i] = 0;
                susp_level[i] = 0;
                for (int j = 0; j < MAX_PROC; j++) 
                {
                    suspected_by[i][j] = 0;
                }
                state[i].k = 0;
                state[i].snk = 0;
                for (int j = 0; j < MAX_PROC; j++) 
                {
                    state[i].susp_level[j] = 0;
                }
                state[i].silent = 0;
                
            }
            
            
        
    }
    
}
