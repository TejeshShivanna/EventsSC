package eventssc.range;

import eventssc.dao.DaoException;
import eventssc.dao.DaoFactory;
import eventssc.event.Event;

import java.util.ArrayList;

/*Created by iberry on 11/6/16.
 */
public class EventManager {
    public static ArrayList<Event> getAllEvents() throws DaoException {
        return DaoFactory.getEventDao().getAllEvents();
    }

    public static Event getEventById(long eventId) throws DaoException {
        if (eventId <= 0) {
            return null;
        }
        Event event = DaoFactory.getEventDao().getEventById(eventId);

        return event;
    }

    public static boolean createEvent(String jsonStr) throws DaoException {
        return DaoFactory.getEventDao().createEvent(jsonStr);
    }

    public static double[] getLocationById(int locationId) throws DaoException {
        if (locationId <= 0) {
            return null;
        }
        double location[] = DaoFactory.getEventDao().getLocationById(locationId);
        return location;
    }

}
