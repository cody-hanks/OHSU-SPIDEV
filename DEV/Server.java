
import java.io.*;
import java.net.*;
import java.util.Date;
import java.time.LocalDateTime;
import java.lang.*;
import java.util.concurrent.locks.*;

public class Server extends Thread{
	
	
	static int portNumber = 5002;
	Data _dat;
	ReentrantLock[] _runlocklst;

	
	public Server(ReentrantLock[] runlocks,Data dat)
	{

		_runlocklst = runlocks;
		_dat = dat;
		
	}
	
	public void run()
	{
		ServerSocket serverSocket = null;
		try{
			while(_dat.getcont()){
			serverSocket = new ServerSocket(portNumber);
			Socket clientSocket =null;
			clientSocket = serverSocket.accept();
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
						this.exit();
					break;
					case "STRDT":
						startcollection();
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
		}//end try 
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}//end run 
	
	
	public void exit()
	{
		_dat.setcont(false);
	}
	
	
	public void startcollection()
	{
		Runnable CollectorThread = new DataCollector(_runlocklst,_dat);
		Runnable WriterThread = new DataWriter(_runlocklst,_dat);
		new Thread(CollectorThread).start();
		new Thread(WriterThread).start();
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
