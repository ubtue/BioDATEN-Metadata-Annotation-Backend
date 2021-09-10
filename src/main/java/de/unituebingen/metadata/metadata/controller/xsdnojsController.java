package de.unituebingen.metadata.metadata.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("xsdnojs")
@RestController
public class xsdnojsController {

    /**
     * xsdnojs
     * 
     * Creates a form based on of the predefined schemes
     * 
     * @param schema
     * @return
     * @throws IOException
     * @throws TransformerException
     */
    @GetMapping(value = "/{schema}")
    public String xsdnojs(@PathVariable("schema") String schema) throws IOException, TransformerException {

        File file;

        switch (schema) {

            case "biodatenMinimal":
                file = new File("/usr/local/projects/xsd2html2xml-nojs/biodaten/minimal/BiodatenMinimal.xsd");
                break;

            case "premis":
                file = new File("/usr/local/projects/xsd2html2xml-nojs/biodaten/premis/premis.xsd");
                break;

            case "datacite":
            default:
                file = new File("/usr/local/projects/xsd2html2xml-nojs/biodaten/datacite/datacite.xsd");
                break;
        }

        
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
            newFile = new File("/home/qubvh01/tmp/newFile_" + LocalDateTime.now().toString() + Math.random());
            newFileXML = null;

            // Save the MultipartFile to a normal File (html form uploads a MultipartFile)
            file.transferTo(newFile);

            // Check if XML file is present -> save it as a normal file
            if ( fileXML.isPresent() ) {
                newFileXML = new File("/home/qubvh01/tmp/newFileXML_" + LocalDateTime.now().toString() + Math.random());
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
        Source xslt = new StreamSource(new File("/usr/local/projects/xsd2html2xml-nojs/xsd2html2xml.xsl"));

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

        // return the name of the scheme and the parsed content
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("scheme", fileName);
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

        // The string --|placeholder|-- is set in the XSLT processor to identify code thats needs to be replaced.
        // Replace the string --|placeholder|-- with the XSD filename
        // and replace all onclick functions with the functions in the
        // window['xsd2html2xml'][templateFilename] namespace so the code works with
        // multiple schemes
        htmlOutput = htmlOutput
            .replace("--|placeholder|--", templateFilename)
            .replaceAll("onclick=\"", "onclick=\"window['xsd2html2xml']['" + FilenameUtils.removeExtension(templateFilename) + "'].");

        return htmlOutput;
    }
}
