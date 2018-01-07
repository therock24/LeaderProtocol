/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderprotocol1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Vitor
 */
public class message implements Serializable 
{
    public int k;
    public int snk;
    public ConcurrentMap<Integer,Integer> susp_level;
    public CopyOnWriteArrayList<Integer> silent;
    
    public message()
    {
        k=0;
        snk=0;
        susp_level = new ConcurrentHashMap();
        silent = new CopyOnWriteArrayList();;
    }
}
