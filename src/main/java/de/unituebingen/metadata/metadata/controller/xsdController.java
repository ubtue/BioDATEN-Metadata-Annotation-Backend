package de.unituebingen.metadata.metadata.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.io.Resources;

import de.unituebingen.metadata.metadata.dao.MetadataDAO;
import de.unituebingen.metadata.metadata.entities.Metadata;
import de.unituebingen.metadata.metadata.util.RemoteInvenioRDMServiceCall;

@RequestMapping("xsd")
@RestController
public class xsdController {

    @Autowired
    private MetadataDAO metadataDAO;

    private final static String TMPPATH = "/usr/local/tomcat/metadata-annotation/tmp/";
    private final static String XSLTSOURCE = "/usr/local/tomcat/metadata-annotation/xsd2html2xml/";

    // private final static String TMPPATH = "/home/qubvh01/tmp/";
    // private final static String XSLTSOURCE = "/usr/local/projects/metadata-annotation/xsd2html2xml/";

    private final static String XSDSOURCE = XSLTSOURCE + "biodaten/schemas/";
    
    

    /**
     * xsd
     * 
     * Creates a form based on of the predefined schemas
     * 
     * @param schema
     * @return
     * @throws IOException
     * @throws TransformerException
     */
    @GetMapping(value = "/{schema}")
    public String xsd(@PathVariable("schema") String schema) throws IOException, TransformerException {

        File file = this.getFileBySchemaName(schema);
        
        return this.parseFile(file, schema, null, false);
    }
    

    /**
     * uploadFile
     * 
     * Generates a single or multiple forms from uploaded template files
     * Optional: populates the form with uploaded XML files
     * 
     * @param file
     * @param fileXML
     * @return
     */
    @PostMapping
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("fileXML") Optional<MultipartFile> fileXML){

        File newFile = null;
        File newFileXML = null;

        try {

            // Save the XSD file in an temporary folder with a random name (TODO)
            newFile = new File(TMPPATH + "newFile_" + LocalDateTime.now().toString() + Math.random());
            newFileXML = null;

            // Save the MultipartFile to a normal File (html form uploads a MultipartFile)
            file.transferTo(newFile);

            // Check if XML file is present -> save it as a normal file
            if ( fileXML.isPresent() ) {
                newFileXML = new File(TMPPATH + "newFileXML_" + LocalDateTime.now().toString() + Math.random());
                fileXML.get().transferTo(newFileXML);
            } 

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {

            // Parse the xsd + xml files
            if ( newFileXML != null ) {
                return this.parseFile(newFile, file.getOriginalFilename(), newFileXML, true);
            }

            return this.parseFile(newFile, file.getOriginalFilename(), null, true);

        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return e.getMessage();
        }
        
        // return "error";
    }

    @PostMapping(value= "/mets")
    public String convertToMets(@RequestBody String xmlString) {

        String metsResult = "";

        metsResult = this.parseMetsString(xmlString);

        return metsResult;
    }

    @PostMapping(value= "/send-fdat",
                consumes = MediaType.APPLICATION_JSON_VALUE)
    public String sendDataToFdat(@RequestBody Map<String, String> customQuery) {

        String fdatResult = "";
        String metsResult = "";

        metsResult = this.parseMetsString(customQuery.get("xml"));

        try {
            fdatResult = convertMetsToFdatJson(metsResult);

            RemoteInvenioRDMServiceCall.postDataciteRecordToRemoteInvenioService(fdatResult, customQuery.get("fdatKey"));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return fdatResult;
    }

    @PostMapping(value = "/xml-input")
    public String generateFormsFromXML(@RequestParam("fileXML") MultipartFile fileXML) {

        // Create a JSON object to return
        JSONArray jsonArray = new JSONArray();

        jsonArray = this.parseXMLFile(fileXML);

        return jsonArray.toString();
    }


    @GetMapping(value = "/xml-system")
    public String generateFormsFromXMLSystem() {

        // Create a JSON object to return
        JSONArray jsonArray = new JSONArray();

        jsonArray = this.parseXMLFileSystem();

        return jsonArray.toString();
    }


    @GetMapping(value = "/xml/{id}")
    public String generateFormsFromXMLStringDatabase(@PathVariable("id") String metsId) throws IOException, TransformerException {

        // Create a JSON object to return
        JSONArray jsonArray = new JSONArray();

        jsonArray = this.parseXMLStringFromDatabase(metsId);

        return jsonArray.toString();
    }

    @GetMapping(value = "/xml-data")
    public String generateFormsFromXMLStringDatabaseCustomSchemas(@RequestParam Map<String, String> allParams) {

        // Create a JSON object to return
        JSONArray jsonArray = new JSONArray();

        jsonArray = this.parseXMLStringFromDatabase(allParams.get("metsId"), allParams.get("schemas"));

        return jsonArray.toString();
    }


    /**
     * parseFile
     * 
     * Parses the XSD (and XML) file(s)
     * 
     * @param file
     * @param fileName
     * @param xmlFile
     * @return
     * @throws TransformerException
     */
    private String parseFile(File file, String fileName, File xmlFile, Boolean deleteFiles) throws TransformerException {

        StringWriter outWriter = new StringWriter();
        StreamResult result = new StreamResult(outWriter);

        // XSLT processor
        Source xslt = new StreamSource(new File(XSLTSOURCE + "xsd2html2xml.xsl"));

        Source xml;

        // If an XML file is present, use it as a source. if not, use the XSD
        if ( xmlFile != null ) {

            try {

                // Search the first node after the <xml...>
                // and adds the information that there is a matching XSD file to parse
                String content = IOUtils.toString(new FileInputStream(xmlFile), "UTF8");

                // Takes the content of the first node after the <xml ...> and filters the content between the tags
                final Pattern nodePattern = Pattern.compile("(<\\?xml.*\\?>)(<([a-zA-Z].+?)>)", Pattern.DOTALL);
                final Matcher nodeMatcher = nodePattern.matcher(content);

                if ( nodeMatcher.find() ) {

                    String xmlns = " xmlns=\"http://www.w3.org/1999/xhtml\"";
                    String addContent = " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"" + file.getAbsolutePath() + "\"";

                    String searchedContent = nodeMatcher.group(3);

                    // Check if the xmlns attribute is missing and add it if necessary
                    final Pattern xmlnsPattern = Pattern.compile("xmlns\\=\\\"");
                    final Matcher xmlnsMatcher = xmlnsPattern.matcher(searchedContent);

                    if ( !xmlnsMatcher.find() ) {
                        addContent = xmlns + addContent;
                    }

                    // Merge the content of the filtered and the created content above
                    content = content.replaceFirst(searchedContent, searchedContent + addContent);
                    IOUtils.write(content, new FileOutputStream(xmlFile), "UTF8");
                } else {
                    
                }
                
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            xml = new StreamSource(xmlFile);
        } else {
            xml = new StreamSource(file);
        }
               
        // Use Saxon Transformer 
        TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        
        Transformer transformer = factory.newTransformer(xslt);
        transformer.transform(xml, result);

        // Save the result in a string
        StringBuffer sb = outWriter.getBuffer();
        String finalstring = sb.toString();

        // Return the name of the schema and the parsed content
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("schema", fileName);
        jsonObject.put("html", this.cleanHTML(finalstring, fileName));

        // Remove the created temp files from the system
        if ( deleteFiles ) {
            file.delete();

            if ( xmlFile != null ) {
                xmlFile.delete();
            }   
        }

        return jsonObject.toString();
    }


    /**
     * parseXMLContent
     * 
     * Parses the XML content and returns the XSLT processor result
     * 
     * @param templateFile
     * @param fileName
     * @param xmlContent
     * @return
     */
    private JSONObject parseXMLContent(File templateFile, String fileName, String xmlContent) {
    
        JSONObject formResult = new JSONObject();

        StringWriter outWriter = new StringWriter();
        StreamResult result = new StreamResult(outWriter);

        // XSLT processor
        Source xslt = new StreamSource(new File(XSLTSOURCE + "xsd2html2xml.xsl"));

        Source xml;

        xmlContent = "<?xml version=\"1.0\"?>" + xmlContent;

        // Takes the content of the first node after the <xml ...> and filters the content between the tags
        final Pattern nodePattern = Pattern.compile("(<\\?xml.*\\?>)(<([a-zA-Z].+?)>)", Pattern.DOTALL);
        final Matcher nodeMatcher = nodePattern.matcher(xmlContent);

        if ( nodeMatcher.find() ) {

            String xmlns = " xmlns=\"http://www.w3.org/1999/xhtml\"";
            String addContent = " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"" + templateFile.getAbsolutePath() + "\"";

            String searchedContent = nodeMatcher.group(3);

            // Check if the xmlns attribute is missing and add it if necessary
            final Pattern xmlnsPattern = Pattern.compile("xmlns\\=\\\"");
            final Matcher xmlnsMatcher = xmlnsPattern.matcher(searchedContent);

            if ( !xmlnsMatcher.find() ) {
                addContent = xmlns + addContent;
            }

            // Merge the content of the filtered and the created content above
            xmlContent = xmlContent.replaceFirst(searchedContent, searchedContent + addContent);

            xml = new StreamSource(new StringReader(xmlContent));

        } else {
            xml = new StreamSource(templateFile);
        }
                                 
        // Use Saxon Transformer 
        TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        
        Transformer transformer;

        try {
            transformer = factory.newTransformer(xslt);

            transformer.transform(xml, result);

            // Save the result in a string
            StringBuffer sb = outWriter.getBuffer();
            String finalstring = sb.toString();

            formResult.put("schema", fileName);
            formResult.put("html", this.cleanHTML(finalstring, fileName));

        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return formResult;
    }


    /**
     * parseMetsString
     * 
     * Parses the custom BioDATEN XML string to a METS XML string
     * @param metsString
     * @return
     */
    private String parseMetsString(String metsString) {

        String parseResult = "";

        // XSLT processor
        Source xslt = new StreamSource(new File(XSLTSOURCE + "xml2mets.xsl"));

        StringWriter outWriter = new StringWriter();
        StreamResult result = new StreamResult(outWriter);

        // TODO: Required?
        // metsString = "<?xml version=\"1.0\"?>" + metsString;

        Source xml = new StreamSource(new StringReader(metsString));

        // Use Saxon Transformer 
        TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);

        Transformer transformer;

        try {
            transformer = factory.newTransformer(xslt);

            transformer.transform(xml, result);

            // Save the result in a string
            StringBuffer sb = outWriter.getBuffer();
            parseResult = sb.toString();
            
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return parseResult;
    }


    /**
     * parseXMLFile
     * 
     * Handles the parse of the XML file
     * 
     * @param fileXML
     * @return
     */
    private JSONArray parseXMLFile(MultipartFile fileXML) {

        JSONArray result = new JSONArray();

        // Save MultipartFile to File
        File newFileXML = new File("/home/qubvh01/tmp/newFileXML_" + LocalDateTime.now().toString() + Math.random());

        try {
            
            fileXML.transferTo(newFileXML);

            // Parse the XML file and look for schemas (node newSchema)
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(newFileXML);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("newSchema");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                
                Node nNode = nList.item(temp);

                // Get the schema name
                String schemaName = nNode.getAttributes().getNamedItem("schema").getNodeValue();

                JSONObject formContent = new JSONObject();

                // Get the node content
                StringBuffer buff = new StringBuffer();
                getXMLString(nNode, false, buff, true, true);
                String schemaContent = buff.toString();

                File schemaFile = this.getFileBySchemaName(schemaName);

                // If there is a corresponding file, parse the content via the XSLT processor
                if ( schemaFile != null ) {
                    formContent = this.parseXMLContent(schemaFile, schemaName, schemaContent);
                    result.put(formContent);
                }
            }
            
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }


    /**
     * parseXMLFile
     * 
     * Handles the parse of the XML file
     * 
     * @param fileXML
     * @return
     */
    private JSONArray parseXMLFileSystem() {

        JSONArray result = new JSONArray();

        // Save MultipartFile to File
        File newFileXML = new File(TMPPATH + "mixed13.xml");

        try {
            
            // Parse the XML file and look for schemas (node newSchema)
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(newFileXML);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("newSchema");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                
                Node nNode = nList.item(temp);

                // Get the schema name
                String schemaName = nNode.getAttributes().getNamedItem("schema").getNodeValue();

                JSONObject formContent = new JSONObject();

                // Get the node content
                StringBuffer buff = new StringBuffer();
                getXMLString(nNode, false, buff, true, true);
                String schemaContent = buff.toString();

                File schemaFile = this.getFileBySchemaName(schemaName);

                // If there is a corresponding file, parse the content via the XSLT processor
                if ( schemaFile != null ) {
                    formContent = this.parseXMLContent(schemaFile, schemaName, schemaContent);
                    result.put(formContent);
                }
            }
            
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }


    /**
     * parseXMLFile
     * 
     * Handles the parse of the XML string from the database
     * 
     * @param fileXML
     * @return
     */
    private JSONArray parseXMLStringFromDatabase(String metsId, String... schemas) {

        JSONArray result = new JSONArray();

        Optional<Metadata> metadata = metadataDAO.findByMetsId(UUID.fromString(metsId));

        Metadata metadataContent = metadata.get();

        String xmlContent = metadataContent.getMets_xml();

        try {
            
            // Parse the XML file and look for schemas (node newSchema)
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlContent)));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("newSchema");

            String[] splittedSchemas;
            List<String> schemasList;

            // Check if there are custom schemas
            if ( schemas.length > 0 ) {

                // Split the query param and add it to a list so it can be used with contains() function
                splittedSchemas = schemas[0].split(",");
                schemasList = new ArrayList<>(Arrays.asList(splittedSchemas));
            } else {
                schemasList = Collections.emptyList();
            }

            // Keep the initial size because content from the list is removed later
            int schemaListInitialSize = schemasList.size();

            // Loop through all the found schemas
            for (int temp = 0; temp < nList.getLength(); temp++) {
                
                Node nNode = nList.item(temp);

                // Get the schema name
                String schemaName = nNode.getAttributes().getNamedItem("schema").getNodeValue();

                // Check if there are custom schemas and check if the custom schema matches with the one found
                // If not, skip the loop
                if ( schemaListInitialSize > 0 && !schemasList.contains(schemaName) ) {
                    continue;
                }

                JSONObject formContent = new JSONObject();

                // Get the node content
                StringBuffer buff = new StringBuffer();
                getXMLString(nNode, false, buff, true, true);
                String schemaContent = buff.toString();

                File schemaFile = this.getFileBySchemaName(schemaName);

                // If there is a corresponding file, parse the content via the XSLT processor
                if ( schemaFile.exists() && !schemaFile.isDirectory() ) {
                    formContent = this.parseXMLContent(schemaFile, schemaName, schemaContent);
                    result.put(formContent);

                    // If there are custom schemas, remove the handled from the list
                    if ( schemasList.size() > 0 && schemasList.contains(schemaName) ) {
                        schemasList.remove(schemaName);
                    }
                }                
            }

            // Check if there are unused custom schemas left
            // This can happen if the xml string in the database does not include content or information about a schema
            // If for example the user did not save any information for schema x, schema x will not get a newSchema node in the database
            // Checking for unused schemas will ensure that these are rendered as well. If not, the processor will only render schemas
            // that are already present in the xml database string with a newSchema node
            if ( schemasList.size() > 0 ) {

                for ( String currentSchema: schemasList ) {

                    // Get the schema name
                    String schemaName = currentSchema;

                    JSONObject formContent = new JSONObject();

                    File schemaFile = this.getFileBySchemaName(schemaName);

                    // If there is a corresponding file, parse the content via the XSLT processor
                    if ( schemaFile.exists() && !schemaFile.isDirectory() ) {
                        formContent = this.parseXMLContent(schemaFile, schemaName, "");
                        result.put(formContent);
                    }         
                }
            }
            
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }


    /**
     * cleanHTML
     * 
     * Clean the HTML code that was generated by the XSLT processor
     * 
     * @param htmlInput
     * @param templateFilename
     * @return
     */
    private String cleanHTML(String htmlInput, String templateFilename) {

        String htmlOutput = "";

        String headContent = "";
        String bodyContent = "";

        // Cut content between <head>...</head> and <body>...</body> 
        // Only the content between the tags should be returned
        final Pattern patternHead = Pattern.compile("<head>(.+?)</head>", Pattern.DOTALL);
        final Matcher matcherHead = patternHead.matcher(htmlInput);

        if (matcherHead.find()) {
            headContent = matcherHead.group(1);
        }

        final Pattern patternBody = Pattern.compile("<body>(.+?)</body>", Pattern.DOTALL);
        final Matcher matcherBody = patternBody.matcher(htmlInput);

        if (matcherBody.find()) {
            bodyContent = matcherBody.group(1);
        }

        htmlOutput = (headContent + bodyContent).trim();

        // Remove all meta, script and style tags
        htmlOutput = htmlOutput.replaceAll("(?i)<meta[^>]*>", " ").replaceAll("\\s+", " ").trim();
        htmlOutput = htmlOutput.replaceAll("(?i)<style[^>]*>", " ").replaceAll("\\s+", " ").trim();
        htmlOutput = htmlOutput.replaceAll("(?i)<script[^>]*>", " ").replaceAll("\\s+", " ").trim();
        htmlOutput = htmlOutput.replaceAll("(?i)</style[^>]*>", " ").replaceAll("\\s+", " ").trim();
        htmlOutput = htmlOutput.replaceAll("(?i)</script[^>]*>", " ").replaceAll("\\s+", " ").trim();

        // The string --|placeholder|-- is set in the XSLT processor to identify code thats needs to be replaced.
        // Replace the string --|placeholder|-- with the XSD filename
        // and replace all onclick functions with the functions in the
        // window['xsd2html2xml'][templateFilename] namespace so the code works with
        // multiple schemas
        htmlOutput = htmlOutput
            .replace("--|placeholder|--", templateFilename)
            .replaceAll("onclick=\"", "onclick=\"window['xsd2html2xml']['" + FilenameUtils.removeExtension(templateFilename) + "'].");

        return htmlOutput;
    }


    /**
     * getXMLString
     * 
     * Writes XML String to buffer
     * 
     * @param node
     * @param withoutNamespaces
     * @param buff
     * @param endTag
     * @param ignoreCurrent
     */
    public static void getXMLString(Node node, boolean withoutNamespaces, StringBuffer buff, boolean endTag, boolean ignoreCurrent) {

        if ( !ignoreCurrent ) {
            buff.append("<")
                .append(namespace(node.getNodeName(), withoutNamespaces));
        
            if (node.hasAttributes()) {
                buff.append(" ");
        
                NamedNodeMap attr = node.getAttributes();
                int attrLenth = attr.getLength();
                for (int i = 0; i < attrLenth; i++) {
                    Node attrItem = attr.item(i);
                    String name = namespace(attrItem.getNodeName(), withoutNamespaces);
                    String value = attrItem.getNodeValue();
        
                    buff.append(name)
                        .append("=")
                        .append("\"")
                        .append(value)
                        .append("\"");
        
                    if (i < attrLenth - 1) {
                        buff.append(" ");
                    }
                }
            }
        }
    
        if (node.hasChildNodes()) {

            if ( !ignoreCurrent ) {
                buff.append(">");
            }
    
            NodeList children = node.getChildNodes();
            int childrenCount = children.getLength();
    
            if (childrenCount == 1) {
                Node item = children.item(0);
                int itemType = item.getNodeType();
                if (itemType == Node.TEXT_NODE) {
                    if (item.getNodeValue() == null) {
                        buff.append("/>");
                    } else {
                        buff.append(item.getNodeValue());
                        buff.append("</")
                            .append(namespace(node.getNodeName(), withoutNamespaces))
                            .append(">");
                    }
    
                    endTag = false;
                }
            }
    
            for (int i = 0; i < childrenCount; i++) {
                Node item = children.item(i);
                int itemType = item.getNodeType();
                if (itemType == Node.DOCUMENT_NODE || itemType == Node.ELEMENT_NODE) {
                    getXMLString(item, withoutNamespaces, buff, endTag, false);
                }
            }
        } else if ( !ignoreCurrent ) {
            if (node.getNodeValue() == null) {
                buff.append("/>");
            } else {
                buff.append(node.getNodeValue());
                buff.append("</")
                    .append(namespace(node.getNodeName(), withoutNamespaces))
                    .append(">");
            }
    
            endTag = false;
        }
    
        if (endTag && !ignoreCurrent ) {
            buff.append("</")
                .append(namespace(node.getNodeName(), withoutNamespaces))
                .append(">");
        }
    }
    

    /**
     * namespace
     * 
     * Gets the namespace of the Node
     * 
     * @param str
     * @param withoutNamespace
     * @return
     */
    private static String namespace(String str, boolean withoutNamespace) {
        if (withoutNamespace && str.contains(":")) {
            return str.substring(str.indexOf(":") + 1);
        }
    
        return str;
    }


    /**
     * getFileBySchemaName
     * 
     * Returns the schema file for the schema name
     * 
     * @param schemaName
     * @return
     */
    private File getFileBySchemaName(String schemaName) {

        File file;

        switch (schemaName) {

            case "biodatenMinimal":
                file = new File(XSDSOURCE + "BiodatenMinimal.xsd");
                break;

            case "premis":
                file = new File(XSDSOURCE + "premis.xsd");
                break;

            case "datacite":
                file = new File(XSDSOURCE + "datacite.xsd");
                break;
                
            default:
                file = new File(XSDSOURCE + schemaName + ".xsd");
                break;
        }

        return file;
    }

    private String convertMetsToFdatJson(String metsString) throws IOException, InterruptedException {

        // Save the metString to a file in an temporary folder with a random name 
        // We need the xml to be in a valid file for the converting python script to work
        File newFileMetsXML = new File(TMPPATH + "newFile_" + LocalDateTime.now().toString() + Math.random());

        BufferedWriter writer = new BufferedWriter(new FileWriter(newFileMetsXML));
        writer.write(metsString);
    
        writer.close();

        File newJSONFile = new File(TMPPATH + "newFile_" + LocalDateTime.now().toString() + Math.random());

        // Call the python script (Arg 1: Path to xml file, Arg 2: Path to temp JSON file which we read)
        Process p = new ProcessBuilder("python2.7", Resources.getResource("static/assets/python/convert_fdat.py").getPath(), newFileMetsXML.getAbsolutePath(), newJSONFile.getAbsolutePath())
            .redirectErrorStream(true)
            .start();
        p.getInputStream().transferTo(System.out);
        int rc = p.waitFor();

        Path filePath = newJSONFile.toPath();

        //Path filePath = Paths.get(Resources.getResource("static/assets/python/example_metadata.json").getPath());

        String result = Files.readString(filePath);

        // Delete the files
        if ( newFileMetsXML.exists() ) {
            newFileMetsXML.delete();
        }
        
        if ( newJSONFile.exists() ) {
            newJSONFile.delete();
        }

        return result;
    }
}
