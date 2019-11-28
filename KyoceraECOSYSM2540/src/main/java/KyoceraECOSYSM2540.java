import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class KyoceraECOSYSM2540 {
    //Kyocera ECOSYS M2540dn
    public JSONObject parser(String url) throws JSONException {
        String body = null;
        try {
            HttpClient client = new DefaultHttpClient();

            HttpGet response = new HttpGet(url + "/js/jssrc/model/dvcinfo/dvccounter/DvcInfo_Counter_PrnCounter.model.htm");
            ResponseHandler<String> handler = new BasicResponseHandler();
            body = client.execute(response, handler);

            response = new HttpGet(url + "/js/jssrc/model/dvcinfo/dvccounter/DvcInfo_Counter_ScanCounter.model.htm");
            body += client.execute(response, handler);

            response = new HttpGet(url + "/js/jssrc/model/startwlm/Hme_DvcSts.model.htm");
            body += client.execute(response, handler);

            response = new HttpGet(url + "/js/jssrc/model/dvcinfo/dvcconfig/DvcConfig_Config.model.htm?arg1=0");
            body += client.execute(response, handler);

            response = new HttpGet(url + "/js/jssrc/model/startwlm/Hme_Toner.model.htm");
            body += client.execute(response, handler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] subStr;
        int printTotal = 0;
        int scanTotal = 0;

        JSONObject obj = new JSONObject();
        JSONArray arr;
        JSONObject arr_obj;

        obj.put("client_init", "putDevices");
        obj.put("url", url);
        obj.put("article", "0");

        subStr = body.split(";");
        for (int i = 1; i < subStr.length; i++) {
            if (subStr[i].contains("_pp.copytotal")) {
                printTotal = Integer.parseInt(subStr[i].replaceAll("[^0-9\\\\+]", ""));
            }
            if (subStr[i].contains("_pp.printertotal")) {
                printTotal += Integer.parseInt(subStr[i].replaceAll("[^0-9\\\\+]", ""));
                obj.put("printCycles", printTotal);
            }
            if (subStr[i].contains("_pp.scanCopy")) {
                scanTotal = Integer.parseInt(subStr[i].replaceAll(", 10", "").replaceAll("[^0-9\\\\+]", ""));
            }
            if (subStr[i].contains("_pp.scanBlackWhite")) {
                scanTotal = scanTotal + Integer.parseInt(subStr[i].replaceAll(", 10", "").replaceAll("[^0-9\\\\+]", ""));
            }
            if (subStr[i].contains("_pp.scanOther")) {
                scanTotal = scanTotal + Integer.parseInt(subStr[i].replaceAll(", 10", "").replaceAll("[^0-9\\\\+]", ""));
                obj.put("scanCycles", scanTotal);
            }
            if (subStr[i].contains("_pp.PanelMessage")) {
                obj.put("status", subStr[i].replaceAll("_pp.PanelMessage = '", "").replaceAll("'", "").trim());
            }
            if (subStr[i].contains("_pp.bonjourName")) {
                obj.put("productName", subStr[i].replaceAll("_pp.bonjourName = '", "").replaceAll("'", "").trim());
            }
            if (subStr[i].contains("_pp.serialNumber")) {
                obj.put("serialNumber", subStr[i].replaceAll("_pp.serialNumber = '", "").replaceAll("'", "").trim());
            }
            if (subStr[i].contains("_pp.ipv4IPAddressWired")) {
                System.out.println(subStr[i].replaceAll("_pp.ipv4IPAddressWired = '", "").replaceAll("'", "").trim());
            }
            if (subStr[i].contains("_pp.Renaming.push")) {
                if (!subStr[i].contains("_pp.Renaming.push(parseInt('-1', 10))")) {
                    arr_obj = new JSONObject();
                    arr = new JSONArray(new ArrayList<String>());
                    arr_obj.put("black", subStr[i].replaceAll(",10", "").replaceAll("[^0-9\\\\+]", ""));
                    arr.put(arr_obj);
                    obj.put("cartridge", arr);
                }
            }
        }
        return obj;
    }
}
