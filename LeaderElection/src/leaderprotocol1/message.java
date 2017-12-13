/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderprotocol1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Vitor
 */
public class message 
{
    public int k;
    public int snk;
    public Map<Integer,Integer> susp_level;
    public ArrayList<Integer> silent;
    
    public message()
    {
        k=0;
        snk=0;
        susp_level = new HashMap();
        silent = new ArrayList();;
    }
}
