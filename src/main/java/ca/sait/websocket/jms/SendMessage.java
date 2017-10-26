/**
 * 
 */
package ca.sait.websocket.jms;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;

/**
 * 
 * @author Chris Elias
 */
@Stateless
public class SendMessage {
	
    @Resource(mappedName = "java:/jms/queue/eventQueue")
    private Queue myQueue;
	
    @Inject
    private JMSContext jmsContext;
    
    public void sendMessage(String message) {
        jmsContext.createProducer().send(myQueue, message);
    	
    }
}