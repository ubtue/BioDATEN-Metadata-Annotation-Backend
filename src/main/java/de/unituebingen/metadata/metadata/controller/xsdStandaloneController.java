package de.unituebingen.metadata.metadata.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.StyledEditorKit;
import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@RequestMapping("xsdstandalone")
@RestController
public class xsdStandaloneController {

    private final static String XSLTSOURCE = "/usr/local/projects/metadata-annotation/old_xsd2html2xml/";
    private final static String XSDSOURCE = XSLTSOURCE + "biodaten/schemas/";
    private final static String TMPPATH = "/home/qubvh01/tmp/";

    // // ein Stylesheet zur Identitätskopie ...
    // private static final String IDENTITAETS_XSLT = "<xsl:stylesheet
    // xmlns:xsl='http://www.w3.org/1999/XSL/Transform'"
    // + " version='1.0'>" + "<xsl:template match='/'><xsl:copy-of select='.'/>"
    // + "</xsl:template></xsl:stylesheet>";

    // // ... der XML-Spezifikationen im XML-Format
    // // (mittels einer HTTP-URL statt einer file-URL)
    // private static String xmlSystemId =
    // "http://www.w3.org/TR/2000/REC-xml-20001006.xml";

    @GetMapping(value = "/{schema}")
    public String xsdstandalone(@PathVariable("schema") String schema) throws IOException, TransformerException {

        StringWriter outWriter = new StringWriter();
        StreamResult result = new StreamResult(outWriter);

        Source xslt = new StreamSource(new File(XSLTSOURCE + "xsd2html2xml.xsl"));

        Source xml;

        switch (schema) {

            case "biodatenMinimal":
                xml = new StreamSource(
                        new File(XSLTSOURCE + "biodaten/minimal/BiodatenMinimal.xsd"));
                break;

            case "premis":
                xml = new StreamSource(new File(XSLTSOURCE + "biodaten/premis/premis.xsd"));
                break;

            case "datacite":
            default:
                xml = new StreamSource(new File(XSLTSOURCE + "biodaten/datacite/datacite.xsd"));
                break;
        }

        // Source xml = new StreamSource(new
        // File("/usr/local/projects/xsd2html2xml/biodaten/datacite/datacite.xsd"));
        // Source xml = new StreamSource(new
        // File("/usr/local/projects/xsd2html2xml/biodaten/profile/BioDatenProfile.xsd"));
        // Source xml = new StreamSource(new
        // File("/usr/local/projects/xsd2html2xml/biodaten/minimal/BiodatenMinimal.xsd"));
        // Source xml = new StreamSource(new
        // File("/usr/local/projects/xsd2html2xml/biodaten/premis/premis.xsd"));
        Result out = new StreamResult(new File(XSLTSOURCE + "biodaten/datacite/form.html"));

        TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        // factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        // factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer = factory.newTransformer(xslt);
        transformer.transform(xml, result);

        StringBuffer sb = outWriter.getBuffer();
        String finalstring = sb.toString();

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("schema", schema);
        jsonObject.put("html", this.cleanHTML(finalstring));

        return jsonObject.toString();

        // String resultString = "{ \"schema\": \"" + schema + "\", \"html\": \"" +
        // this.cleanHTML(finalstring) + "\"}";

        // return resultString;

        // zeigt, wie man von einem System-Identifikator und einem Reader-Objekt liest
        // Source xmlSource = new StreamSource(xmlSystemId);
        // Source xsltSource = new StreamSource(
        // new StringReader(IDENTITAETS_XSLT));

        // // sendet das Ergebnis an eine Datei
        // File ergebnisDatei = File.createTempFile("/tmp/zResult", ".xml");
        // Result ergebnis = new StreamResult(ergebnisDatei);

        // // die Factory-Instanz erzeugen
        // TransformerFactory transFact = TransformerFactory.newInstance( );

        // // einen Transformer für dieses spezielle Stylesheet erzeugen
        // Transformer trans = transFact.newTransformer(xsltSource);

        // // und die Transformation durchführen
        // trans.transform(xmlSource, ergebnis);

        // return "Die Ergebnisse gehen an: "
        // + ergebnisDatei.getAbsolutePath( );

        // File stylesheet = new
        // File("/usr/local/projects/xsd2html2xml/xsd2html2xml.xsl");

        // StreamSource stylesource = new StreamSource(stylesheet);

        // TransformerFactory transFactory = TransformerFactory.newInstance();

        // Transformer transformer = transFactory.newTransformer(stylesource);

        // return "test";
        // String[] args = new String[] {"pwd"};
        // Process proc = new ProcessBuilder(args).start();

        // String result = new String(proc.getInputStream().readAllBytes());

        // String[] arguments = new String[] {
        // "xsltproc",
        // "-o",
        // "/usr/local/projects/xsd2html2xml/biodaten/datacite/form.html",
        // "/usr/local/projects/xsd2html2xml/xsd2html2xml.xsl",
        // "/usr/local/projects/xsd2html2xml/biodaten/datacite/datacite.xsd"};

        // Process proc = new ProcessBuilder(arguments).start();

        // String result = new String(proc.getInputStream().readAllBytes());
        // try {
        // Process proc = new ProcessBuilder(arguments).start();

        // return proc.toString();

        // } catch (IOException e) {

        // e.printStackTrace();
        // }
        // return result;
    }

    // @CrossOrigin(origins = "*")
    // @PostMapping
    // @ResponseBody
    // public String uploadFile(@RequestParam("file") MultipartFile file) {

    //     try {

    //         // String uploadDir = "/uploads/";
    //         // String realPath = request.getServletContext().getRealPath(uploadDir);

    //         // File transferFile = new File(realPath + "/" + file.getOriginalFilename());
    //         // file.transferTo(transferFile);

    //         return file.getOriginalFilename();

    //     } catch (Exception e) {

    //         e.printStackTrace();

    //         return "Failure";
    //     }

    // }

    
    @PostMapping
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("fileXML") Optional<MultipartFile> fileXML){

        File newFile = new File(TMPPATH + "newFile");
        File newFileXML = null;

        try {
            file.transferTo(newFile);

            if ( fileXML.isPresent() ) {
                newFileXML = new File(TMPPATH + "newFileXML");
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

            if ( newFileXML != null ) {
                return this.parseFile(newFile, file.getOriginalFilename(), newFileXML);
            }

            return this.parseFile(newFile, file.getOriginalFilename(), null);
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return e.getMessage();
        }
        
        // return "error";

        
    }

    
    private String parseFile(File file, String fileName, File xmlFile) throws TransformerException {

        StringWriter outWriter = new StringWriter();
        StreamResult result = new StreamResult(outWriter);

        Source xslt = new StreamSource(new File(XSLTSOURCE + "xsd2html2xml.xsl"));

        Source xml;

        if ( xmlFile != null ) {

            try {

                String content = IOUtils.toString(new FileInputStream(xmlFile), "UTF8");

                final Pattern nodePattern = Pattern.compile("(<\\?xml.*\\?>)(<[^\\s]+)", Pattern.DOTALL);
                final Matcher nodeMatcher = nodePattern.matcher(content);

                if ( nodeMatcher.find() ) {
                    content = content.replaceAll(nodeMatcher.group(2) + " ", nodeMatcher.group(2) + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"" + file.getAbsolutePath() + "\" ");
                    IOUtils.write(content, new FileOutputStream(xmlFile), "UTF8");
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
        
        TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        
        Transformer transformer = factory.newTransformer(xslt);
        transformer.transform(xml, result);

        StringBuffer sb = outWriter.getBuffer();
        String finalstring = sb.toString();

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("schema", fileName);
        jsonObject.put("html", this.cleanHTML(finalstring));

        return jsonObject.toString();
    }

    private String cleanHTML(String htmlInput) {

        String htmlOutput = "";

        String headContent = "";
        String bodyContent = "";

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

        return htmlOutput;
    }
}
