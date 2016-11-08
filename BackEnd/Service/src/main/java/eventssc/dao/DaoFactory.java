/*
package eventssc.dao;

import java.util.HashMap;
import java.util.Map;


public class DaoFactory {

	@SuppressWarnings("rawtypes")
	private static Map<Class, Object> cacheMap = new HashMap<Class, Object>();

	public static EventDao getEventDao() {
		EventDao eventObj = (EventDao) cacheMap.get(EventDao.class);
		if (eventObj == null) {
			eventObj = new EventDao();
			cacheMap.put(EventDao.class, eventObj);
		}
		return eventObj;
	}

}
*/
