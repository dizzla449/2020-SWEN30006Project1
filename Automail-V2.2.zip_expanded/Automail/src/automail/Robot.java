package automail;

import automail.RobotHandle.RobotState;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;

public interface Robot {
	
    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void operate(RobotHandle r) throws ExcessiveDeliveryException;
    /**
     * Sets the route for the robot
     */
	public void setDestination(RobotHandle r);
    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    public void moveTowards(RobotHandle rh, int destination);
    
    public String getIdTube(RobotHandle rh);
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    public void changeState(RobotHandle rh, RobotState nextState);
    
    public void addToLoad(MailItem mailItem) throws ItemTooHeavyException;

	//public abstract MailItem getTube();

	public boolean isEmpty();
}
