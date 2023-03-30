package de.unituebingen.metadata.metadata.controller;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("bioportal-autocomplete")
@RestController
public class bioportalAutocompleteController {

    @RequestMapping(method = RequestMethod.GET, value = "/autocomplete")
    public String getAutocompleteData(@RequestParam Map<String, String> customQuery) throws UnsupportedEncodingException{

        return get("https://bioportal.bioontology.org/search/json_search/?q=" + encodeValue(customQuery.get("q")) + "&target_property=name&ontologies=" + encodeValue(customQuery.get("ontologies")) + "&response=json").replace("({data:\"", "").replace("\"})", "");
    }

    /**
     * get
     * 
     * Handles the get request
     * 
     * @param urlToGet
     * @return
    */
    private static String get(String urlToGet) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL(urlToGet);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
           
            conn.setRequestProperty("Accept", "application/json");
            rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * encodeValue
     * 
     * Encodes a value so that it can be used in a url
     * 
     * @param value
     * @return
     * @throws UnsupportedEncodingException
    */
    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
