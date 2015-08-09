/**
 * 
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
/**
 * 
 *
 */

public class FileQueue implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Queue<String> queue =null;
	
	
	public FileQueue(String filename)
	{
		if(!this.getfromdisk(filename, this))
			queue = new LinkedList<String>();
		if(queue ==null)
			queue = new LinkedList<String>();
		this.savetodisk(filename);
	}
	
	public String next()
	{
		if(!queue.isEmpty())
			return queue.remove();
		return null;
	}
	
	public boolean savetodisk(String filename) 
	{
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(this);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		finally
		{
			try{
			out.flush();
			out.close();
			}catch(Exception ex)
			{ex.printStackTrace();}
		}
		return true;
	}
	public boolean getfromdisk(String filename,FileQueue fqobj)
	{
		ObjectInputStream in = null;
		
		try {
			in = new ObjectInputStream(new FileInputStream(filename));
			fqobj = (FileQueue) in.readObject();
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		finally
		{
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;

	}
	
}
