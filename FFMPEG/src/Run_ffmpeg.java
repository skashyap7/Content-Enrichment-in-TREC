/**
 * Created by Milee on 3/29/2016.
 */

import org.apache.commons.cli.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.ContentHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Arrays;

public class Run_ffmpeg {
    public static Logger ERROR_LOG = Logger.getLogger(Run_ffmpeg.class.getName());

    public static String get_ffmpeg_output(String filepath) throws Exception {
        JSONObject key_object = new JSONObject();
        ERROR_LOG.setLevel(Level.ALL);
        for (File f : new File(filepath).listFiles()) {
            if (f.isDirectory() && f.getName().matches("(?:(video_|audio_).*)")) {
                System.out.println(f.getName());
                int count=0;
                for (File file : f.listFiles()) {
                    if (file.isFile()) {
                        System.out.println(count++);
                        try {
                            InputStream stream = new FileInputStream(file);
                            Parser parser = new AutoDetectParser();
                            Metadata metadata = new Metadata();
                            ParseContext context = new ParseContext();
                            ContentHandler handler = new BodyContentHandler();
                            parser.parse(stream, handler, metadata, context);
                            String[] metadataNames = metadata.names();
                            JSONObject met_obj = new JSONObject();
                            for (String name : metadataNames) {
                                if (metadata.isMultiValued(name)) {
                                    JSONArray met_multival = new JSONArray();
                                    met_multival.addAll(Arrays.asList(metadata.getValues(name)));
                                    met_obj.put(name.toLowerCase(), met_multival);
                                }
                                met_obj.put(name.toLowerCase(), metadata.get(name));
                            }
                            key_object.put(file.getName(), met_obj);
                        }
                        catch(Exception e){
                            ERROR_LOG.log(Level.ERROR, "---------->" +file.getName()+":"+e.getMessage());
                        }
                    }
                }
            }
        }
        return key_object.toJSONString();
    }

    public static void main(String args[])
    {
        String dir_path = "";
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
        try  {
            FileWriter fw = new FileWriter(new File("./ffmpeg_with_tika.json"));
            fw.write(Run_ffmpeg.get_ffmpeg_output(dir_path));
            fw.flush();
            fw.close();
            System.out.println("Successfully Copied JSON Object to File...");
        }
        catch (Exception e) {
            ERROR_LOG.log(Level.ERROR, "---------->" +e.getMessage());
        }

    }
}
