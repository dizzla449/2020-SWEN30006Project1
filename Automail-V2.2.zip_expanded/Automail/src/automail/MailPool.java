package automail;

import java.util.LinkedList;
import java.util.Comparator;
import java.util.ListIterator;

import automail.RobotHandle.RobotType;
import exceptions.ItemTooHeavyException;
import simulation.PriorityMailItem;

/**
 * addToPool is called when there are mail items newly arrived at the building to add to the MailPool or
 * if a robot returns with some undelivered items - these are added back to the MailPool.
 * The data structure and algorithms used in the MailPool is your choice.
 * 
 */
public class MailPool {

	private class Item {
		int priority;
		int destination;
		MailItem mailItem;
		// Use stable sort to keep arrival time relative positions
		
		public Item(MailItem mailItem) {
			priority = (mailItem instanceof PriorityMailItem) ? ((PriorityMailItem) mailItem).getPriorityLevel() : 1;
			destination = mailItem.getDestFloor();
			this.mailItem = mailItem;
		}
	}
	
	public class ItemComparator implements Comparator<Item> {
		@Override
		public int compare(Item i1, Item i2) {
			int order = 0;
			if (i1.priority < i2.priority) {
				order = 1;
			} else if (i1.priority > i2.priority) {
				order = -1;
			} else if (i1.destination < i2.destination) {
				order = 1;
			} else if (i1.destination > i2.destination) {
				order = -1;
			}
			return order;
		}
	}
	
	private LinkedList<Item> regular_pool;
	private LinkedList<Item> food_pool;
	private LinkedList<RobotHandle> robots;
	

	public MailPool(int nrobots){
		// Start empty
		regular_pool = new LinkedList<Item>();
		food_pool = new LinkedList<Item>();
		robots = new LinkedList<RobotHandle>();
	}

	/**
     * Adds an item to the mail pool
     * @param mailItem the mail item being added.
     */
	public void addToPool(MailItem mailItem) {
		Item item = new Item(mailItem);
		
		switch(mailItem.getMailType()) {
			case REGULAR:
				regular_pool.add(item);
				regular_pool.sort(new ItemComparator());
				break;
			case FOOD:
				food_pool.add(item);
				food_pool.sort(new ItemComparator());
				break;
		}
	}

	/**
     * load up any waiting robots with mailItems, if any.
     */
	public void loadItemsToRobot() throws ItemTooHeavyException {
		//List available robots
		ListIterator<RobotHandle> i = robots.listIterator();
		//System.out.format("ROBOT AVALIABLE =  %d\n", robots.size());
		//System.out.printf("REGULAR MAIL POOL SIZE TO START: %3d%n", regular_pool.size());
		while (i.hasNext() && (regular_pool.size() > 0 | food_pool.size()>0)) {
			loadRegularMailItem(i); loadFoodMailItem(i);
		}
		while (i.hasNext()) { i.next();}
	}
	
	//load items to the robot
	private void loadRegularMailItem(ListIterator<RobotHandle> i) throws ItemTooHeavyException {
		ListIterator<Item> j = regular_pool.listIterator();
		if (regular_pool.size() > 0) {
			RobotHandle robot = i.next();
			assert(robot.robot_type == RobotType.REGULAR);
			assert(robot.isEmpty());
			try {
				for (int k=0; k < RegularRobot.MAX_ITEMS && regular_pool.size() > 0; k++) {
					robot.addToLoad(j.next().mailItem);
					//System.out.printf("%7s assigned item %n", robot.robotR.getIdTube(robot));
					j.remove();
				}
				robot.dispatch();
				i.remove();
			} catch (Exception e) { 
	            throw e; 
	        }
		}
	}
	private void loadFoodMailItem(ListIterator<RobotHandle> i) throws ItemTooHeavyException {
		ListIterator<Item> j = food_pool.listIterator();
		if (food_pool.size() > 0 && i.hasNext()) {
			RobotHandle robot = i.next();
			robot.detachArmsAndTubeAddFoodTube();
			assert(robot.isEmpty());
			try {
				for (int k=0; k < FoodRobot.MAX_ITEMS && food_pool.size() > 0; k++) {
					robot.addToLoad(j.next().mailItem); // hand first as we want higher priority delivered first
					j.remove();
				}
				robot.dispatch(); // send the robot off if it has any items to deliver
				i.remove();       // remove from mailPool queue
			} catch (Exception e) { 
	            throw e; 
	        }
		}
	}
	/**
     * @param robot refers to a robot which has arrived back ready for more mailItems to deliver
     */	
	public void registerWaiting(RobotHandle robot) { // assumes won't be there already
		robots.add(robot);
	}
	public int getsize() {
		return regular_pool.size()+food_pool.size();
	}

}
