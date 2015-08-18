
import java.io.*;
import java.net.*;
import java.util.Date;
import java.time.LocalDateTime;
import java.lang.*;
import java.util.concurrent.locks.*;

/*
 * Server class is used as a command reciver to start or stop sampling also as a test 
 * LED may be turned on or off using this protocol
 * 
 * 
 * 
 */
public class Server extends Thread{
	
	
	//static int portNumber = 5002;
	Data _dat;
	ReentrantLock[] _runlocklst;

	//constructor
	public Server(ReentrantLock[] runlocks,Data dat)
	{

		_runlocklst = runlocks;
		_dat = dat;
		
	}
	//Threaded run server to recive commands this is the command protocol for 
	public void run()
	{
		_dat.log_warning("starting server");
		ServerSocket serverSocket = null;
		try{
			while(_dat.getcont()){
			serverSocket = new ServerSocket(_dat.portNumber);
			Socket clientSocket =null;
			clientSocket = serverSocket.accept();
			_dat.log_info("Connection accepted");
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String inputline;
			
			while( _dat.getcont() && (inputline = in.readLine())!= null)
			{
				if(inputline.equals("END"))
					break;
				switch(inputline)
				{
					case "function":
						out.println("");
					break;
					case "Exit":
						_dat.log("exiting");
						this.exit();
					break;
					case "STRDT":
						startcollection();
						_dat.log("Started Data collection");
					break;
					case "STRNET":
						startnetcollection();
						_dat.log("Started network collection port:"+_dat.dataportNumber);
					break;
					case "STP":
						endcollection();
						_dat.log("Stopped collection");
					break;
					case "LEDON":
						this.setledon();
					break;
					case "LEDOFF":
						this.setledoff();
					break;
					default:
						out.println("RCV:"+ inputline);
				}//end switch 
			}//end while 
			out.close();
			in.close();
			clientSocket.close();
			serverSocket.close();
		  }
		  _dat.log_warning("Server Exit");
		}//end try 
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}//end run 
	
	//Exit program close all running threads 
	public void exit()
	{
		_dat.setcont(false);
	}
	
	//start collection to a file
	public void startcollection()
	{
		try{
		endcollection();
		Thread.sleep(1000);
		_dat.setcoll(true);
		Runnable CollectorThread = new DataCollector(_runlocklst,_dat,false);
		Runnable WriterThread = new DataWriter(_runlocklst,_dat);
		new Thread(CollectorThread).start();
		new Thread(WriterThread).start();
		}
		catch(Exception ex)
		{_dat.log_exception("error starting collection");}
	}
	public void startnetcollection()
	{
		try{
		endcollection();
		Thread.sleep(1000);
		_dat.setcoll(true);
		Runnable CollectorThread = new DataCollector(_runlocklst,_dat,true);
		new Thread(CollectorThread).start();
		}
		catch(Exception ex)
		{_dat.log_exception("error starting net collection ");}
	}
		
	public void endcollection()
	{
		_dat.setcoll(false);
	}
	
	public void setledoff()
	{
		try{
			_dat.CSPI.setgpio(_dat.fd,0);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	public void setledon()
	{
		try{
			_dat.CSPI.setgpio(_dat.fd,1);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
}//end class
