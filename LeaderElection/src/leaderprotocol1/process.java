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
    private message[] state;
    
    
    
    public process(int pid)
    {
            this.pid = pid;
            susp_level = new int[MAX_PROC];
            
        
    }
    
}
