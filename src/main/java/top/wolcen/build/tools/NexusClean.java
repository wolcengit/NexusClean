/**
 *
 */
package top.wolcen.build.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author Wolcen
 *
 */
public class NexusClean {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if(args == null|| args.length == 0){
            help();
            return;
        }
        File folder = new File(args[0]);
        if(folder == null || !folder.exists()){
            help();
            return;
        }
        processFolder(folder);
    }

    public static void help() {
        System.out.println("clean nexus SNAPSHOT,only keep lastupdate ");
        System.out.println("using: ");
        System.out.println("java -jar NexusClean.jar  <storage-snapshots-path>");
        System.out.println("Sample: ");
        System.out.println("java -jar NexusClean.jar  C:\\nexus-2.7.0-04-bundle\\sonatype-work\\nexus\\storage\\snapshots");
    }

    protected static void processFolder(File folder) {
        System.out.println("Checking "+folder.getAbsolutePath());
        String name = folder.getName();
        if(name.endsWith("-SNAPSHOT")){
            processSNAPSHOT(folder);
        }else{
            File[] files = folder.listFiles();
            if (files == null)
                return;
            for (File subFile : files) {
                if(subFile.getName().startsWith(".")){
                    continue;
                }
                if (subFile.isDirectory()) {
                    processFolder(subFile);
                }
            }
        }
    }
    protected static void processSNAPSHOT(File folder) {
        try {
            File file = new File(folder,"maven-metadata.xml");
            if(!file.exists()){
                file = new File(folder,"maven-metadata-nexus-snapshots.xml");
            }
            if(!file.exists()){
                System.out.println("NOT SNAPSHOT for Nexus or Local");
                return;
            }
            String xmlstring = IOUtils.toString(new FileInputStream(file));
            SAXReader reader = new SAXReader();
            Document doc = reader.read(new StringReader(xmlstring));
            Element root =  doc.getRootElement();
            String artifactId = root.element("artifactId").getText().trim();
            String version = root.element("version").getText().trim();
            version = version.substring(0,version.length() - "-SNAPSHOT".length());
            Element versioning = root.element("versioning");
            Element snapshot  = versioning.element("snapshot");
            String timestamp = snapshot.element("timestamp").getText().trim();
            String buildNumber = snapshot.element("buildNumber").getText().trim();
            String filePre = artifactId+"-"+version+"-"+timestamp+"-"+buildNumber;

            File[] files = folder.listFiles();
            if (files == null)
                return;
            for (File subFile : files) {
                if(subFile.getName().startsWith(filePre)){
                    continue;
                }
                if(subFile.getName().startsWith("maven-metadata")){
                    continue;
                }
                System.out.println("delete "+subFile.getAbsolutePath());
                subFile.delete();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
