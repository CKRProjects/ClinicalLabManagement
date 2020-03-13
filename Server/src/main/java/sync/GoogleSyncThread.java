package sync;

import Utils.FileSystemStorageUtil;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;

import java.io.*;
import java.net.URL;
import java.security.GeneralSecurityException;

public class GoogleSyncThread implements Runnable {

  private static final String APPLICATION_NAME = "";
  private Drive driveService = null;

  public GoogleSyncThread(String accessToken, String refreshToken) throws GeneralSecurityException, IOException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    Credential cred = new Credential(BearerToken.authorizationHeaderAccessMethod());
    cred.setAccessToken(accessToken);
    cred.setRefreshToken(refreshToken);
    driveService = new Drive.Builder(HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(), cred)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  @Override
  public void run() {

    try {
      while (!SyncStatus.getSyncStatus().equals("STOP")) {
        SyncStatus.setSyncStatus("RUNNING");

        String reportsFolderPath = getClass().getClassLoader().getResource("Reports").getFile();
        File reportsFolder = new File(reportsFolderPath);

        long lastModifiedTime = reportsFolder.lastModified();
        File reportsLastSynced = new File(this.getClass().getClassLoader().getResource("Reports" + File.separator + "lastSynced").getFile());
        FileReader fr = new FileReader(reportsLastSynced);
        BufferedReader br = new BufferedReader(fr);
        String value = br.readLine();
        long lastSyncedTime = Long.valueOf(value!=null?value:"0");

        if (lastModifiedTime != lastSyncedTime) {
          syncToGdrive(reportsFolder, "Reports");
          FileWriter fw = new FileWriter(reportsLastSynced);
          fw.write(String.valueOf(lastModifiedTime));
          fw.close();
        }

        String testsFolderPath = this.getClass().getClassLoader().getResource("Tests").getFile();
        File testsFolder = new File(testsFolderPath);

        lastModifiedTime = reportsFolder.lastModified();

        File testsLastSynced = new File(this.getClass().getClassLoader().getResource("Tests" + File.separator + "lastSynced").getFile());
        fr = new FileReader(reportsLastSynced);
        br = new BufferedReader(fr);
        value = br.readLine();
        lastSyncedTime = Long.valueOf(value!=null?value:"0");

        if (lastModifiedTime != lastSyncedTime) {
          syncToGdrive(testsFolder, "Tests");
          FileWriter fw = new FileWriter(testsLastSynced);
          fw.write(String.valueOf(lastModifiedTime));
          fw.close();
        }

        SyncStatus.setSyncStatus("DONE");
        wait(30 * 60 * 1000);
      }

    } catch (Exception e) {
      SyncStatus.setSyncStatus("ERROR");
      e.printStackTrace();
    }
  }

  private void syncToGdrive(File syncFolder, String folderName) throws IOException {
    File[] files = syncFolder.listFiles();

    for (File f : files) {
      com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
      fileMetadata.setName("ClinicFiles/" + folderName + "/" + f.getName());
      FileContent mediaContent = new FileContent("image/jpeg", f);
      com.google.api.services.drive.model.File file = driveService.files().create(fileMetadata, mediaContent)
          .setFields("id")
          .execute();
    }
  }

}
