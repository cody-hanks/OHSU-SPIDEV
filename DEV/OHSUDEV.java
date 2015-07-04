

import java.io.*;
import java.net.*;
import java.util.Date;
import java.lang.*;
import java.util.concurrent.locks.*;


public class OHSUDEV
{

	
	
	
	
	public static void main(String[] args)
	{


		// now setup for arrays and objects 
		
		ReentrantLock[] locks = new ReentrantLock[6];
		locks[0] = new ReentrantLock();//locks for list acces 
		locks[1] = new ReentrantLock();//locks for list acces
		locks[2] = new ReentrantLock();//lock for continue boolean
		
		//setup a run stop variable 
		Data dat = new Data(locks);
		
		//Starting server 
		System.out.println("Starting OHSU Sleep server");
		//dat.lists = new long[2][listlength][chlist];
		
		//create the runnable thread objects
		Runnable ServerThread = new Server(locks,dat);
		
		//start the server thread
		new Thread(ServerThread).start();
		
		
		//(new Thread(new Server())).start();//start command server 
		//(new Thread(new DataCollector())).start();
	
		System.out.println("---------------------------------");
		while(dat.getcont())
		{
			try
			{
				Thread.sleep(10*1000);
				//System.out.println("bool: " + dat.getcont());
			}
			catch(Exception ex)
			{
					System.out.println("OHSU Exception");
			}
		}
		System.out.println("Application Exit");
	
	}
}
