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
public class message {
    
    public static final int HEARTBEAT = 1;
    public static final int STOP_LEADER = 2;
    public static final int SUSPICION = 3;
    public int tag_k; // 1- heartbeat ; 2 - stop_leader; 3 - suspicion
    public int k; // id of k
    public int sl_k; // suspect level of k
    public int silent_k; // id of process suspected by k (only suspicion msgs)
    public int hbc_k; //value of k period counter (0 in suspicion)
    
    public message(int tag, int k, int slk, int silent_k, int hbc_k)
    {
        
        this.tag_k=tag;
        this.k=k;
        this.sl_k=slk;
        this.silent_k= silent_k;
        this.hbc_k = hbc_k;
    }
    
}
