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

    public boolean markInterest(String interestStr) throws DaoException {
        return eventDao.markInterest(interestStr);
    }

    public List<Event> getTodaysEvents() throws DaoException {
        return eventDao.getTodaysEvents();
    }

    public boolean createEvent(String jsonStr) throws DaoException {
        return eventDao.createEventWithLatLong(jsonStr);
    }

    public double[] getLocationById(int locationId) throws DaoException {
        if (locationId <= 0) {
            return null;
        }
        double location[] = eventDao.getLocationById(locationId);
        return location;
    }

    public List<Event> getInterestedEvents(int userId) throws DaoException {
        return eventDao.getInterestedEvents(userId);
    }

    public List<Event> getCreatedEvents(int userId) throws DaoException {
        return eventDao.getCreatedEvents(userId);
    }

    public List<String> getUsersInterested(int eventId) throws DaoException {
        return eventDao.getUsersInterested(eventId);
    }
}
