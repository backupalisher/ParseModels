import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class KyoceraECOSYSP6035 {
    private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static JSONObject parser(String url) throws JSONException {
        HttpClient client = new DefaultHttpClient();
        String body = null;

        JSONObject obj = new JSONObject();
        JSONArray arr;
        JSONObject arr_obj;

        obj.put("client_init", "putDevices");
        obj.put("url", url);
        obj.put("article", "0");
        obj.put("productName","Kyocera ECOSYS P6035");

        //Счетчик
        try {
            HttpGet response = new HttpGet(url + "/dvcinfo/dvccounter/DvcInfo_Counter_PrnCounter.htm");
            ResponseHandler<String> handler = new BasicResponseHandler();
            body = client.execute(response, handler);

            String[] subStr = body.split(";");
            for (int i = 1; i < subStr.length; i++) {
                if (subStr[i].contains("counterTotal[1] = ")) {
                    int printTotal = Integer.parseInt(subStr[i].replaceAll("[^0-9\\\\+]", "").substring(1));
                    obj.put("printCycles", printTotal);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Статус
        try {
            HttpGet response = new HttpGet(url + "/startwlm/Hme_DvcSts.htm");
            ResponseHandler<String> handler = new BasicResponseHandler();
            body = client.execute(response, handler);

            byte[] ptext = body.getBytes(ISO_8859_1);
            String value = new String(ptext, UTF_8);
            String[] subStr = value.split(";");
            for (int i = 1; i < subStr.length; i++) {
                if (subStr[i].contains("Status[4] = ")) {
                    obj.put("status", subStr[i].substring(14).trim().replaceAll("\"",""));
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Статус
        try {
            HttpGet response = new HttpGet(url + "/dvcinfo/dvcconfig/DvcConfig_Config.htm");
            ResponseHandler<String> handler = new BasicResponseHandler();
            body = client.execute(response, handler);

            String[] subStr = body.split(";");
            for (int i = 1; i < subStr.length; i++) {
                if (subStr[i].contains("ComnAddLabelProperty('2',mes[174]+")) {
                    String serial = subStr[i].trim().substring(40,50);
                    obj.put("serialNumber", serial);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Картриджи
        try {
            HttpGet response = new HttpGet(url + "/startwlm/Hme_Toner.htm");
            ResponseHandler<String> handler = new BasicResponseHandler();
            body = client.execute(response, handler);

            arr_obj = new JSONObject();
            arr = new JSONArray(new ArrayList<String>());

            String[] subStr = body.split(";");
            for (int i = subStr.length-1; i >= 0; i--) {
                if (subStr[i].contains("Renaming[3]")) {
                    arr_obj.put("yellow", subStr[i].substring(14).trim().replaceAll("[^0-9]",""));
                }
                if (subStr[i].contains("Renaming[2]")) {
                    arr_obj.put("magenta", subStr[i].substring(14).trim().replaceAll("[^0-9]",""));
                }
                if (subStr[i].contains("Renaming[1]")) {
                    arr_obj.put("cyan", subStr[i].substring(14).trim().replaceAll("[^0-9]",""));
                }
                if (subStr[i].contains("Renaming[0]")) {
                    arr_obj.put("black", subStr[i].substring(14).trim().replaceAll("[^0-9]",""));
                    break;
                }
            }
            arr.put(arr_obj);
            obj.put("cartridge", arr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
