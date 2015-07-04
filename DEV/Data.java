import java.io.*;
import java.net.*;
import java.util.Date;
import java.time.LocalDateTime;
import java.lang.*;
import java.util.concurrent.locks.*;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;



public class Data{
	// setup interface for the capplication 
	public interface cspi extends Library{
		public int opengpio(int pin);
		public void setgpio(int fd,int val);
		public void closegpio(int fd);
	};
	//private 
	volatile boolean _cont;
	
	//lists for data 
	public static int listlength=16384;
	public volatile int[][][] lists;
	public ReentrantLock[] _locks;
	public cspi CSPI;
	public int fd;
	public boolean[] listhasdata;
	
	
	//constructor
	public Data(ReentrantLock[] locks)
	{
		_locks = locks;
		_cont = true;
		// setup the c interface 
		CSPI = (cspi)Native.loadLibrary("cspi",cspi.class);
		fd = CSPI.opengpio(204);
		if(fd <0)
		{
			System.out.println("Unable to open GPIO");
			_cont = false;
		}
		listhasdata = new boolean[2];
		listhasdata[0] = false;
		listhasdata[1] = false;
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
	
	
	
	
	

}
