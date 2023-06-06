


import org.xml.sax.SAXException;


import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, SAXException {

        checkStartUp();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri( URI.create("https://pineappleea.github.io/"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        String body = response.body();

        body = body.substring(body.indexOf("<!--link-goes-here-->")+("<!--link-goes-here-->".length()+1));
        body = body.substring(0,body.indexOf("</div>"));
        String latestVersion = body.split("<br>")[0];
        System.out.println(latestVersion);
        latestVersion = latestVersion.substring(latestVersion.indexOf(">")+1,latestVersion.lastIndexOf("<"));
       String latestVerstionLink =body.substring(body.indexOf("href="),body.indexOf(">"));;
        latestVerstionLink = latestVerstionLink.split("=")[1];
        System.out.println(latestVersion);
        System.out.println(latestVerstionLink);
        StringBuilder sb = new StringBuilder();
//        sb.append(latestVersion.split(" ")[0]);
//        sb.append("-");
        sb.append(latestVersion.split(" ")[1]);
        sb.append("-");
        sb.append(latestVersion.split(" ")[2]);
        System.out.println(sb.toString());

        if(!isOutdated(sb.toString())){
            if(launchYuzu()){
                System.out.println("Launching yuzu.exe successful");
                return;
            }else{
                System.out.println("Re-downloading yuzu-ea");
            }

        }

        String downloadLink = "https://github.com/pineappleEA/pineapple-src/releases/download/"+sb.toString().trim()+"/Windows-Yuzu-"+sb.toString().trim()+".zip";
        System.out.println(downloadLink);

        File file = new File("yuzu-update.zip");
        if(file.exists()){
            file.delete();
            file.createNewFile();
        }else
        {
            file.createNewFile();
        }

        downloadUsingStream(downloadLink,file.getAbsolutePath());
        System.out.println("Download completed");
        updatedConfig(sb.toString());


        System.out.println("Extracting "+file.getName());
        String destDir = "./Yuzu";
        File destDirFile = new File(destDir);
        System.out.println("Deleting old files "+destDirFile.getAbsolutePath() );
        deleteOldFiles(destDirFile);

        unzip(file.getAbsolutePath(), destDir);
        launchYuzu();
    }

    private static void checkStartUp() throws IOException {
        File config = new File("config");
        if(!config.exists()){
            config.createNewFile();
        }
    }


    private static void unzip(String zipFile,String extractFolder)
    {
        try
        {
            int BUFFER = 2048;
            File file = new File(zipFile);

            ZipFile zip = new ZipFile(file);
            String newPath = extractFolder;

            new File(newPath).mkdir();
            Enumeration zipFileEntries = zip.entries();

            // Process each entry
            while (zipFileEntries.hasMoreElements())
            {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();

                File destFile = new File(newPath, currentEntry);
                //destFile = new File(newPath, destFile.getName());
                File destinationParent = destFile.getParentFile();

                // create the parent directory structure if needed
                destinationParent.mkdirs();

                if (!entry.isDirectory())
                {
                    BufferedInputStream is = new BufferedInputStream(zip
                            .getInputStream(entry));
                    int currentByte;
                    // establish buffer for writing file
                    byte data[] = new byte[BUFFER];

                    // write the current file to disk
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos,
                            BUFFER);

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }


            }
            System.out.println("Extraction complete");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Failed to Unzip :(");
        }

    }

    private static void deleteOldFiles(File destDirFile){
        deleteDirectory(destDirFile);
        destDirFile.delete();
    }
    public static void deleteDirectory(File file)
    {
        // store all the paths of files and folders present
        // inside directory
        if(!file.exists())return;
        for (File subfile : file.listFiles()) {

            // if it is a subfolder,e.g Rohan and Ritik,
            //  recursively call function to empty subfolder
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }

            // delete files and empty subfolders
            subfile.delete();
        }
    }

    private static boolean launchYuzu() throws IOException {

        String currentDir = System.getProperty("user.dir");
        System.out.println("Current dir using System:" + currentDir);
        System.out.println("Launching Yuzu");
//        Runtime.getRuntime().exec("yuzu.exe",
//                null, new File(currentDir+"/Yuzu/yuzu-windows-msvc-early-access/"));
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(new File(currentDir + "/Yuzu/yuzu-windows-msvc-early-access/yuzu.exe"));
        }catch(java.lang.IllegalArgumentException e){
            e.printStackTrace();
            System.out.println("Failed to Launch yuzu.exe :"+e.getMessage());
            return false;
        }
        return true;
    }
    private static void downloadUsingStream(String urlStr, String file) throws IOException{
        URL url = new URL(urlStr);
        URLConnection urlConnection = url.openConnection();
        System.out.println("Date= "+new Date(urlConnection.getLastModified()));
        System.out.println("Size= "+urlConnection.getContentLength());
        BufferedInputStream bis = new BufferedInputStream(url.openStream());

        FileOutputStream fis = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int count=0,countTracker=0, percentOld=0,percentNew=1;


        while((count = bis.read(buffer,0,1024)) != -1)
        {
            countTracker += count;
            percentNew =  ((int) (countTracker /(float)urlConnection.getContentLength()* 100));
            if(percentOld != percentNew) {
                percentOld = percentNew ;
                System.out.println("Downloding progress " + percentOld +"%");

            }
            fis.write(buffer, 0, count);

        }
        fis.close();
        bis.close();

    }

    private static void downloadUsingNIO(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    private static void updatedConfig(String toWrite) throws IOException {
        Path fileName
                = Path.of("config");
        Files.writeString(fileName, toWrite,
                StandardCharsets.UTF_8);
    }
   private static boolean isOutdated(String version) throws IOException {


       Path fileName
               = Path.of("config");
       String str = Files.readString(fileName);
       System.out.println(str);
       if (str.trim().equals(version)) {
           return false;

       }
       return true;
    }
}