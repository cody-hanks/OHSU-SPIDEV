

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.LinkedList;
import java.lang.*;
import java.util.concurrent.locks.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;




public class OHSUDEV
{
	
	public static void main(String[] args) 
	{

//		
//		while(fqin.queue.peek() != null)
//			System.out.println(fqin.next());
		
//		/*try {
//			GoogleDriveUploader_Cody.generateNewAccessAndRefreshToken();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}*/	

		
		// now setup for arrays and objects 
		
		ReentrantLock[] locks = new ReentrantLock[6];
		locks[0] = new ReentrantLock();//locks for list acces 
		locks[1] = new ReentrantLock();//locks for list acces
		locks[2] = new ReentrantLock();//lock for continue boolean
		locks[3] = new ReentrantLock();//Lock for collection 
		
		//setup a run stop variable 
		Data dat = new Data(locks);
		
		//parse the configuration file 
		xmlconfig configparse = new xmlconfig(dat.configfile,dat);
		configparse.parsexmlconfig();
			
		//create a gooogle drive object and copy the folder into it.
		
		GoogleDriveUploader_Cody gu = new GoogleDriveUploader_Cody(locks,dat);
		
		
		
		
		File f = new File(dat.configfile);
		try {
			gu.doUpload(f, IUploadInterface.SUBFOLDER.Configs);
		} catch (IOException e) {
			dat.log("error uploading config file");
			e.printStackTrace();
		}
		//set dat google uploader 
		dat.gu = gu;
		
		
		
		
		//set dat file queue 
		//file queue stored to disk for later access 
		//TODO dont think this is working not sure why its failing other than possibly empty queue
		dat.fq = new FileQueue(dat.qeuefile);
		
		//start thread for uploading 
		Runnable UploadThread = new RunUploader(locks,dat);
		new Thread(UploadThread).start();
		
		
		//Starting server 
		System.out.println("Starting OHSU Sleep server");
		
		
		//create the runnable thread objects
		Runnable ServerThread = new Server(locks,dat);
		
		//check for auto run 
		if(dat.autostart)
		{
			(new Server(locks,dat)).startcollection();
		}
		
		//start the server thread
		new Thread(ServerThread).start();
		
	
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
