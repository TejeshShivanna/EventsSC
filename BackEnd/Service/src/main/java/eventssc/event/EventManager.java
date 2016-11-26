package eventssc.event;

import eventssc.dao.DaoException;
import eventssc.dao.EventDao;
import eventssc.dao.LocationDao;
import eventssc.model.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class EventManager {

    private EventDao eventDao;
    private LocationDao locationDao;

    @Autowired
    public EventManager(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    public EventManager(LocationDao locationDao) {
        this.locationDao = locationDao;
    }

    public List<Event> getAllEvents() throws DaoException {
        return eventDao.getAllEvents();
    }

    public Event getEventById(int eventId) throws DaoException {
        if (eventId <= 0) {
            return null;
        }
        Event event = eventDao.getEventById(eventId);

        return event;
    }

    public boolean createEvent(String jsonStr) throws DaoException {

        return eventDao.createEvent(jsonStr);
    }

    public boolean markInterest(String interestStr) throws DaoException {
        return eventDao.markInterest(interestStr);
    }

    public double[] getLocationById(int locationId) throws DaoException {
        if (locationId <= 0) {
            return null;
        }
        double location[] = eventDao.getLocationById(locationId);
        return location;
    }

}
