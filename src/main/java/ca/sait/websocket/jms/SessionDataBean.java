/**
 * 
 */
package ca.sait.websocket.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Olga
 * this bean is to store and manipulate SessionData objects
 */
@Singleton
@LocalBean
public class SessionDataBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(SessionDataBean.class);
	private final static ReentrantReadWriteLock RWLOCK = new ReentrantReadWriteLock();
	private final static WriteLock WRITE = RWLOCK.writeLock();
	private final static ReadLock READ = RWLOCK.readLock();
	private static String userName;
	
	// declaring a static map of SessionData objects <String, Object>
	private static final Map <String, SessionData> sessionData = new HashMap<String, SessionData>();
	
	//Method to create a new SessionData object and add it to the static map. This map can be accessed from
	//any classes getting data from the front end. In our case, it is called from the SampleEndPoint class.
	public static void addSessionData(Session session, String userName) 
	{
		LOG.trace("enter addSessionData(Session session, String userName)");
		//adding write lock to avoid simultaneous writing to the map
		WRITE.lock();
		try {
			sessionData.put(session.getId(), new SessionData (session, userName));
		} finally {
			LOG.trace("exit addSessionData(Session session, String userName)");
		}
		WRITE.unlock();
	}
	
	//Method to remove the specified session object from the static map. In is called from the onClose method.
	public static void remove (Session session) 
	{
		LOG.trace("enter remove (Session session)");
		
		WRITE.lock();
		try {
			sessionData.remove(session.getId());
		} finally {
			WRITE.unlock();
		}
		LOG.trace("enter remove (Session session)");
	}
	
	//This method is to return the name of the user associated with a certain session. 
	public static String findNameByID (Session session) 
	{
		LOG.trace("enter findNameByID (Session session)");
		//adding read lock to avoid simultaneous reading from the map
		READ.lock();
		userName = null;
		try {
			sessionData.keySet().forEach(key -> {
				if (key.equals(session.getId())) {
					userName = sessionData.get(key).getUserName();
				}
			});
		} finally {
			LOG.trace("exit findNameByID (Session session)");
		}
		
		READ.unlock();
		return userName;
		
	}
	
	//This method returns a map of Session IDs and SessionData objects. There is no locks as far as there are always read locks
	//when this method is being called from the SampleEndPoint method.
	public static Map <String, SessionData> getSessionDataMap ()
	{
		LOG.trace("enter getSessionDataMap()");
		try {
			return sessionData;
		} finally {
			LOG.trace("exit getSessionDataMap()");
		}
	}

}
