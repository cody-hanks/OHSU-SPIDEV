package org.embedrf.core.test;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.embedrf.core.data.upload.GoogleDriveUploader_Cody;
import org.embedrf.core.data.upload.DriveUploader;
import org.embedrf.core.data.upload.GoogleDriveUploader;
import org.embedrf.core.data.upload.IUploadInterface.SUBFOLDER;

public class TestUploader {



	//private static String CLIENT_ID =  "480665486644-9h2ve9e33131g0fp66nfen85nfcnpnp4.apps.googleusercontent.com";
	//private static String CLIENT_SECRET = "dn6H0c_JJ-FwgKWCsQQJ8DVD";

	private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

	
	// these are for codys ,  jac bme..
	
	private static String CLIENT_ID =  "240795554120-i4baqa7giopgfh8imann3ikeal1tcall.apps.googleusercontent.com";
	//"480665486644-9h2ve9e33131g0fp66nfen85nfcnpnp4.apps.googleusercontent.com";
private static String CLIENT_SECRET = "GmVpPttxeVoQFKTbwLw2nCeX";
	public static void main(String[] args) {
		
		try {
			
			//GoogleDriveUploader du = new GoogleDriveUploader("Nick_Office_Desktop");
			
			//generateNewAccessAndRefreshToken();
			
			
			GoogleDriveUploader_Cody du = new GoogleDriveUploader_Cody("Cody_Test1");
			
			du.initilizeFolderIDs();
			
			
			
		//	String filename = "TestA.txt";
			
			//File f = new File ("TestA.txt");
			
		//	du.doUpload(new java.io.File("C:\\Git_Local\\erfv4\\uploadtest\\" + filename), SUBFOLDER.Configs);
			
			
			
			
	/*		HttpTransport httpTransport = new NetHttpTransport();
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
			
			
			GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);*/

//			GoogleCredential credential = new GoogleCredential.Builder()
//            .setTransport(httpTransport)
//            .setJsonFactory(jsonFactory)
//            .setClientSecrets(CLIENT_ID, CLIENT_SECRET).build();
			
                 // credential.setAccessToken("1/oiNHmbGUWn1tbCtTQCwk3DMIxMcabgGKT6oJJ01xTb4");
			
           //  credential.setRefreshToken("1/oiNHmbGUWn1tbCtTQCwk3DMIxMcabgGKT6oJJ01xTb4");  //this does not expire. 
                 
                  
//			//Create a new authorized API client
//			Drive service = new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName("erfcollectorv4").build();
//
//			
//			String filename = "TestC.txt";
//			
//			//Insert a file  
//			File body = new File();
//			body.setTitle(filename);
//			body.setDescription(filename);
//			body.setMimeType("text/plain");
//
//			java.io.File fileContent = new java.io.File("C:\\A_DEV\\UploadTest\\" + filename);
//			FileContent mediaContent = new FileContent("text/plain", fileContent);
//
//			File file = service.files().insert(body, mediaContent).execute();
//			System.out.println("File ID: " + file.getId());

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	
	
	private static void generateNewAccessAndRefreshToken() throws IOException
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
	
	
	
	
	 /**
	   * Insert new file.
	   *
	   * @param service Drive API service instance.
	   * @param title Title of the file to insert, including the extension.
	   * @param description Description of the file to insert.
	   * @param parentId Optional parent folder's ID.
	   * @param mimeType MIME type of the file to insert.
	   * @param filename Filename of the file to insert.
	   * @return Inserted file metadata if successful, {@code null} otherwise.
	   */
	  private static File insertFile(Drive service, String title, String description,
	      String parentId, String mimeType, String filename) {
	    // File's metadata.
	    File body = new File();
	    body.setTitle(title);
	    body.setDescription(description);
	    body.setMimeType(mimeType);

	    // Set the parent folder.
	    if (parentId != null && parentId.length() > 0) {
	      body.setParents(
	          Arrays.asList(new ParentReference().setId(parentId)));
	    }

	    // File's content.
	    java.io.File fileContent = new java.io.File(filename);
	    FileContent mediaContent = new FileContent(mimeType, fileContent);
	    try {
	      File file = service.files().insert(body, mediaContent).execute();

	      // Uncomment the following line to print the File ID.
	      // System.out.println("File ID: " + file.getId());

	      return file;
	    } catch (IOException e) {
	      System.out.println("An error occured: " + e);
	      return null;
	    }
	  }

	  
	  
	
}
