/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderprotocol2;
 
import customdatagram.CustomSocket;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vitor
 */
public class Main {
    
     public static void main(String[] args) throws InterruptedException, FileNotFoundException, IOException {
        // TODO code application logic here
        boolean measure = true;
        
        if(measure)
        {
            //for (int n_proc = 3; n_proc <= 10; n_proc++) 
        //{
            int n_it = 100;
            int n_proc = 4;
            boolean flag1 = true, flag2 = false;
            int leader = 0; 

            File out;
            FileOutputStream outstream;
            out = new File("2timingsp"+n_proc+".csv");
            out.createNewFile(); // if file already exists will do nothing
            outstream = new FileOutputStream(out, false);

            float total = 0;
            float total2 = 0;
            
            for (int z = 0; z < n_it; z++) 
            {
                flag1 = true;
                flag2 = false;
                process pr[]=new process[n_proc];
                for(int i = 0; i < n_proc; i++)
                {
                    
                    pr[i] = new process(i,0,0,10,100);
                    //System.out.println("pid = " + pr[i].pid );
                }
                
                long start=System.nanoTime();
                for (int i = 0; i < n_proc; i++) 
                {

                   pr[i].start();
                   pr[i].ptflag = true;
                }
                
                
                while(flag1)
                {
                    flag2 = false;
                    leader=pr[0].leader();
                    for (int j = 1; j < n_proc; j++) 
                    {
                        if(leader != pr[j].leader())
                        {
                            flag2 = true;
                            break;
                        }
                    }
                    if(!flag2)
                    {
                        flag1=false;
                    }

                }
                float totaltime = (float)(System.nanoTime() - start)/1000000;
                //System.out.println("totaltime " + totaltime);
                String towrite = ""+ totaltime;
                total += totaltime;
                outstream.write(towrite.getBytes(Charset.forName("UTF-8")));
                

                flag1 = true;
                System.out.println("starting second election");
                pr[0].stop();
                start=System.nanoTime();
                while(flag1)
                {
                    flag2 = false;
                    leader=pr[1].leader();
                    /*for (int o = 1; o < n_proc; o++) 
                    {
                        //System.out.print(pr[o].leader());
                    }
                    //System.out.println();
                   */
                    for (int j = 2; j < n_proc; j++) 
                    {
                            if(leader != pr[j].leader())
                            {
                                flag2= true;
                                break;
                            }
                    }
                    if(!flag2 && leader != 0 )
                    {
                        flag1=false;
                    }
                }
                System.out.println("leader = " + leader);
                totaltime = (float)(System.nanoTime() - start)/1000000;
                //System.out.println("totaltime " + totaltime);
                total2 += totaltime;
                towrite = ","+ totaltime + "\n";
                outstream.write(towrite.getBytes(Charset.forName("UTF-8")));
                System.out.println("iteration nº" + z);
                
                
                for (int i = 0; i < n_proc; i++) 
                {
                   pr[i].stop();
                }
                
                
            }
            String towrite = "avg = "+ total/(float)n_it + "\navg2 = " + total2/(float)n_it + "\n";
            outstream.write(towrite.getBytes(Charset.forName("UTF-8")));
            outstream.close();
            System.out.printf(towrite);
            System.exit(0);
        }
        else
        {
         
         process p1 = new process((int)(Math.random()*10000),Integer.parseInt(args[0]),Float.parseFloat(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]));
         p1.start();
         sleep(Integer.parseInt(args[4])*1000);  
         p1.stop();
         p1.outputStats();
         System.exit(0);
        }
    }
}
