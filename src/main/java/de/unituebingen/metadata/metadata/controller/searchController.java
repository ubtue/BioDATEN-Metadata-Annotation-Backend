package de.unituebingen.metadata.metadata.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.io.Resources;

@RequestMapping("search")
@RestController
public class searchController {
   
    private final static String CHECK_STRING = "111";

    private final static String GDS_RESULT_BACKUP = "static/assets/python/harvesting/gds_result.txt";
    private final static String TRANSFORM_GDS_IDS_PATH = "static/assets/python/harvesting/230331_transformGdsIds.py";
    private final static String GET_SERIES_IDS_PATH = "static/assets/python/harvesting/230331_getSeriesIds.py";
    private final static String GET_METADATA_PATH = "static/assets/python/harvesting/230331_getMetadata.py";


    // private final static String TMPPATH = "/usr/local/tomcat/metadata-annotation/tmp/harvesting/";

    private final static String TMPPATH = "/home/qubvh01/tmp/harvesting/";


    /**
     * handleSolrUpdate
     * 
     * Handles the update process of the solr core. This will call different functions to get the
     * solr content up to date.
     * 
     * @param check
     * @return
     */
    @GetMapping(value = "/update-solr/{check}")
    public String handleSolrUpdate(@PathVariable("check") String check) {

        String result = "Success";

        // Check if the all response file is present
        File allResponseJson = new File(TMPPATH + "all_response.json");

        if ( allResponseJson.exists() && !allResponseJson.isDirectory() ) {

            /*TODO:
             * For now, this method is not used anymore. The update will happen on the server directly.
             * The method used by the update is the getResponseJson method.
             */

        } else {

            result = "No response file found. Please make sure the update-data url was called before.";
        }

        return result;
    }


    /**
     * handleDataUpdate
     * 
     * Handles the json file update
     * Calls 3 python scripts in succession
     * 
     * @param check
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @GetMapping(value = "/update-data/{check}")
    public String handleDataUpdate(@PathVariable("check") String check) throws IOException, InterruptedException {


        /* TODO:
         * For all the methods: It would be nice to get good status messages, so they are defined as String methods
         */
        String result = "Success";

        // Call all 3 python scripts
        transformGdsIds();
        getSeriesIds();
        getMetadata();

        return result;
    }


    /**
     * getResponseJson
     * 
     * Outputs the json file with all results as a string
     * 
     * @param check
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @GetMapping(value = "/get-response-json/{check}")
    public String getResponseJson(@PathVariable("check") String check) throws IOException, InterruptedException {

        String result = "";

        // Check if the all response file is present
        File allResponseJson = new File(TMPPATH + "all_response.json");

        if ( allResponseJson.exists() && !allResponseJson.isDirectory() ) {

            // Ouput json file as string
            Path filePath = allResponseJson.toPath();

            // Get the file content as the result
            result = Files.readString(filePath);
        }

        return result;
    }
    

    /**
     * transformGdsIds
     * 
     * Calls the python script for transforming the gd ids
     * 
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private String transformGdsIds() throws IOException, InterruptedException {

        // Check if gds_result file exists, if not copy the backup file there
        File gdsResultFile = new File(TMPPATH + "gds_result.txt");

        if ( !gdsResultFile.exists() ) {
            
            File gdsResultBackupFile = new File(Resources.getResource(GDS_RESULT_BACKUP).getPath());

            if ( gdsResultBackupFile.exists() && !gdsResultBackupFile.isDirectory() ) {
                FileUtils.copyFile(gdsResultBackupFile, gdsResultFile);
            }
        } 

        // Call the python script to transform the GDS IDS (Arg 1: Path to temp directory)
        Process p = new ProcessBuilder("python2.7", Resources.getResource(TRANSFORM_GDS_IDS_PATH).getPath(), TMPPATH)
            .redirectErrorStream(true)
            .start();
        p.getInputStream().transferTo(System.out);
        int rc = p.waitFor();

        return "";
    }


    /**
     * getSeriesIds
     * 
     * Calls the python script for the series ids
     * 
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private String getSeriesIds() throws IOException, InterruptedException {

        // Call the python script to transform the GDS IDS (Arg 1: Path to temp directory)
        Process p = new ProcessBuilder("python2.7", Resources.getResource(GET_SERIES_IDS_PATH).getPath(), TMPPATH)
            .redirectErrorStream(true)
            .start();
        p.getInputStream().transferTo(System.out);
        int rc = p.waitFor();

        return "";
    }
    

    /**
     * getMetadata
     * 
     * Calls a python script thats transforms the metadata in the right way to import it to a solr core
     * 
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private String getMetadata() throws IOException, InterruptedException {

        // Call the python script to transform the GDS IDS (Arg 1: Path to temp directory)
        Process p = new ProcessBuilder("python2.7", Resources.getResource(GET_METADATA_PATH).getPath(), TMPPATH)
            .redirectErrorStream(true)
            .start();
        p.getInputStream().transferTo(System.out);
        int rc = p.waitFor();

        return "";
    }
}