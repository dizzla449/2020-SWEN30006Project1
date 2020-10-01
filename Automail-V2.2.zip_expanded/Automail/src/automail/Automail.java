package automail;

import simulation.IMailDelivery;

public class Automail {
	      
    public RobotHandle[] robots;
    public MailPool mailPool;
    
    public Automail(MailPool mailPool, IMailDelivery delivery, int numRobots) {  	
    	/** Initialize the MailPool */
    	
    	this.mailPool = mailPool;
    	
    	/** Initialize robots */
    	robots = new RobotHandle[numRobots];
    	for (int i = 0; i < numRobots; i++) robots[i] = new RobotHandle(delivery, mailPool, i);
    }
    
}
