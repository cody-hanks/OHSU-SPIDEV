

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive.Children;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

public class GoogleDriveUploader_Cody implements IUploadInterface{
	
	Data _dat;
	ReentrantLock[] _runlocklst;
	
	

	private Drive service = null;;
	// donte need this if sitedata folder is set. private  String parentfolderid = "";  //google drive id of the parent folder for the site data folders.....   "
	private static String parentfolderName = "";

	private  String sitefoldername = "";
	//private  String sitefolderid = "";

	//for now we will hard code the refresh token..

	private static String CLIENT_ID =  "";
			//"480665486644-9h2ve9e33131g0fp66nfen85nfcnpnp4.apps.googleusercontent.com";
	private static String CLIENT_SECRET = "";
	
	public static String ACCESS_REFRESH_TOKEN = "";
	private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	
			//"dn6H0c_JJ-FwgKWCsQQJ8DVD";

	//	private List<ParentReference> parList = null;

	Calendar lastDriveUploaderSvcBuild = Calendar.getInstance();  //10/27/14,  rebuild this periodically 


	private static com.google.api.services.drive.model.File root_sites_folder = null;   //root site data folder (holds all site folders). 

	private static String site_folder_id = null;  //the folder for the given (assigned site). 

	private static String site_config_folder_id = null;  //the folder for the given (assigned site). 

	private static String site_raw_data_folder_id = null;  //the folder for the given (assigned site). 

	private static String site_continious_data_folder_id = null;  //the folder for the given (assigned site). 

	private static String site_scripts_data_folder_id = null;  //the folder for the given (assigned site). 


	public static final int FILE_FORMAT_VERSION = 2;  //make final.

	public GoogleDriveUploader_Cody(ReentrantLock[] runlocks,Data dat)
	{
		_runlocklst = runlocks;
		_dat = dat;
		this.sitefoldername = _dat.siteid;
		this.parentfolderName = _dat.parent_folder;
		this.CLIENT_ID = _dat.clientid;
		this.ACCESS_REFRESH_TOKEN = _dat.token;
		this.CLIENT_SECRET = _dat.secret;
		
		buildDriveService();
		
		
	}




	private void buildDriveService()
	{
		
		lastDriveUploaderSvcBuild = Calendar.getInstance();

		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();

		GoogleCredential credential = new GoogleCredential.Builder()
		.setTransport(httpTransport)
		.setJsonFactory(jsonFactory)
		.setClientSecrets(CLIENT_ID, CLIENT_SECRET).build();

		//credential.setAccessToken(ACCESS_REFRESH_TOKEN);
		credential.setRefreshToken(ACCESS_REFRESH_TOKEN);  //this does not expire. ,,we will update this periodically from web service
		//Create a new authorized API client
		service = new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName("SleepStudy_Odroid").build();
		if(service != null)
		{	 
			root_sites_folder = getSiteDatav4FolderRef();	
			if(root_sites_folder == null)
				return;
		}

		initilizeFolderIDs();
	}



	private String getfolderID(SUBFOLDER sub)
	{
		switch(sub)
		{
		case Configs:
			return site_config_folder_id;	
		case Continious:
			return site_continious_data_folder_id;
		case Script:
			return site_scripts_data_folder_id;
		default:
			return null;			
		}
	}


	@Override
	public void doUpload(File f, SUBFOLDER subfolder) throws IOException {

		Calendar now = Calendar.getInstance();
		long deltaMin = (now.getTimeInMillis() - lastDriveUploaderSvcBuild.getTimeInMillis())/60000;
		if(deltaMin > 24*60)  //every 24 hours, rebuild the svc,  to make sure it does nto exprie the cookie..etc 
		{
			buildDriveService();
		}
		
		//Check file exists
		boolean found =false;
		List<com.google.api.services.drive.model.File> filelist = retrieveAllFiles();
		if(!filelist.isEmpty())
		{
			for(com.google.api.services.drive.model.File fn : filelist)
			{
				try{
				String gfileid = getfolderID(subfolder);
				String pfileid = fn.getParents().get(0).getId();
				String gfilenm = fn.getTitle();
				String pfilenm = f.getName();
				
				if(gfileid.equals(pfileid) && gfilenm.equals(pfilenm))
				{
					found =true;
				}}
				catch(Exception ex){}
			}
			
		}//end if empty list 
		if(!found)
		{
		
		

		String mimetype = "text/plain";
		if(f.getName().endsWith(".zip"))
		{
			mimetype = "application/zip";
		}
		else if(f.getName().endsWith(".xml"))
		{
			mimetype = "text/xml";
		}

		//Logger.appendLogfile("NodeMgr-DriveUploader","Uploading file: " + f.getName());
		//Insert a file  
		com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();

		body.setTitle(f.getName());
		body.setDescription(f.getName());
		body.setMimeType(mimetype);


		String subfolderid = getfolderID(subfolder);
		if(subfolderid != null)
		{

			//12/8/14, figure out the daily folderif needed. 
			// based on file name. 
			if(FILE_FORMAT_VERSION == 1)
			{

				if(subfolderid == site_continious_data_folder_id && f.getName().startsWith("c_"))
				{
					int start = f.getName().indexOf("__");
					String filedate = f.getName().substring(start+3, start + 11);


					String c_dailyfolderid = getRemoteFolderID(filedate,site_continious_data_folder_id);
					//Logger.appendLogfile("NodeMgr-DriveUploader","got valid c daily folder id: " + c_dailyfolderid);

					subfolderid = c_dailyfolderid;
				}	
				else if(subfolderid == site_scripts_data_folder_id)
				{
					int start = f.getName().indexOf("__");
					String filedate = f.getName().substring(start+3, start + 11);

					String script_dailyfolderid = getRemoteFolderID(filedate,site_scripts_data_folder_id);
					//Logger.appendLogfile("NodeMgr-DriveUploader","got valid daily script folder id: " + script_dailyfolderid);	
					subfolderid = script_dailyfolderid;
				}
			}
			else
			{
				String filedate = f.getName().substring(0, 6);
				if(subfolderid == site_continious_data_folder_id && f.getName().endsWith("_cont.zip"))
				{
					//int start = f.getName().indexOf("__");

					String c_dailyfolderid = getRemoteFolderID(filedate,site_continious_data_folder_id);
					//Logger.appendLogfile("NodeMgr-DriveUploader","got valid c daily folder id: " + c_dailyfolderid);

					subfolderid = c_dailyfolderid;
				}	
				else if(subfolderid == site_scripts_data_folder_id)
				{
					//int start = f.getName().indexOf("__");
					//String filedate = f.getName().substring(start+3, start + 11);

					String script_dailyfolderid = getRemoteFolderID(filedate,site_scripts_data_folder_id);
					//Logger.appendLogfile("NodeMgr-DriveUploader","got valid daily script folder id: " + script_dailyfolderid);	
					subfolderid = script_dailyfolderid;
				}
			}
			
			




			ArrayList<ParentReference> parList = new ArrayList<ParentReference>();
			ParentReference pr = new ParentReference();
			pr.setId(subfolderid);
			parList.add(pr);

			body.setParents(parList);



			FileContent mediaContent = new FileContent(mimetype, f);
			com.google.api.services.drive.model.File file = service.files().insert(body, mediaContent).execute();
			
			//Logger.appendLogfile("NodeMgr-DriveUploader", "Success -- File ID: " + file.getId());
		}
		else
		{
			//Logger.appendLogfile("NodeMgr-DriveUploader", "Cant upload file, null folder ID");
		}
		}
	}



	public void initilizeFolderIDs()
	{
		if(setupSiteFolderID())  //site
		{
			if(setupConfigDataFolderID())  //config 
			{
				if(setupRawDataFolderID())//raw data
				{
					if(setupContiniousDataFolderID())   //cont data
					{
						setupScriptDataFolderID();   //script data. 
					}
				}
			}
		}
	}


	/***
	 * 
	 * @param filename
	 * @return
	 */
	private boolean setupSiteFolderID()
	{
		site_folder_id = null; 
		System.out.println("Initilizing site folder id: " + sitefoldername);
		FileList files = null;
		Drive.Files.List request = null;	
		try{
			request = service.files().list();
			request.setQ("title = '" + sitefoldername + "'"+" and trashed = false");   //put out  a request for the folder name (in general). 
			files = request.execute();
		} 
		catch (Exception ex)
		{
			
			ex.printStackTrace();
			return false;
		}
			
		do
		{
			try
			{   // now iter through each returned element(could be more than one), and verify that the parent folder is our root sites folder.
				Iterator<com.google.api.services.drive.model.File> listiter = files.getItems().iterator();
				while(listiter.hasNext())
				{
					com.google.api.services.drive.model.File f = listiter.next();

					//verify parent id  = matches the id of the root folder id. 
					List<ParentReference> parlist = f.getParents();
					Iterator<ParentReference> pariter = parlist.iterator();
					while(pariter.hasNext())
					{

						ParentReference par = pariter.next();
						if(par.getId().compareTo(root_sites_folder.getId()) == 0)
						{
							site_folder_id = f.getId();
							System.out.println("found site folder id:" + site_folder_id); 
							return true;
						}
					}	
				}		
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}  while (request.getPageToken() != null &&
				request.getPageToken().length() > 0);


		//if you get here the folder does not exist,  create it now. 

		//Insert create site folder 
		try {
			System.out.println("Site folder does not exist, creating one now.");
			com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();

			body.setTitle(sitefoldername);
			body.setDescription(sitefoldername);
			body.setMimeType("application/vnd.google-apps.folder");

			ArrayList<ParentReference> parList = new ArrayList<ParentReference>();
			ParentReference pr = new ParentReference();

			pr.setId(root_sites_folder.getId());  //set parent id(site folder)
			parList.add(pr);
			body.setParents(parList);

			com.google.api.services.drive.model.File site_folder = service.files().insert(body).execute();

			System.out.println("Created new site sub folder for: "+ sitefoldername +" : id = " + site_folder.getId());
			site_folder_id = site_folder.getId();	
			return true;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}

	}






	/***
	 * 
	 * @param filename
	 * @return
	 */
	private boolean setupConfigDataFolderID()
	{
		site_config_folder_id = null;
		System.out.println("Initilizing configs folder id");
		ChildList children = null;
		Children.List request = null;	
		try{
			request = service.children().list(site_folder_id);
			request.setQ("title = '" + "configs" + "'"+" and trashed = false");   //put out  a request for the folder name (in general). 
			children = request.execute();
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}

		do
		{
			try
			{                            
				for (ChildReference child : children.getItems()) {
					site_config_folder_id = child.getId();
					System.out.println("site_config_folder_id = " +site_config_folder_id);
					return true;
				}

			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}  while (request.getPageToken() != null &&
				request.getPageToken().length() > 0);


		try {
			//Insert create  configs sub folder
			System.out.println("Configs folder does not exist, creating one now.");
			com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();

			body.setTitle("configs");
			body.setDescription("configs");
			body.setMimeType("application/vnd.google-apps.folder");

			ArrayList<ParentReference> parList = new ArrayList<ParentReference>();
			ParentReference pr = new ParentReference();		
			pr.setId(site_folder_id);  
			parList.add(pr);
			body.setParents(parList);

			com.google.api.services.drive.model.File cfgfolder = service.files().insert(body).execute();		
			site_config_folder_id = cfgfolder.getId();
			System.out.println("Created new configs sub folder created, id = " + site_config_folder_id);
			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}



	/***
	 * 
	 * @param filename
	 * @return
	 */
	private boolean setupRawDataFolderID()
	{
		System.out.println("Initilizing raw data folder id");
		site_raw_data_folder_id = null;
		ChildList children = null;
		Children.List request = null;	
		try{
			request = service.children().list(site_folder_id);
			request.setQ("title = '" + "rawdata" + "'"+" and trashed = false");   //put out  a request for the folder name (in general). 
			children = request.execute();
		} 
		catch (Exception ex)
		{
			return false;
		}

		do
		{
			try
			{                            
				for (ChildReference child : children.getItems()) {
					site_raw_data_folder_id = child.getId();
					System.out.println("site_raw_data_folder_id = " +site_raw_data_folder_id);
					return true;
				}
			}
			catch (Exception ex)
			{
					ex.printStackTrace();
			}
		}  while (request.getPageToken() != null &&
				request.getPageToken().length() > 0);



		try {

			System.out.println("RawData folder does not exist, creating one now.");
			com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();

			body.setTitle("rawdata");
			body.setDescription("rawdata");
			body.setMimeType("application/vnd.google-apps.folder");

			ArrayList<ParentReference> parList = new ArrayList<ParentReference>();
			ParentReference pr = new ParentReference();		
			pr.setId(site_folder_id);  
			parList.add(pr);
			body.setParents(parList);

			com.google.api.services.drive.model.File rawfolder = service.files().insert(body).execute();
			site_raw_data_folder_id = rawfolder.getId();
			System.out.println("New raw data sub folder created, id = " +  rawfolder.getId());
			return true;
		}catch(Exception ex)
		{

		}
		return false;
	}

	/***
	 * 
	 * @param filename
	 * @return
	 */
	private boolean setupContiniousDataFolderID()
	{
		System.out.println("Initilizing continious folder id");
		site_continious_data_folder_id = null;
		ChildList children = null;
		Children.List request = null;	
		try{
			request = service.children().list(site_raw_data_folder_id);
			request.setQ("title = '" + "continious" + "'"+" and trashed = false");   //put out  a request for the folder name (in general). 
			children = request.execute();
		} 
		catch (Exception ex)
		{
			return false;
		}

		do
		{
			try
			{                            
				for (ChildReference child : children.getItems()) {
					site_continious_data_folder_id = child.getId();
					System.out.println("site_continious_data_folder_id = " +site_continious_data_folder_id);
					return true;
				}
			}
			catch (Exception ex)
			{

			}
		}  while (request.getPageToken() != null &&
				request.getPageToken().length() > 0);



		try {

			System.out.println("Continious folder does not exist, creating one now.");
			com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();


			body.setTitle("continious");
			body.setDescription("continious");
			body.setMimeType("application/vnd.google-apps.folder");

			ArrayList<ParentReference> parList = new ArrayList<ParentReference>();
			ParentReference pr = new ParentReference();		
			pr.setId(site_raw_data_folder_id);  
			parList.add(pr);
			body.setParents(parList);

			com.google.api.services.drive.model.File contfolder = service.files().insert(body).execute();		
			site_continious_data_folder_id = contfolder.getId();
			System.out.println("New - continious sub folder created, id = " +  contfolder.getId());
			return true;
		}catch(Exception ex)
		{

		}
		return false;
	}



	/***
	 * 
	 * @param filename
	 * @return
	 */
	private boolean setupScriptDataFolderID()
	{

		site_scripts_data_folder_id = null;
		ChildList children = null;
		Children.List request = null;	
		try{
			request = service.children().list(site_raw_data_folder_id);
			request.setQ("title = '" + "script" + "'" +" and trashed = false");   //do not include trashed files..
			children = request.execute();
		} 
		catch (Exception ex)
		{
			return false;
		}

		do
		{
			try
			{           

				for (ChildReference child : children.getItems()) {

					site_scripts_data_folder_id = child.getId();
					System.out.println("site_scripts_data_folder_id = " +site_scripts_data_folder_id);
					return true;
				}

			}
			catch (Exception ex)
			{

			}
		}  while (request.getPageToken() != null &&
				request.getPageToken().length() > 0);



		try {

			System.out.println("Script folder does not exist, creating one now.");
			com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();


			body.setTitle("script");
			body.setDescription("script");
			body.setMimeType("application/vnd.google-apps.folder");

			ArrayList<ParentReference> parList = new ArrayList<ParentReference>();
			ParentReference pr = new ParentReference();		
			pr.setId(site_raw_data_folder_id);  
			parList.add(pr);
			body.setParents(parList);

			com.google.api.services.drive.model.File contfolder = service.files().insert(body).execute();		
			site_scripts_data_folder_id = contfolder.getId();
			System.out.println("New - script sub folder created, id = " +  contfolder.getId());
			return true;
		}catch(Exception ex)
		{

		}
		return false;
	}





	/***
	 * 
	 * @return a reference to the SITE_DATA_v4 folder
	 */
	private com.google.api.services.drive.model.File getSiteDatav4FolderRef()
	{
		FileList files = null;
		Drive.Files.List request = null;
		try{
			request = service.files().list();
			request.setQ("title = '" + parentfolderName + "'");
			files = request.execute();
		} 
		catch (Exception ex)
		{
			return null;
		}

		do
		{
			try
			{

				Iterator<com.google.api.services.drive.model.File> listiter = files.getItems().iterator();
				while(listiter.hasNext())
				{
					com.google.api.services.drive.model.File f = listiter.next();
					System.out.println(parentfolderName + " folder ID:" + f.getId()); 
				}

				if (files.getItems().size() == 1)
					return files.getItems().get(0);
				else
				{
					return null;
				}
			}
			catch (Exception ex)
			{
			}
		}  while (request.getPageToken() != null || request.getPageToken().length() <= 0);
		return null;
	}






	/***
	 * return valid folder id if found,  , or if succesful creation,  null otherwise
	 * @param filename
	 * @return
	 */
	private String getRemoteFolderID(String foldername, String parentfolderid)
	{
		System.out.println("Fetching Remote Folder ID for: "  + foldername);
		//site_continious_data_folder_id = null;
		ChildList children = null;
		Children.List request = null;	
		try{
			request = service.children().list(parentfolderid);
			request.setQ("title = '" + foldername + "'"+" and trashed = false");   //put out  a request for the folder name (in general). 
			children = request.execute();
		} 
		catch (Exception ex)
		{
			return null;
		}

		do
		{
			try
			{                            
				for (ChildReference child : children.getItems()) {			
					String folder_id = child.getId();		
					System.out.println("folder: "+ foldername + " already exists at id: " +folder_id + " will not create");
					return folder_id;
				}
			}
			catch (Exception ex)
			{

			}
		}  while (request.getPageToken() != null &&
				request.getPageToken().length() > 0);



		try {

			System.out.println(foldername + ": folder does not exist, creating one now.");
			com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();


			body.setTitle(foldername);
			body.setDescription(foldername);
			body.setMimeType("application/vnd.google-apps.folder");

			ArrayList<ParentReference> parList = new ArrayList<ParentReference>();
			ParentReference pr = new ParentReference();		
			pr.setId(parentfolderid);  
			parList.add(pr);
			body.setParents(parList);

			com.google.api.services.drive.model.File contfolder = service.files().insert(body).execute();		

			String folder_id = contfolder.getId();

			System.out.println("folder: "+ foldername + " CREATED at id: " +folder_id);
			return folder_id;
		}catch(Exception ex)
		{

		}
		return null;
	}


	public static void generateNewAccessAndRefreshToken() throws IOException
	{
		
		
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();


		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
		.setAccessType("offline")
		.setApprovalPrompt("force").build();

			
	
		//flow.createAndStoreCredential(response, userId)
		
		String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
		
		
		
		System.out.println("Please open the following URL in your browser then type the authorization code:");
		System.out.println("  " + url);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String code = br.readLine();

		
		GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
		
		String refreshtok = response.getRefreshToken();
		System.out.println("refresh tok: " + refreshtok);
		
		
		//GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);

	}


	private List<com.google.api.services.drive.model.File> retrieveAllFiles() throws IOException
	{
		Calendar now = Calendar.getInstance();
		long deltaMin = (now.getTimeInMillis() - lastDriveUploaderSvcBuild.getTimeInMillis())/60000;
		if(deltaMin > 24*60)  //every 24 hours, rebuild the svc,  to make sure it does nto exprie the cookie..etc 
		{
			buildDriveService();
		}
		
		
		
		List<com.google.api.services.drive.model.File> result = new ArrayList<com.google.api.services.drive.model.File>();
		com.google.api.services.drive.Drive.Files.List request = service .files().list().setQ("trashed = false");
		do{
		try{
		com.google.api.services.drive.model.FileList files = request.execute();
		result.addAll(files.getItems());
		request.setPageToken(files.getNextPageToken());
		
		}catch(Exception e)
		{
			_dat.log("error "+e.getMessage());
			e.printStackTrace();
		}
		
		}while(request.getPageToken() != null && request.getPageToken().length() >0);
		return result;
		
	}//end of list 

}
