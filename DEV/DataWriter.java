

import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.concurrent.locks.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;


public class DataWriter extends Thread{

	// 
	private Data _dat;
	private ReentrantLock[] _runlocklst;

	
	//Data collector
	public DataWriter(ReentrantLock[] runlocks,Data dat)
	{
		
		_runlocklst = runlocks;
		_dat = dat;
	}
	
	//run thread on start 
	public void run()
	{
		try{
			
	
			while(_dat.getcont())
			{
				try{
					
						Thread.sleep(1000);
						if(_dat.listhasdata[0])
						{
							//System.out.println("list 0 has data ");
							
							boolean locked = _dat._locks[0].tryLock();
							if(_dat._locks[0].isHeldByCurrentThread())
							{
							PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("/home/cody/data/output.txt",true)));
							for(int x=0;x<_dat.listlength;x++)
							{
								for(int intval :_dat.lists[0][x])
									writer.write(intval+",");
								writer.write("\r\n");
							}
							writer.close();
							_dat.listhasdata[0] = false;
							
							_dat._locks[0].unlock();
							}
						}
						if(_dat.listhasdata[1])
						{	
							//System.out.println("list 1 has data ");
							boolean locked =_dat._locks[1].tryLock();
							if(_dat._locks[1].isHeldByCurrentThread()){
							PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("/home/cody/data/output.txt",true)));
							for(int x=0;x<_dat.listlength;x++)
							{
								for(int intval :_dat.lists[1][x])
									writer.write(intval+",");
								writer.write("\r\n");
							}
							writer.close();
							_dat.listhasdata[1] = false;
							_dat._locks[1].unlock();
							}
						}
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
				}//end while 
			
			}
			catch(Exception ex)
			{
				_dat.setcont(false);
				System.out.println("Error output file");
				ex.printStackTrace();
			} 
	}//end of run 
}
