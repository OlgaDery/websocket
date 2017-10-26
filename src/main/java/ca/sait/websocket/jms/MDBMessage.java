package ca.sait.websocket.jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "eventQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") 
    }, mappedName = "eventQueueMDB")
public class MDBMessage implements MessageListener {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    @Inject
    @JMSMessage
    private Event<Message> jmsEvent;

    @Override
    public void onMessage(Message msg) {
    	logger.trace("ENTER onMessage()");
        jmsEvent.fire(msg);
        
        logger.trace("EXIT onMessage()");
    }

}