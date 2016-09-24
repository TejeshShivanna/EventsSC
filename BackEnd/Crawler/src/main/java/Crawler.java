import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Crawler {
    final static Logger logger = LoggerFactory.getLogger(Crawler.class);

    public static JSONArray getEventsInfo(Document document){
        JSONArray eventsInfo = new JSONArray();
        try {
            Elements events = document.getElementsByClass("list-group");
            for (Element element : events.get(0).children()) {
                JSONObject eventInfo = new JSONObject();
                eventInfo.put("EventName", element.getElementsByTag("a").last().text());
                eventInfo.put("EventDate",element.getElementsByClass("timeDate").text());
                eventInfo.put("EventLocation", element.getElementsByClass("location").text());
                eventInfo.put("EventLink", element.getElementsByTag("a").last().attr("href"));
                eventsInfo.add(eventInfo);
            }
        }
        catch (Exception e){
            logger.error(e.getStackTrace().toString());
        }
        return eventsInfo;
    }

    public static void writeEventsInfoToFile(JSONArray eventsInfo, String path){
        try{
            File file = new File(path);
            file.getParentFile().mkdirs();
            FileWriter fileWriter = new FileWriter(path);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("EventsInfo", eventsInfo);
            fileWriter.write(jsonObject.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        }
        catch (IOException io){
            logger.error(io.getStackTrace().toString());
        }
    }

    public static void main(String[] args) {
        try{
            String url = "https://careers.usc.edu/alumni/info/events";
            String outputFilePath = "data/output/eventsInfo.json";

            Document document = Jsoup.connect(url).get();
            JSONArray eventsInfo = getEventsInfo(document);
            writeEventsInfoToFile(eventsInfo, outputFilePath);
        }
        catch (Exception ex){
            logger.error(ex.getStackTrace().toString());
        }
    }
}
