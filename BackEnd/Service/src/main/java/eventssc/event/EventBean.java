package eventssc.event;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import eventssc.dao.DaoException;
import eventssc.model.Event;

public class EventBean {

    private EventManager eventMgr;

    public EventBean(EventManager eventMgr) {
        this.eventMgr = eventMgr;
    }

    public String getAllEvents() throws DaoException {
        List<Event> events = eventMgr.getAllEvents();
        List<Event> updatedEvents = new ArrayList<Event>();

        for (Event event : events) {
            double location[] = eventMgr.getLocationById(event.getLocationID());
            event.setLatitude(location[0]);
            event.setLongitude(location[1]);
            updatedEvents.add(event);
        }
        if (updatedEvents != null) {
            Gson gson = new Gson();
            return gson.toJson(events);
        }
        return "[]";
    }

    public boolean getEventById(int eventId) throws DaoException {
        Event event = eventMgr.getEventById(eventId);
        Gson gson = new Gson();
        if (event != null) {
            // request.setAttribute(Attribute.EVENT_VIEW.toString(), product);
            String jsonInString = gson.toJson(event);
            return true;
        }
        return false;
    }

//    public boolean addToInterested(int eventId, int userId, boolean status) throws DaoException {
//        return eventMgr.addToInterested(eventId, userId, status);
//    }

    public boolean createEvent(String jsonStr) throws DaoException {
        if (jsonStr == null || jsonStr == "{}") {
            return false;
        }
        return eventMgr.createEvent(jsonStr);
    }

}
