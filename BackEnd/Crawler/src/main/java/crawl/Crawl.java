package crawl;

import location.UscLocation;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.Constants;
import table.Event;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Crawl {

    private final Logger logger = LoggerFactory.getLogger(Crawl.class);

    public JSONArray crawlEvents(Document document){

        JSONArray eventsInfo = new JSONArray();
        try {
            Elements events = document.getElementById("events").getElementsByTag("li");

            for(Element event: events){
                String eventLink = event.getElementsByTag("a").attr("href");
                String eventName = event.getElementsByTag("a").text();

                Document descriptionDocument = Jsoup.connect(eventLink).get();

                Element eventDateAndTime = descriptionDocument.getElementById("detail_left");
                String eventDate = eventDateAndTime.getElementsByClass("date").first().text();
                String[] eventTimes = eventDateAndTime.getElementsByTag("time").first().text().split("-");
                String eventStartTime = eventTimes[0].trim();
                if(!eventStartTime.contains("AM") && !eventStartTime.contains("PM")){
                    eventStartTime = "12:00 AM";
                }
                String eventEndTime = (eventTimes.length==2)?eventTimes[1].trim():"NA";
                if(!eventEndTime.contains("AM") && !eventEndTime.contains("PM")){
                    eventEndTime = "11:00 PM";
                }

                Element descriptionAndLocation = descriptionDocument.getElementById("evernote");
                String eventDescription = descriptionAndLocation.getElementsByTag("p").first().text();
                String eventLocation = descriptionAndLocation.getElementById("location").text();

                List<String> eventCategories = new ArrayList();
                Element category = descriptionDocument.getElementById("categories");
                for(Element c: category.getElementsByTag("a")){
                    eventCategories.add(c.text());
                }

                JSONObject eventInfo = new JSONObject();
                eventInfo.put("name", eventName.replaceAll("'", "''"));
                eventInfo.put("date", eventDate);
                eventInfo.put("startTime", eventStartTime);
                eventInfo.put("endTime", eventEndTime);
                eventInfo.put("address", eventLocation);
                eventInfo.put("description", eventDescription.replaceAll("'", "''"));
                eventInfo.put("category", eventCategories.toString());
                eventsInfo.add(eventInfo);
            }
        }
        catch (Exception e){
            logger.error(e.getStackTrace().toString());
        }
        return eventsInfo;
    }

    public JSONArray crawlBuilding(Document document){
        JSONArray buildingsInfo = new JSONArray();
        try {
            Elements buildingList = document.getElementById("buildingList").getElementsByTag("tbody").get(0).children();

            for(int i=1; i<buildingList.size(); i++){
                Elements buildingInfo = buildingList.get(i).children();
                JSONObject buildingInfoJson = new JSONObject();
                buildingInfoJson.put("code", buildingInfo.get(1).getElementsByTag("a").text());
                buildingInfoJson.put("name", buildingInfo.get(2).getElementsByTag("a").text());
                buildingInfoJson.put("address", buildingInfo.get(3).getElementsByTag("a").text());
                if(buildingInfoJson.get("address").toString().contains("CA")){
                    buildingsInfo.add(buildingInfoJson);
                }
            }
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
        return buildingsInfo;
    }

    public void crawlAndWriteToFile(String crawlItem){
        try{
            JSONObject jsonObject = new JSONObject();
            Document document;
            JSONArray eventsInfo;
            String fileName = null;
            if(crawlItem.equals("Events")){
                document = Jsoup.connect(Constants.crawlUrl).get();
                eventsInfo = crawlEvents(document);
                jsonObject.put("EventsInfo", eventsInfo);
                fileName = Constants.eventsFile;
            }
            else if(crawlItem.equals("Building")) {
                document = Jsoup.connect(Constants.buildingUrl).get();
                eventsInfo = crawlBuilding(document);
                jsonObject.put("BuildingInfo", eventsInfo);
                fileName = Constants.bldgFile;
            }

            File file = new File(fileName);
            file.getParentFile().mkdirs();
            FileWriter fileWriter = new FileWriter(fileName);

            fileWriter.write(jsonObject.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        }
        catch (IOException io){
            logger.error(io.getStackTrace().toString());
        }
    }

    public List<Event> getEventsInfo() throws Exception{
        List<Event> eventList = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(Constants.eventsFile));
            JSONArray jsonArray = (JSONArray) jsonObject.get("EventsInfo");

            eventList = new ArrayList<Event>();
            for (int i = 0; i < jsonArray.size(); i++) {
                eventList.add(objectMapper.readValue(jsonArray.get(i).toString(), Event.class));
            }
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
        return eventList;
    }

    public List<UscLocation> getUscLocationsInfo() throws Exception{
        List<UscLocation> locationList = null;
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(Constants.bldgFile));
            JSONArray jsonArray = (JSONArray) jsonObject.get("BuildingInfo");

            locationList = new ArrayList<UscLocation>();
            for (int i = 0; i < jsonArray.size(); i++) {
                locationList.add(objectMapper.readValue(jsonArray.get(i).toString(), UscLocation.class));
            }
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
        return locationList;
    }
}