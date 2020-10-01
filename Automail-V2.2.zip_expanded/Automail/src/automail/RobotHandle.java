package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import simulation.Building;
import simulation.IMailDelivery;
import simulation.Statistics;

public class RobotHandle {
	
	static public final int INDIVIDUAL_MAX_WEIGHT = 2000;
	IMailDelivery delivery;
    protected final String id;
    /** Possible states the robot can be in */
    public enum RobotState { DELIVERING, WAITING, RETURNING }
    protected RobotState current_state;
    protected int current_floor;
    protected int destination_floor;
    protected MailPool mailPool;
    protected boolean receivedDispatch;
    protected static  int[] locked_floors;
    protected int deliveryCounter;
    public enum RobotType { REGULAR, FOOD }
    protected RegularRobot robotR;
    protected FoodRobot robotF;
    protected RobotType robot_type = RobotType.REGULAR;
        
    
    public RobotHandle(IMailDelivery delivery, MailPool mailPool, int number){
    	this.id = "R" + number;
        // current_state = RobotState.WAITING;
    	current_state = RobotState.RETURNING;
        current_floor = Building.MAILROOM_LOCATION;
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.deliveryCounter = 0;
        robotR = new RegularRobot();
        robotF = new FoodRobot();

    }
    public static void initialiseLockedFloors() {
    	locked_floors = new int[Building.FLOORS];
    }

    public void dispatch() {
    	receivedDispatch = true;
    }
    
    public boolean isEmpty() {
    	boolean empty = false;
    	switch(robot_type) {
	    	case REGULAR:
	    		empty = robotR.isEmpty();
	    		break;
	    	case FOOD:
	    		empty = robotF.isEmpty();
	    		break;
    	}
    	return empty;
    }
    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void operate() throws ExcessiveDeliveryException{
    	switch(robot_type) {
	    	case REGULAR:
	    		robotR.operate(this);
	    		break;
			case FOOD:
				robotF.operate(this);
				break;
    	}
	}
    public void addToLoad(MailItem mailItem) throws ItemTooHeavyException {
    	switch(robot_type) {
	    	case REGULAR:
	    		robotR.addToLoad(mailItem);
	    		break;
			case FOOD:
				robotF.addToLoad(mailItem);
				break;
    	}
    }
    public void registerWaiting() {
    	mailPool.registerWaiting(this);
    }
    public void addToPool(MailItem m) {
    	mailPool.addToPool(m);
    }
    public int getNumber() {
    	return Integer.parseInt(id.substring(1));
    }
    /**
     * Sets the route for the robot
     */
    /*
	private void setDestination() {
		switch(robot_type) {
    	case REGULAR:
    		robotR.setDestination(this);
		case FOOD:
			robotR.setDestination(this);
		}
	}*/
	public void detachFoodTubeAddArmsAndTube() {
		robot_type = RobotType.REGULAR;
	}
	public void detachArmsAndTubeAddFoodTube() {
		robot_type = RobotType.FOOD;
		Statistics.numberOf_robot_type_changes++;
	}
}
