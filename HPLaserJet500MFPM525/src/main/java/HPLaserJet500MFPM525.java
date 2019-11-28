import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

public class HPLaserJet500MFPM525 {
    //HP LaserJet 500 MFP M525
    public static JSONObject parser(String url) throws KeyManagementException, NoSuchAlgorithmException {
        JSONObject jsonMessage = null;
        Document page;

//        GetPageHttps getPageHttps = new GetPageHttps();
        page = getPage(url);

        if (page != null) {
            Element status = page.select("span[id=MachineStatus]").first();
            Element cartridge = page.select("span[id=SupplyGauge0]").first();
            Document configurationPage = getPage(url + "/hp/device/InternalPages/Index?id=ConfigurationPage");
            Element productName = configurationPage.select("strong[id=ProductName]").first();
            Element serialNumber = configurationPage.select("strong[id=SerialNumber]").first();
            Element adfCycles = configurationPage.select("strong[id=ADFMaintenance]").first();
            Element engineCycles = configurationPage.select("strong[id=EngineCycles]").first();

            JSONObject obj = new JSONObject();
            JSONArray arr;
            JSONObject arr_obj;
            try {
                obj.put("client_init", "putDevices");
                obj.put("productName", productName.text());
                obj.put("url", url);
                obj.put("serialNumber", serialNumber.text());
                obj.put("status", status.text());
                obj.put("printCycles", engineCycles.text());
                arr_obj = new JSONObject();
                arr = new JSONArray(new ArrayList<String>());

                arr_obj.put("adfCycles", adfCycles.text());
                arr.put(arr_obj);
                obj.put("KIT", arr);
                arr_obj = new JSONObject();
                arr = new JSONArray(new ArrayList<String>());
                arr_obj.put("black", cartridge.text());
                arr.put(arr_obj);
                obj.put("cartridge", arr);
                jsonMessage = obj;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jsonMessage;
    }

    private static Document getPage(String link) throws KeyManagementException, NoSuchAlgorithmException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = (hostname, session) -> true;

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        String html = null;
        try {
            URL url = new URL(link);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(2000);
            Reader reader = new InputStreamReader(con.getInputStream());
            if (reader != null) {
                while (true) {
                    int ch = reader.read();
                    if (ch == -1) {
                        break;
                    }
                    html += String.valueOf((char) ch);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (html != null) {
            Document page = Jsoup.parse(html);
            return page;
        }
        return null;
    }
}
