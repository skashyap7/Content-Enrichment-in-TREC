import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Milee on 3/31/2016.
 */
public class getSolrJson {
    public static Logger ERROR_LOG = Logger.getLogger(getSolrJson.class.getName());

    public static void main(String args[]) throws Exception{
        ERROR_LOG.setLevel(Level.ALL);
        JSONParser parser=new JSONParser();
        JSONArray final_json=new JSONArray();
        int count=0;
        for(File f : new File("TTR_DOI_OUTPUT").listFiles())
        {
            try {
                Object jObject = parser.parse(new FileReader(f));
                JSONObject jsonObject = (JSONObject) jObject;
                JSONArray file_arr=new JSONArray();
                for(Object v :jsonObject.keySet()) {
                    JSONObject file_json=new JSONObject();
                    file_json.put("id",v.toString());
                    JSONObject jarr=(JSONObject) jsonObject.get(v);
                    file_json.put("content-type",jarr.get("content-type"));
                    file_json.put("short_url",jarr.get("short_url"));
                    if(jarr.containsKey("measurements")) {
                        JSONArray measure=(JSONArray) jarr.get("measurements");
                        file_json.put("measurements",measure);
                    }
                    if(jarr.containsKey("title")) {
                        file_json.put("title",jarr.get("title"));
                    }
                    file_arr.add(file_json);
                }
                final_json.add(file_arr);
            }
            catch (Exception e) {
                ERROR_LOG.log(Level.INFO, "---------->" +f.getName()+":"+e.getMessage());
            }
            File final_file=new File("TTR_DOI_JSONFORSOLR/"+f.getName().replace(".json","")+"_solr.json");
            FileWriter fw=new FileWriter(final_file);
            fw.write(final_json.toJSONString());
            fw.flush();
            fw.close();
            System.out.println("Wrote "+f.getName()+" to output directory ---"+count++);
        }
    }
}
