package eventssc.event;

import eventssc.dao.DaoException;
import eventssc.dao.EventDao;
import eventssc.model.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class EventManager {

    private EventDao eventDao;

    @Autowired
	public EventManager(EventDao eventDao){
		this.eventDao = eventDao;
	}

	public List<Event> getAllEvents() throws DaoException {
		return eventDao.getAllEvents();
	}

	public Event getEventById(long eventId) throws DaoException {
		if (eventId <= 0) {
			return null;
		}
		Event event = eventDao.getEventById(eventId);

		return event;
	}

	public boolean createEvent(String jsonStr) throws DaoException{
		return eventDao.createEvent(jsonStr);
	}

    public double[] getLocationById(int locationId) throws DaoException {
        if (locationId <= 0) {
            return null;
        }
        double location[] = eventDao.getLocationById(locationId);
        return location;
    }

}
