

import java.io.File;
import java.io.IOException;


public interface IUploadInterface {
	//void doUpload(File f) throws IOException;
	void doUpload(File f, SUBFOLDER subfolder) throws IOException;
	

	public static enum SUBFOLDER {
		Configs,
		Continious,
		Script
	}
}
