/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customdatagram;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 *
 * @author vitor
 */
public class SocketStats {
    
    private static final String FILENAME = "stats.txt";
    private int pid;
    private int totalmsg;
    private int lostmsg;
    private float avgdelay;
    private File out;
    private FileOutputStream outstream;
    
    
    public SocketStats(int pid)
        {
            this.totalmsg = 0;
            this.lostmsg = 0;
            this.avgdelay = 0; 
            this.pid = pid;
        }
    
    public void update(boolean lost, double delay)
    {
        totalmsg++;
        avgdelay+=delay;
        
        if(lost)
        {
            lostmsg++;
        }
    
    }
    
    public void saveStats()
    {
        out = new File(FILENAME);
        try 
        {
            out.createNewFile(); // if file already exists will do nothing
            outstream = new FileOutputStream(out, true);
            avgdelay = (float)(this.avgdelay/(this.totalmsg-this.lostmsg));
            String towrite = "PID: " + this.pid + "    AVGDelay: " + avgdelay + "    Lostmsg: " + this.lostmsg + "    totalmsg: " + this.totalmsg + "\n";
            outstream.write(towrite.getBytes(Charset.forName("UTF-8"))); 
            outstream.close();
        } 
        catch (IOException ex) 
        {
            System.out.println("Unable to create stats file!");
        }   
    }
    
}
