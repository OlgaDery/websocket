/**
 * 
 */
package ca.sait.websocket.jms;

import javax.websocket.Session;

/**
 * @author Olga
 * This class is to store the Session object and the name of the user, associated with the certain session, as 
 * its parameters for future tracking and manipulation.
 */
public class SessionData {
	
	 private Session session;
	 private String userName;
	 
	 public void setSession(Session session) {
		this.session = session;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}


	SessionData (Session session, String userName) 
	 {
		 this.session = session;
		 this.userName = userName;
	 }
	 
	 public Session getSession() {
		return session;
	}

	public String getUserName() {
		return userName;
	}


}
