package ca.sait.websocket.jms;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import javax.ejb.EJB;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/websocket/{name}")
public class SampleEndPoint  implements Serializable {

    private static final long serialVersionUID = -5210916133350425632L;

    private static final Logger LOG = LoggerFactory.getLogger(SampleEndPoint.class);
    private final static ReentrantReadWriteLock RWLOCK = new ReentrantReadWriteLock();
    private final static ReadLock READ = RWLOCK.readLock();
  
    @Inject
    private SendMessage senderBean;
    
    @EJB
    //this singleton bean is used to store and manipulate data related to websocket sessions
    private SessionDataBean sessEJB;
 
    
    public SampleEndPoint() {
    	LOG.trace("ENTER SampleEndPoint()");
    	LOG.trace("EXIT SampleEndPoint()");
	}
    

    @OnOpen
    //this method opens a session and gets a user`s name as a @PathParam, and stores a new SessionData object
    //(created from Session and user`s name as constructor parameters) in the static map of the SessionDataBean EJB
    public void onOpen(final Session session, @PathParam("name") final String name) {
    	LOG.trace("ENTER onOpen(session)");
    	 
        try {
            
            session.getBasicRemote().sendText("session opened for " + name);
            LOG.info("user name :{}", name);
            LOG.info("session ID :{}", session.getId());

            SessionDataBean.addSessionData(session, name);
          //adding read lock to avoid simultaneous read from the map
            READ.lock();
            for (String key : SessionDataBean.getSessionDataMap().keySet()) 
		    {
		        	if (!key.equals(session.getId()))
		            {
		            		try {
				            	//sending text messages to all the active users of the application except the new one	
		            			SessionDataBean.getSessionDataMap().get(key).getSession().getBasicRemote().sendText("joined ".concat(": ").concat(name));
				                } catch (IOException ex) {
				                    LOG.error(ex.getMessage(), ex);
				                }
		            	}
		        	   
		            	
		    }      
            READ.unlock();
		    //unlocking

            if (senderBean == null) {
                LOG.info("senderBean is null");
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
        	LOG.trace("EXIT onOpen(session)");
        }
    }

    @OnMessage
    public void onMessage(final String message, final Session client) {
    	LOG.trace("ENTER onMessage(message, session)");
    	
    	LOG.info("session ID :{}", client.getId());
        //creating a string by concatenation of the message body, the user`s name and the users ID and sending it to a queue 
            	if (senderBean != null) 
            	{
                        senderBean.sendMessage(message.concat("&").concat(client.getId()).concat("&").concat(SessionDataBean.findNameByID(client)));
            		
            	}
            
    	LOG.trace("EXIT onMessage(message, session)");
    }

    @OnClose
    public void onClose(final Session session) {
    	
    	LOG.trace("ENTER onClose(session)");
        try {
        	 LOG.info("session closed :{}", session.getId());
        	 //getting a user`s name from the SessionData object before it is getting removed from the SessionDataBean map
        	 String userName = SessionDataBean.findNameByID(session);
        	 SessionDataBean.remove(session);
        	 
        	//adding read lock to avoid simultaneous reading from the map
        	 READ.lock();
        	 
        	//sending the text messages to the rest of the active users using the SessionData objects being stored in the SessionDataBean map
        	 for (String key : SessionDataBean.getSessionDataMap().keySet()) 
 		    {
 		        	try 
 		        	{
 				            		
 		        		SessionDataBean.getSessionDataMap().get(key).getSession().getBasicRemote().sendText("quited".concat(": ").concat(userName));
 				     } catch (IOException e) 
 		        	{
 				                    LOG.error(e.getMessage(), e);
 				     }
 		            	
 		    }
        	  //unlocking
        	 READ.unlock();
             

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            
   
        } finally {
        	LOG.trace("EXIT onClose(session)");
        }
    }
    
    //this method is being called after the message being sent to the queue reached the destination and 
    //jmsEvent fired off
    public void onJMSMessage(@Observes @JMSMessage Message msg) {
    	LOG.trace("ENTER onJMSMessage(message)");

        LOG.info("Got JMS Message at WebSocket!");
        
		try {
			//parsing the message body to extract the message, the creator ID and the creator name
			String[] data = msg.getBody(String.class).split("&");
			//adding read lock to avoid simultaneous read from the map
			READ.lock();
		    for (String key : SessionDataBean.getSessionDataMap().keySet()) 
		    {
		    	//sending text of the received message to all the users except one who created this message
		        	if (!key.equals(data[1]))
		            {
		            	try {
		            		
		            		SessionDataBean.getSessionDataMap().get(key).getSession().getBasicRemote().sendText("message from ".concat(data[2]).concat(": ").concat(data[0]));
		            		
		                } catch (IOException ex) {
		                    LOG.error(ex.getMessage(), ex);
		                }
		            } else {
		            	continue;
		            }
		        }
		    
		  //unlocking
		    READ.unlock();
		    
		    
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			LOG.error(e.getMessage(), e);
		}
        

        LOG.trace("EXIT onJMSMessage(message)");
    }

}
