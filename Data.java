import java.io.*;
import java.net.*;
import java.util.Date;
import java.time.LocalDateTime;
import java.lang.*;
import java.util.concurrent.locks.*;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import java.time.*;
/*
 * Data class is the global data storage 
 * items must be in a class to be passed by reference instead of value 
 * _cont = program continue 
 * _coll = collection threads continue 
 *  
 */
public class Data{
	// setup interface for the capplication 
	public interface cspi extends Library{
		public int opengpio(int pin);
		public void setgpio(int fd,int val);
		public void closegpio(int fd);
	};
	//private 
	volatile boolean _cont;
	volatile boolean _coll;
	
	//lists for data 
	public static int listlength=16384;
	public static String fileLocation = "/home/cody/data/";
	public static String logfile = fileLocation+"log.txt";
	public volatile int[][][] lists;
	public ReentrantLock[] _locks;
	public cspi CSPI;
	public int fd;
	public boolean[] listhasdata;
	public int[] chList;
	public int chCount;
	
	
	//server port number 
	static int portNumber = 5002;
	static int dataportNumber = 5003;
	
	//timerdata 
	public int tme = 1*10*60*1000;//10 minute 
	
	
	//configure
	public String configfile = fileLocation+"config.xml";
	public boolean autostart = false; //autostart default to no
	public int frequency = 250;
	
	//constructor
	public Data(ReentrantLock[] locks)
	{
		_locks = locks;
		_cont = true;
		_coll = false;
		// setup the c interface 
		CSPI = (cspi)Native.loadLibrary("cspi",cspi.class);
		fd = CSPI.opengpio(204);
		if(fd <0)
		{
			log("Unable to open GPIO");
			_cont = false;
		}
		listhasdata = new boolean[2];
		listhasdata[0] = false;
		listhasdata[1] = false;
		chList = new int[16];
	}
	
	//set continue 
	public void setcont(boolean value)
	{
		try{
			_locks[2].lock();
				_cont = value;
			_locks[2].unlock();
		}
		catch(Exception ex)
		{
			System.out.println("Exiting error");
		}
	}
	//get continue 
	public boolean getcont()
	{
		boolean value = false;
		try{
			_locks[2].lock();
				value = _cont;
			_locks[2].unlock();
		}
		catch(Exception ex)
		{
			System.out.println("Exiting error");
		}
		return value;
	}
	//set collect 
	public void setcoll(boolean value)
	{
		try{
			_locks[3].lock();
				_coll = value;
			_locks[3].unlock();
		}
		catch(Exception ex)
		{
			System.out.println("Exiting error");
		}
	}
	//get coollect  
	public boolean getcoll()
	{
		boolean value = false;
		try{
			_locks[3].lock();
				value = _coll;
			_locks[3].unlock();
		}
		catch(Exception ex)
		{
			System.out.println("Exiting error");
		}
		return value;
	}
	
	
	//Global logger 
	public void log(String txt)
	{
		try{
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(logfile,true)));
		writer.write(LocalDateTime.now().toString()+"_"+txt+ "\r\n");
		writer.close();
		}
		catch(Exception ex)
		{
			System.out.println("Log Writer exception");
			
		}
	}
	
	
	

}
