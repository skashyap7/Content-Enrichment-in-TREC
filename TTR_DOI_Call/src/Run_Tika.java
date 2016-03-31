import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ttr.DOIGeneratorContentHandler;
import org.apache.tika.parser.ttr.MeasurementExtractingContentHandler;
import org.apache.tika.parser.ttr.TTRContentHandler;
import org.apache.tika.sax.BodyContentHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.xml.sax.ContentHandler;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Milee on 3/22/2016.
 */

public class Run_Tika {

    //Initialize common variables
    public final static Logger ERROR_LOG = Logger.getLogger(Run_Tika.class.getName());
    public static final Set<String> MET_LIST = new HashSet<>(Arrays.asList(new String[] {
            "title","Content-Type","measurements","doi"}));
    int count = 0;

    public String getTikaOutput(String filename) throws Exception {
        File file = new File(filename);
        InputStream stream = new FileInputStream(file);

        Parser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        ContentHandler handler = new BodyContentHandler();

        // get the ttr first
        TTRContentHandler ttrContentHandler = new TTRContentHandler(handler);

        // extract measurements and add to metadata
        MeasurementExtractingContentHandler measurementExtractingContentHandler =
                new MeasurementExtractingContentHandler(ttrContentHandler, metadata);

        // generate DOI and add to metadata
        DOIGeneratorContentHandler doiGeneratorContentHandler =
                new DOIGeneratorContentHandler(measurementExtractingContentHandler, metadata);
        String basename = file.getName();
        parser.parse(stream, doiGeneratorContentHandler, metadata, context);

        //Call Yourl shortener
        String url = YourlsShortener.getShortenedUrl(metadata.get("doi"));
        //http://localhost/yourls-1.7.1/8
        //http://dx.doi.org/10.1201/b16928
        //polar.usc.edu/10.1201/b16928
        String[] metadataNames = metadata.names();
        JSONObject met_obj = new JSONObject();
        met_obj.put("short_url", url.replace("http://localhost/yourls-1.7.1", "polar.usc.edu/10.2432"));
        for (String name : metadataNames) {
            if(MET_LIST.contains(name)) {
                if (metadata.isMultiValued(name)) {
                    JSONArray met_multival = new JSONArray();
                    if(name=="measurements") {
                        JSONArray outer_arr=new JSONArray();
                        for(String s:metadata.getValues(name)) {
                            JSONArray arr = new JSONArray();
                            arr.addAll(Arrays.asList(s.split(":")));
                            outer_arr.add(arr);
                        }
                        met_multival.addAll(outer_arr);
                    }
                    else
                    met_multival.addAll(Arrays.asList(metadata.getValues(name)));
                    met_obj.put(name.toLowerCase(), met_multival);
                } else{
                        if(name=="measurements") {
                            JSONArray arr = new JSONArray();
                            for (String s : metadata.getValues(name)) {
                                arr.addAll(Arrays.asList(s.split(":")));
                            }
                            met_obj.put(name.toLowerCase(), arr);
                        }
                        else
                            met_obj.put(name.toLowerCase(), metadata.get(name));
                    }
            }
        }
        System.out.println(count++);
        //Adding basename as key
        JSONObject key_object = new JSONObject();
        key_object.put(basename, met_obj);

        return key_object.toJSONString();
    }

    public void printOutput(String dir_path) throws Exception {
        Files.walk(Paths.get(dir_path),1)
                .forEach(dir -> {
                    if(Files.isDirectory(dir)) {
                        try {
                            System.out.println("Processing Directory: " + dir.toString());
                            FileWriter fw = new FileWriter(new File("./TTR_DOI_OUTPUT/" + dir.getFileName() + "_output.json"));
                            ERROR_LOG.setLevel(Level.ALL);
                            fw.append('{');
                            //Execute tika by traversing each file in directory and creating a json for the mime type in the folder
                            Files.walk(Paths.get(dir.toUri()), 1)
                                    .forEach(file -> {
                                        if (Files.isRegularFile(file)) {
                                            try {
                                                System.out.println("File in process: " + file.toString());
                                                String output = this.getTikaOutput(file.toString());
                                                output = output.substring(1, output.length() - 1);
                                                fw.append(output + ",");
                                            } catch (Exception e) {
                                                ERROR_LOG.log(Level.ERROR, "---------->" + file.getFileName().toString() + ":" + e.getMessage());
                                            }
                                        }
                                    });
                            fw.append('}');
                            fw.flush();
                            fw.close();
                        } catch (Exception e) {
                            ERROR_LOG.log(Level.ERROR, "---------->" + dir.getFileName().toString() + ":" + e.getMessage());
                        }
                    }
                });
    }


    public static void main(String args[]) throws Exception {
        String dir_path = "";
        Run_Tika tika_obj = new Run_Tika();
        try {
            Options opt = new Options();

            opt.addOption("h", false, "Print help for this application");
            opt.addOption("s", true, "The source path of directory");

            BasicParser parser = new BasicParser();
            CommandLine cl = parser.parse(opt, args);

            if (cl.hasOption('h')) {
                HelpFormatter f = new HelpFormatter();
                f.printHelp("OptionsTip", opt);
            } else {
                dir_path = cl.getOptionValue("s");
                System.out.println(dir_path);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tika_obj.printOutput(dir_path);
    }
}
