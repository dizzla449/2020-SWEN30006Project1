package automail;

import java.util.ArrayList;
import automail.RobotHandle.RobotState;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import simulation.Building;
import simulation.Clock;
import simulation.Statistics;
import automail.RobotHandle;

public class FoodRobot implements Robot {
	
	private ArrayList<MailItem> tube = new ArrayList<MailItem>(); 
	private int boot_time = 0;
	static public final int MAX_ITEMS = 3;
	
	public FoodRobot(/*IMailDelivery delivery, MailPool mailPool, int number*/) {
		//super(delivery, mailPool, number);
		// TODO Auto-generated constructor stub
	}

	public void operate(RobotHandle rh) throws ExcessiveDeliveryException {   
    	switch(rh.current_state) {
		/** This state is triggered when the robot is returning to the mailroom after a delivery */
			case RETURNING:
				/** If its current position is at the mailroom, then the robot should change state */
	            if(rh.current_floor == Building.MAILROOM_LOCATION){
	            	if (tube.size()  > 0) {
	            		for (MailItem m: tube) {
	            			rh.addToPool(m);
	                        System.out.printf("T: %3d > old addToPool [%s]%n", Clock.Time(), m.toString());
	                        
	            		}     
	                    tube.clear();
	            	}
	    			/** Tell the sorter the robot is ready */
	            	rh.registerWaiting();
	            	rh.detachFoodTubeAddArmsAndTube();
	            	changeState(rh, RobotState.WAITING);
	            } else {
	            	/** If the robot is not at the mailroom floor yet, then move towards it! */
	                moveTowards(rh, Building.MAILROOM_LOCATION);
	            	break;
	            }
			case WAITING:
	            /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
	            if(!isEmpty() && rh.receivedDispatch && boot_time >= 5){
	            	if (RobotHandle.locked_floors[tube.get(0).destination_floor-1]==0) {
		            	rh.receivedDispatch = false;
		            	rh.deliveryCounter = 0; // reset delivery counter
		            	boot_time = 0;
		            	setDestination(rh);
		            	changeState(rh, RobotState.DELIVERING);
	            	}
	            }
	            if (tube.size()>0) { boot_time++;}
	            break;
			case DELIVERING:
				if(rh.current_floor == rh.destination_floor){
					if (RobotHandle.locked_floors[rh.current_floor-1] == rh.getNumber()) {
		                /** Delivery complete, report this to the simulator! */
						Statistics.food_items_delivered++;
						Statistics.total_food_weight_delivered += tube.get(0).weight;
						rh.delivery.deliver(tube.remove(0));
		                RobotHandle.locked_floors[rh.current_floor-1] = 0;
		                rh.deliveryCounter++;
		                if(rh.deliveryCounter > MAX_ITEMS){  // Implies a simulation bug
		                	throw new ExcessiveDeliveryException();
		                }
		                /** Check if want to return, i.e. if there is no item in the tube*/
		                
		                if(tube.size() == 0){
		                	changeState(rh, RobotState.RETURNING);
		                }
		                else {
		                	if (RobotHandle.locked_floors[tube.get(0).destination_floor-1] == 0){
		                	 setDestination(rh);
			                 changeState(rh, RobotState.DELIVERING);
		                	}
		                }
					}
					else if (rh.destination_floor != tube.get(0).destination_floor) {
						if (RobotHandle.locked_floors[tube.get(0).destination_floor-1] == 0) {
							setDestination(rh);
			                changeState(rh, RobotState.DELIVERING);
			                moveTowards(rh, rh.destination_floor);
						}
					}
				}
				else if (RobotHandle.locked_floors[tube.get(0).destination_floor-1] == rh.getNumber()){
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
    	rh.destination_floor = tube.get(0).getDestFloor();
    	RobotHandle.locked_floors[rh.destination_floor-1] = rh.getNumber();
    }
    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    public void moveTowards(RobotHandle rh, int destination) {
    	if(rh.current_floor < destination){
    		rh.current_floor++;
        } else if (rh.current_floor > destination) {
        	rh.current_floor--;
        }
    }
    
    public String getIdTube(RobotHandle rh) {
    	return String.format("%s(%1d)", rh.id, tube.size());
    }
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    public void changeState(RobotHandle rh, RobotState nextState){
    	if (rh.current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(rh), rh.current_state, nextState);
    	}
    	rh.current_state = nextState;
    	if(nextState == RobotState.DELIVERING){
            System.out.printf("T: %3d > %7s-> [%s]%n", Clock.Time(), getIdTube(rh), tube.get(0).toString());
    	}
    }

	//public MailItem getTube() {
	//	return tube.get(0);
	//}
    
	//static private int count = 0;
	//static private Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();

//	@Override
//	public int hashCode() {
//		Integer hash0 = super.hashCode();
//		Integer hash = hashMap.get(hash0);
//		if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
//		return hash;
//	}

	public boolean isEmpty() {
		return !(tube.size()>0);
	}

	public void addToLoad(MailItem mailItem) throws ItemTooHeavyException {
		assert(tube.size() < MAX_ITEMS);
		tube.add(mailItem);
		if (mailItem.weight > RobotHandle.INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}


}
