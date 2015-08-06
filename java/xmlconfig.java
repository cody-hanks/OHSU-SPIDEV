import java.io.*;
import java.net.*;
import java.util.Date;
import java.lang.*;
import java.util.concurrent.locks.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/*
 * Class for parsing xml config file 
 * file location should be in the same folder 
 * root element, settings 
 * 
 * 
 * 
*/
public class xmlconfig
{
	Data _dat;
	String _filename;
	public xmlconfig(String filename,Data dat)
	{
		_dat = dat;
		_filename = filename;	
		
	}
	public void parsexmlconfig()
	{
		//read the configuration file. 
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try{
			//Build the XML reader 
			DocumentBuilder db = dbf.newDocumentBuilder();
			_dat.log("parsing");
			Document doc = db.parse(_filename);
			_dat.log("parsed config file");
			doc.getDocumentElement().normalize();
			
			//get list of channel objects 
			NodeList nList = doc.getElementsByTagName("channel");
			
			_dat.log("reading channels");
			
			
			//add sample channels to the list 
			String channel;
			for(int tmp =0;tmp<nList.getLength();tmp++)
			{
				//System.out.println(((Element)nList.item(tmp)).getElementsByTagName("number").item(0).getTextContent());
				channel = ((Element)nList.item(tmp)).getElementsByTagName("number").item(0).getTextContent();
				if(Integer.parseInt(channel) <16)
				{
					_dat.chList[_dat.chCount] = Integer.parseInt(channel);
					_dat.chCount++;
					_dat.log("Configureation added Channel: "+_dat.chList[_dat.chCount-1]);
				}
			}
			//get auto start settings 
			_dat.log("channel count: "+_dat.chCount);
			String strAutoStart = doc.getElementsByTagName("autostart").item(0).getTextContent();
			if(strAutoStart.equals("1"))
				_dat.autostart = true;
			_dat.log("AutoStart config: "+strAutoStart);
			//get sample frequency
			String frequency = doc.getElementsByTagName("frequency").item(0).getTextContent();
			_dat.frequency = Integer.parseInt(frequency);
			_dat.log("frequency set to: "+_dat.frequency);
			
			
			
			
			
		}
		catch(Exception ex)
		{
				_dat.log("error reading: "+_filename+" configureation file");
		}
		
	}
	
	
	
	
	
	
}
