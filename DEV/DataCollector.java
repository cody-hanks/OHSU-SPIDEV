
import java.io.*;
import java.util.concurrent.locks.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;



public class DataCollector extends Thread{
	// setup interface for the capplication 
	public interface cspi extends Library{
		public String test();
		public int openspi();
		public void setupspi(int fd);
		public int xfer(int fd, int cmd);
		public void closespi(int fd);
	};
	// 
	private cspi CSPI;
	private Data _dat;
	private ReentrantLock[] _runlocklst;
	private int _chlist[];
	private int	_freq; 
	private int _chlen;
	private double _delay;
	
	//Data collector
	public DataCollector(ReentrantLock[] runlocks,Data dat)
	{
		
		_runlocklst = runlocks;
		_dat = dat;
		_freq = 250;
		_chlen = 4;
		_chlist = new int[_chlen];
		_chlist[0] = 0;
		_chlist[1] = 8;
		_chlist[2] = 1;
		_chlist[3] = 2;
		_delay = (1.0/(_chlen * (double)_freq))*1000000000;
		System.out.println("Freq");
		System.out.println(_delay);
	}
	
	//run thread on start 
	public void run()
	{
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
		int listindex =0;
		int sampleindex =0;
		_dat.lists =new int[2][_dat.listlength][_chlen];
		_dat._locks[0].lock();
		while(_dat.getcont())
		{
			//System.out.println(CSPI.xfer(fd,value));
			_dat.lists[listindex][sampleindex][index] = CSPI.xfer(fd,value);
			value = (241 | ((_chlist[(index+2)%_chlen]%8)<<1))<<8 | low;
			//System.out.println("filling index: "+index+"sent ch :"+	_chlist[(index+2)%_chlen] +" chindex:"
			//					+((index+2)%_chlen));
			if(_chlist[index] >7)
				_dat.CSPI.setgpio(_dat.fd,1);
			else 
				_dat.CSPI.setgpio(_dat.fd,0);
				
			index++;
			if(index >= (_chlen))
			{
				index =0;
				sampleindex++;
			}
			if(sampleindex >= _dat.listlength)
			{
				try{
					_dat.listhasdata[listindex] = true;
					//System.out.println("list "+listindex+" Has data");
					_dat._locks[listindex].unlock();
					listindex++;
					if(listindex >= 2)
						{listindex =0;}
					_dat._locks[listindex].lock();
					//_dat._locks[0].tryLock(10,TimeUnit.SECONDS);
					sampleindex =0;
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}	

			while((System.nanoTime()-startTime)<_delay)
			{	}
			startTime += _delay;
			//_dat.setcont(false);
		}
		
	}
	
	
	
	

}
