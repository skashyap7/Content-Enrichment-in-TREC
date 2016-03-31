/**
 * Created by Milee on 3/22/2016.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONObject;

import org.json.simple.parser.JSONParser;

import static org.apache.http.params.CoreProtocolPNames.USER_AGENT;

public class YourlsShortener {

    // HTTP GET request
    public static String getShortenedUrl(String url) throws Exception {

        //"http://localhost/yourls-1.7.1/yourls-api.php?action=shorturl&username=username&password=password" +
        //"&url=https://technet.microsoft.com/en-us/library/gg494976.aspx&format=json"
        String query="http://localhost/yourls-1.7.1/yourls-api.php?action=shorturl&username=username&password=password" +
        "&url=file:///"+url.replace('\\','/')+"&format=string";
        URL obj = new URL(query);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        //System.out.println("\nSending 'GET' request to URL : " + url);
        //System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}
