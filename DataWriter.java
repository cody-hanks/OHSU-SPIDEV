

import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.concurrent.locks.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import java.time.*;

/*
 * Data writer includes timer class used to age file names 
 * Data writer loops while data.coll to write data arrays to disk
 * Data wrtier also writes the time sync file
 * 
 */
public class DataWriter extends Thread{
	//reminder task extends the timer task to age file names call back used to the data writerclass 
	//callback when name has changed locks both lists for a small amount of time in order to prevent writing part of a list 
	// in two seperate files. 
	//files change at 1AM and 1PM 
	class ReminderTask extends TimerTask{
		//data used in reminder task class 
		private Data _dat;
		private ReentrantLock[] _runlocklst;
		private DataWriter _DW;
		//constructor
		public ReminderTask(ReentrantLock[] runlocks,Data dat,DataWriter DW)
		{
			_runlocklst = runlocks;
			_dat = dat;
			_DW=DW;
		}
		// timer task runs when time has expired
		public void run(){
			//run onl if the filename has actually changed 
			if(_DW.filenamechanged())//_dat.getcont())
			{
				Timer tmr = new Timer();
				_DW.updatefilename();
				_dat.log("Updated File Name");
				tmr.schedule(new ReminderTask(_runlocklst,_dat,_DW),_dat.tme);
			}
			else//else will reset timer and log 
			{
				_dat.log("Filename checked");
				Timer tmr = new Timer();
				tmr.schedule(new ReminderTask(_runlocklst,_dat,_DW),_dat.tme);
			}
		}//end run 
	}//end class reminder task  

	// data for the data writer class 
	private Data _dat;
	private ReentrantLock[] _runlocklst;
	private Timer tmr;
	private String filename;
	private LocalDateTime lasttimer;
	
	//Data writer constructor
	public DataWriter(ReentrantLock[] runlocks,Data dat)
	{
		_runlocklst = runlocks;
		_dat = dat;
		//Setup the Data writer 
		lasttimer = LocalDateTime.now();
		//start the timer for file ageing
		tmr = new Timer();
		tmr.schedule(new ReminderTask(runlocks,dat,this),_dat.tme);//time set in Data tme 
		
	}
	//run thread on start of threaded data writer runs as long as data .coll is true
	public void run()
	{
		try{
			_getfilename();//set inital file name 
			_dat.log("writing to file "+_dat.fileLocation+filename);
			while(_dat.getcoll())
			{
				try{
					
						Thread.sleep(500);//long sleeps between checking for list ready to be written as list take some time to fill
						if(_dat.listhasdata[0])//check for data in list 0 
						{
							//there is data in list 0 
							boolean locked = _dat._locks[0].tryLock();// try to lock if you cant get it dont do anything
							if(_dat._locks[0].isHeldByCurrentThread())
							{
								//we got the lock 
								File f = new File(_dat.fileLocation+filename+".txt");
								long lines;
								//check for current lines in file this is for the time sync 
								if(f.exists())
								{
									LineNumberReader lnr = new LineNumberReader(new FileReader(new File(_dat.fileLocation+filename+".txt")));
									lnr.skip(Long.MAX_VALUE);
									lines = lnr.getLineNumber();
									lnr.close();
								}
								else{lines=0;}
								//setup writers for the time file and the data file 
								PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(_dat.fileLocation+filename+".txt",true)));
								PrintWriter timewriter = new PrintWriter(new BufferedWriter(new FileWriter(_dat.fileLocation+"time"+filename+".txt",true)));
								//ok write the sync line and data 
								timewriter.write(lines+","+LocalDateTime.now().toString()+"\r\n");
								for(int x=0;x<_dat.listlength;x++)
								{
									for(int intval :_dat.lists[0][x])
										writer.write(intval+",");
									writer.write("\r\n");
								}
								//cleanup close files 
								writer.close();
								timewriter.close();
								//reset has data 
								_dat.listhasdata[0] = false;
								//unlock list0
								_dat._locks[0].unlock();
							}//end if 
						}//end if list0 has data 
						if(_dat.listhasdata[1])
						{	
							//list 1 has data same as above reference changed to list 1 
							boolean locked =_dat._locks[1].tryLock();
							if(_dat._locks[1].isHeldByCurrentThread()){
							File f = new File(_dat.fileLocation+filename+".txt");
							long lines;
							if(f.exists())
							{
							LineNumberReader lnr = new LineNumberReader(new FileReader(new File(_dat.fileLocation+filename+".txt")));
							lnr.skip(Long.MAX_VALUE);
							lines = lnr.getLineNumber();
							lnr.close();
							}
							else{lines=0;}
							
							PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(_dat.fileLocation+filename+".txt",true)));
							PrintWriter timewriter = new PrintWriter(new BufferedWriter(new FileWriter(_dat.fileLocation+"time"+filename+".txt",true)));
							timewriter.write(lines+","+LocalDateTime.now().toString()+"\r\n");
							for(int x=0;x<_dat.listlength;x++)
							{
								for(int intval :_dat.lists[1][x])
									writer.write(intval+",");
								writer.write("\r\n");
							}
							writer.close();
							timewriter.close();
							_dat.listhasdata[1] = false;
							_dat._locks[1].unlock();
							}
						}//end list1 has data 
					}//end try 
					catch(Exception ex)
					{
						_dat.log("Error writing data to a file");
						ex.printStackTrace();
						
					}
				}//end while 
				tmr.cancel();
				
				_dat.log("Exit DataWriter");
				
			
			}
			catch(Exception ex)
			{
				_dat.setcont(false);//close program fatal exception 
				System.out.println("Error output file");
				ex.printStackTrace();
			} 
	}//end of run 
	///
	///return inital file name stored in Datawriter filename
	private void _getfilename()
	{
		filename =""+LocalDateTime.now().getYear()+LocalDateTime.now().getMonth().getValue()+LocalDateTime.now().getDayOfMonth()+LocalDateTime.now().getHour()+LocalDateTime.now().getMinute();
		if(LocalDateTime.now().getHour() >12)
		{filename +="PM";}
		else 
		{filename += "AM";}
	}
	//check for filename changes (will change at hour =1 so 1am and 1pm 
	public boolean filenamechanged()
	{
		String temp = ""+LocalDateTime.now().getYear()+LocalDateTime.now().getMonth().getValue()+LocalDateTime.now().getDayOfMonth()+LocalDateTime.now().getHour()+LocalDateTime.now().getMinute();
		if(LocalDateTime.now().getHour() >12)
		{temp +="PM";}
		else 
		{temp += "AM";}
		_dat.log("new file: "+temp+" old: "+filename);
		if(temp.equals(filename))
			return false;
		else
			return true;
	}
	//call back for timer object locks both lists from reader/and writer threads while names are changed. 
	public void updatefilename()
	{
		
		try{
			//lock the lists 
			_dat._locks[0].lock();
			_dat._locks[1].lock();
			_dat.log("tried to lock both lists");
			if(_dat._locks[0].isHeldByCurrentThread() &&_dat._locks[1].isHeldByCurrentThread())
			{
				String oldname = filename;
				_dat.fq.queue.add(_dat.fileLocation+"time"+oldname+".txt");
				_dat.fq.queue.add(_dat.fileLocation+oldname+".txt");
				_getfilename();
				_dat.log("new filename "+filename);
				_dat.log("released locks");
			}
		}
		finally{
			if(_dat._locks[0].isHeldByCurrentThread())
				_dat._locks[0].unlock();
			if(_dat._locks[1].isHeldByCurrentThread())
				_dat._locks[1].unlock();
		}
		
		
	}
}
