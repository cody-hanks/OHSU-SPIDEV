


import java.io.*;
import java.net.*;
import java.util.Date;
import java.time.LocalDateTime;
import java.lang.*;
import java.util.concurrent.locks.*;
import java.util.zip.*;
import java.util.Enumeration;


public class RunUploader extends Thread{

	//static int portNumber = 5002;
	Data _dat;
	ReentrantLock[] _runlocklst;

	//constructor
	public RunUploader(ReentrantLock[] runlocks,Data dat)
	{
		_runlocklst = runlocks;
		_dat = dat;
		
	}
	
	public void run()
	{
		
		
		
		
		
	
		
		
		//only run while collecting 
		while(_dat.getcont())
		{
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//delay unimportant threads
			if(!_dat.fq.queue.isEmpty())
			{
				String filename = _dat.fq.queue.peek();
				try{
					File f = new File(filename);
					_dat.log("Create Zip:"+filename);
					
					String Zipfilename = filename.substring(0,filename.length()-4) + ".zip";
					_dat.log("Zip filename:"+Zipfilename);
					ZipOutputStream outzip = new ZipOutputStream(new FileOutputStream(Zipfilename));
					String[] basefilename = filename.split("/");
					ZipEntry zipent = new ZipEntry(basefilename[basefilename.length-1]);
					outzip.putNextEntry(zipent);
					InputStream IS = new FileInputStream(f);
					_dat.log("write zip file");
					int len; byte[] buf = new byte[8192];
					while((len = IS.read(buf))>0)
					{
						outzip.write(buf,0,len);
					}
					outzip.closeEntry();
					outzip.close();
					IS.close();
					
					_dat.log("Close zip file and delete file on drive");
					File zipfile = new File(Zipfilename);
					_dat.log("Try uploading file "+Zipfilename);
					_dat.gu.doUpload(zipfile, IUploadInterface.SUBFOLDER.Continious);
					//_dat.gu.doUpload(f, IUploadInterface.SUBFOLDER.Continious);
					_dat.log("File was uploaded: "+Zipfilename);
					_dat.fq.next();
					_dat.log("filename expired from queue "+Zipfilename);
					_dat.fq.savetodisk(_dat.qeuefile);
					Scavenge_stale_files(f);
					Scavenge_stale_files(zipfile);
					
					//TODO Need to change this to three days later
				}
				catch(Exception ex)
				{
					_dat.log("Error uploading "+filename);
				}
			}//end if 
		}//end while 
	}//end run 
	private void Scavenge_stale_files(File f)
	{
		f.delete();
	}
	
}
