/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderprotocol1;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vitor
 */
public class Lock{

  private boolean isLocked = false;
  
  public Lock()
  {
      
  }

  public synchronized void lock()
  {
    while(isLocked){
        try {
            wait();
        } catch (InterruptedException ex) {
            Logger.getLogger(Lock.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    isLocked = true;
  }

  public synchronized void unlock(){
    isLocked = false;
    notify();
  }
}
