
import java.io.*;
import java.util.concurrent.locks.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import java.net.*;

/*
 *Data collector collects the data and stores it in two lists from Data 
 * optional interface for sending over the network the data values as well  
 * 
 * C interface for data collection used stored in cspi.c //compiled to a library
 * 
 */
public class DataCollector extends Thread{
	// setup interface for the capplication 
	public interface cspi extends Library{
		public String test();
		public int openspi();
		public void setupspi(int fd);
		public int xfer(int fd, int cmd);
		public void closespi(int fd);
	};
	// data used by the collector
	private cspi CSPI;
	private Data _dat;
	private ReentrantLock[] _runlocklst;
	private int _chlist[];
	private int	_freq; 
	private int _chlen;
	private double _delay;
	private boolean _net;
	
	//Data collector
	public DataCollector(ReentrantLock[] runlocks,Data dat,boolean net)
	{
		_net = net;
		_runlocklst = runlocks;
		_dat = dat;
		_freq = _dat.frequency;
		if(net)
			_freq = _freq/100;
		
		_chlen = _dat.chCount;//16;
		_chlist = _dat.chList;//new int[_chlen];
		/*_chlist[0] = 0;
		_chlist[1] = 8;
		_chlist[2] = 1;
		_chlist[3] = 9;
		_chlist[4] = 2;
		_chlist[5] = 10;
		_chlist[6] = 3;
		_chlist[7] = 11;
		_chlist[8] = 4;
		_chlist[9] = 12;
		_chlist[10] = 5;
		_chlist[11] = 13;
		_chlist[12] = 6;
		_chlist[13] = 14;
		_chlist[14] = 7;
		_chlist[15] = 15;*/
		_delay = (1.0/(_chlen * (double)_freq))*1000000000;
		//System.out.println("Freq");
		//System.out.println(_delay);
		_dat.log("Sampleing at frequency: "+_freq);
		_dat.log("Delay set to: "+_delay+" seconds");
	}
	//thread run 
	public void run()
	{
		if(_net)
		{
			runnet();
		}
		else
		{
			runsave();
		}
	}
	
	//run thread on start 
	public void runsave()
	{
		// setup the c interface 
		CSPI = (cspi)Native.loadLibrary("cspi",cspi.class);
		//System.out.println(CSPI.test());//SPI
		int fd = CSPI.openspi();
		//two bytes for the control register of the AD7869 
		int high =241;//ch0 
		int low = 36;//initial 
		//setup the send value 
		int value = (high<<8)|low;
		//call for ioctl
		CSPI.setupspi(fd);
		//this is the start timer for periodic sampling
		long startTime = System.nanoTime();
		int retval;
		int index =0;
		int listindex =0;
		int sampleindex =0;
		//get details of the list of channels 
		_dat.lists =new int[2][_dat.listlength][_chlen];
		//start with list 0 get a lock so we are able to fill it. 
		_dat._locks[0].lock();
		while(_dat.getcoll())
		{
			//First store value from IC,  Note the value we get from the ADC was the control register it was sent two sends ago 
			//additional note chip 1 has a 2 command delay and chip 2 has a 2 command delay up to 4 commands depending on how they are 
			//organized in the channel list 
			//System.out.println(CSPI.xfer(fd,value));
			_dat.lists[listindex][sampleindex][index] = CSPI.xfer(fd,value);
			//update value with the new channel information  this takes the base channel 000 or thechannel from list 
			value = (241 | ((_chlist[(index+2)%_chlen]%8)<<1))<<8 | low;
			//System.out.println("filling index: "+index+"sent ch :"+	_chlist[(index+2)%_chlen] +" chindex:"
			//					+((index+2)%_chlen));
			//if channel is >7 IE 8 then set the gpio for chip 2 
			if(_chlist[index] >7)
				_dat.CSPI.setgpio(_dat.fd,0);
			else 
				_dat.CSPI.setgpio(_dat.fd,1);
			//increase the index for next channel 	
			index++;
			//check for last channel in list and reset to first channel increase sample row
			if(index >= (_chlen))
			{
				index =0;
				sampleindex++;
			}
			//check if list is full and swap lists if it is 
			if(sampleindex >= _dat.listlength)
			{
				try{
					_dat.listhasdata[listindex] = true;
					//System.out.println("list "+listindex+" Has data");
					_dat._locks[listindex].unlock();
					listindex++;
					if(listindex >= 2)
						{listindex =0;}
					if(_dat.getcoll())
					{_dat._locks[listindex].lock();}
					//_dat._locks[0].tryLock(10,TimeUnit.SECONDS);
					sampleindex =0;
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}	
			//wait until next sample is due
			while((System.nanoTime()-startTime)<_delay)
			{	}
			startTime += _delay;
			//_dat.setcont(false);
		}
		_dat.log("Exited DataCollector");
		
	}
	//this function samples and returns values to a connected tcp network listener 
	public void runnet()
	{
		ServerSocket serverSocket = null;
		try{
			
			// setup the c interface 
			CSPI = (cspi)Native.loadLibrary("cspi",cspi.class);
			//System.out.println(CSPI.test());//SPI
			int fd = CSPI.openspi();
			int high =241;//ch0 
			int low = 36;//initial 
			int value = (high<<8)|low;
			CSPI.setupspi(fd);
			long startTime = System.nanoTime();
			int retval;
			int index =0;
			int sampleindex =0;
			serverSocket = new ServerSocket(_dat.dataportNumber);
			Socket clientSocket =null;
			clientSocket = serverSocket.accept();
			_dat.log("Data Connection accepted");
			PrintWriter networkwriter = new PrintWriter(clientSocket.getOutputStream(),true);
			//_dat.lists =new int[2][_dat.listlength][_chlen];
			//_dat._locks[0].lock();
			while(_dat.getcoll())
			{
				//System.out.println(CSPI.xfer(fd,value));
				//_dat.lists[listindex][sampleindex][index] = CSPI.xfer(fd,value);
				networkwriter.println(_chlist[(index+2)%_chlen] +","+CSPI.xfer(fd,value));
				value = (241 | ((_chlist[(index+2)%_chlen]%8)<<1))<<8 | low;
				//System.out.println("filling index: "+index+"sent ch :"+	_chlist[(index+2)%_chlen] +" chindex:"
				//					+((index+2)%_chlen));
				if(_chlist[index] >7)
					_dat.CSPI.setgpio(_dat.fd,0);
				else 
					_dat.CSPI.setgpio(_dat.fd,1);
					
				index++;
				if(index >= (_chlen))
				{
					index =0;
					sampleindex++;
				}
				while((System.nanoTime()-startTime)<_delay)
				{	}
				startTime += _delay;
				//_dat.setcont(false);
			}
			clientSocket.close();
		}//end try
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try{
			serverSocket.close();
			}
			catch(Exception ex)
			{ex.printStackTrace();}
		}
		_dat.log("Exited DataCollector");
	}
	
	

}
