package automail;

import automail.RobotHandle.RobotState;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import simulation.Building;
import simulation.Clock;
import simulation.Statistics;

public class RegularRobot implements Robot {
	
    
    private MailItem deliveryItem = null;
    private MailItem tube = null;
    static public final int MAX_ITEMS = 2;

	public RegularRobot(/*IMailDelivery delivery, MailPool mailPool, int number*/) {
		//super(delivery, mailPool, number);
		// TODO Auto-generated constructor stub
	}

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void operate(RobotHandle rh) throws ExcessiveDeliveryException { 
	    	switch(rh.current_state) {
	    		/** This state is triggered when the robot is returning to the mailroom after a delivery */
	    		case RETURNING:
	    			/** If its current position is at the mailroom, then the robot should change state */
	                if(rh.current_floor == Building.MAILROOM_LOCATION){
	                	if (tube != null) {
	                		rh.addToPool(tube);
	                        System.out.printf("T: %3d > old addToPool [%s]%n", Clock.Time(), tube.toString());
	                        tube = null;
	                	}
	        			/** Tell the sorter the robot is ready */
	                	rh.registerWaiting();
	                	changeState(rh, RobotState.WAITING);
	                } else {
	                	/** If the robot is not at the mailroom floor yet, then move towards it! */
	                    moveTowards(rh, Building.MAILROOM_LOCATION);
	                	break;
	                }
	    		case WAITING:
	                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
	                if(!isEmpty() && rh.receivedDispatch){
	                	rh.receivedDispatch = false;
	                	rh.deliveryCounter = 0; // reset delivery counter
	                	setDestination(rh);
	                	changeState(rh, RobotState.DELIVERING);
	                }
	                break;
	    		case DELIVERING:
	    			if(rh.current_floor == rh.destination_floor && RobotHandle.locked_floors[rh.current_floor-1]==0){ // If already here drop off either way
	    				/** Delivery complete, report this to the simulator! */
	    				rh.delivery.deliver(deliveryItem);
	    				Statistics.regular_items_delivered++;
	    				Statistics.total_regular_weight_delivered += deliveryItem.weight;
	                    deliveryItem = null;
	                    rh.deliveryCounter++;
	                    if(rh.deliveryCounter > MAX_ITEMS){  // Implies a simulation bug
	                    	throw new ExcessiveDeliveryException();
	                    }
	                    /** Check if want to return, i.e. if there is no item in the tube*/
	                    if(tube == null){
	                    	changeState(rh,RobotState.RETURNING);
	                    }
	                    else{
	                        /** If there is another item, set the robot's route to the location to deliver the item */
	                        deliveryItem = tube;
	                        tube = null;
	                        setDestination(rh);
	                        changeState(rh, RobotState.DELIVERING);
	                    }
	    			} else {
		        		/** The robot is not at the destination, move towards it or robot is waiting for locked floor */
		                moveTowards(rh, rh.destination_floor);
	    			}
	                break;
	    	}
    }
    /**
     * Sets the route for the robot
     */
    public void setDestination(RobotHandle rh) {
        /** Set the destination floor */
    	if (deliveryItem != null) {
    		rh.destination_floor = deliveryItem.destination_floor;
    	} else if (tube != null){
    		rh.destination_floor = tube.destination_floor;
    	}
    }
    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    public void moveTowards(RobotHandle rh, int destination) {
        if (rh.current_floor < destination){
            rh.current_floor++;
        } else if (rh.current_floor > destination) {
            rh.current_floor--;
        }
    }
    
    public String getIdTube(RobotHandle rh) {
    	return String.format("%s(%1d)", rh.id, (tube == null ? 0 : 1));
    }
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    public void changeState(RobotHandle rh, RobotState nextState){
    	assert(!(deliveryItem == null && tube != null));
    	if (rh.current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(rh), rh.current_state, nextState);
    	}
    	rh.current_state = nextState;
    	if(nextState == RobotState.DELIVERING){
            System.out.printf("T: %3d > %7s-> [%s]%n", Clock.Time(), getIdTube(rh), deliveryItem.toString());
    	}
    }

	//public MailItem getTube() {
	//	return tube;
	//}
    
//	static private int count = 0;
//	static private Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();
//	@Override
//	public int hashCode() {
//		Integer hash0 = super.hashCode();
//		Integer hash = hashMap.get(hash0);
//		if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
//		return hash;
//	}

	public boolean isEmpty() {
		return (deliveryItem == null && tube == null);
	}
	
	public void addToLoad(MailItem mailItem) throws ItemTooHeavyException {
		if (deliveryItem == null) {
			deliveryItem = mailItem;
			if (deliveryItem.weight > RobotHandle.INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
		}
		else if (tube == null) {
			tube = mailItem;
			if (tube.weight > RobotHandle.INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
		}
	}
/*
	public void addToHand(MailItem mailItem) throws ItemTooHeavyException {
		assert(deliveryItem == null);
		deliveryItem = mailItem;
		if (deliveryItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}

	public void addToTube(MailItem mailItem) throws ItemTooHeavyException {
		assert(tube == null);
		tube = mailItem;
		if (tube.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}*/
}
