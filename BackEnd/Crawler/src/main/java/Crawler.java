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
import java.util.ArrayList;
import java.util.List;

public class Crawler {
    final static Logger logger = LoggerFactory.getLogger(Crawler.class);

    public static JSONArray getEventsInfo(Document document){
        JSONArray eventsInfo = new JSONArray();
        try {
            Elements events = document.getElementById("events").getElementsByTag("li");

            for(Element event: events){
                String eventLink = event.getElementsByTag("a").attr("href");
                String eventName = event.getElementsByTag("a").text();

                Document descriptionDocument = Jsoup.connect(eventLink).get();

                Element eventDateAndTime = descriptionDocument.getElementById("detail_left");
                String eventDate = eventDateAndTime.getElementsByClass("date").first().text();
                String eventTime = eventDateAndTime.getElementsByTag("time").first().text();

                Element descriptionAndLocation = descriptionDocument.getElementById("evernote");
                String eventDescription = descriptionAndLocation.getElementsByTag("p").first().text();
                String eventLocation = descriptionAndLocation.getElementById("location").text();

                List<String> eventCategories = new ArrayList<String>();
                Element category = descriptionDocument.getElementById("categories");
                for(Element c: category.getElementsByTag("a")){
                    eventCategories.add(c.text());
                }

                JSONObject eventInfo = new JSONObject();
                eventInfo.put("EventName", eventName);
                eventInfo.put("EventDate", eventDate);
                eventInfo.put("EventTime", eventTime);
                eventInfo.put("EventLocation", eventLocation);
                eventInfo.put("EventDescription", eventDescription);
                eventInfo.put("EventCategory", eventCategories.toString());
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
            String url = "https://careers.usc.edu/eventcalendar/";
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
