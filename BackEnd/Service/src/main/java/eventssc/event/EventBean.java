package eventssc.event;

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
		if (events != null) {
			for (Event event : events) {
				System.out.println(event.getEventName());
			}
            Gson gson = new Gson();
            return gson.toJson(events);
		}
		return "[]";
	}

	public boolean getEventById(long eventId) throws DaoException {
		Event event = eventMgr.getEventById(eventId);
		Gson gson = new Gson();
		if (event != null) {
			// request.setAttribute(Attribute.EVENT_VIEW.toString(), product);
			String jsonInString = gson.toJson(event);
			System.out.println(jsonInString);
			System.out.println(event.getEventDescription());
			return true;
		}
		return false;
	}

	public boolean addToInterested(long eventId) throws DaoException {

		Event event = eventMgr.getEventById(eventId);

		if (event != null) {
			return true;
		}
		return false;
	}

	public boolean createEvent(String jsonStr) throws DaoException{
		return eventMgr.createEvent(jsonStr);
	}

}
