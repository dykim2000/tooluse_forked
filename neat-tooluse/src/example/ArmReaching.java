package example;

import example.ExampleFitnessFunction.StickState;

public class ArmReaching {
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param len1 Length of the first arm segment
	 * @param len2 Length of the second arm segment
	 * @param tool1_len Length of the first tool
	 * @param tool2_len Length of the second tool
	 * @param stick_state The number of sticks currently being used
	 */
	public ArmReaching( int x, int y, int len1, int len2, int tool1_len, int tool2_len, StickState stick_state) {
		bx = x;
		by = y;
		l1 = len1;
		l2 = len2;
		l3 = tool1_len;
		l4 = tool2_len;
		hand_size = 10; //3
		
		pick_x = bx + l1 + l2;
		pick_y = by;
		drop_x = bx - l1 - l2;
		drop_y = by;
		real_l2 = l2;
		holding = false;//The arm begins without holding tool1 or tool2
		holding2 = false;
		stickState = stick_state;
		
		resetArm();
		resetTarget(stickState);
	}
	
	public void resetArm() {
	//reset the arm position with random angles for the joint 1 and 2
	//the joint angle will be between -90 and 90
		
		theta1 = 180 * (Math.random() - 0.5);
		theta2 = 180 * (Math.random() - 0.5);
		holding = false;
		holding2 = false;
		
		setArmConfiguration();
	}
	
	private void setArmConfiguration() {
	//set the arm configuration according to the current joints angles
		
		if(theta1 > 150) theta1 = 150.0;
		//if(theta2 > 90) theta2 = 90.0;
		if(theta2 > 150) theta2 = 150.0;
		
		if(theta1 < -150) theta1 = -150.0;
		//if(theta2 < -90) theta2 = -90.0;
		if(theta2 < -150) theta2 = -150.0;
//		if( theta1 < 0 ) theta1 += 360;
//		if( theta1 > 360 ) theta1 -= 360;
//		if( theta2 < 0 ) theta1 += 360;
//		if( theta2 > 360 ) theta1 -= 360;
//		System.out.println("theta1: " + theta1 + " theta2: " + theta2);
		
//		mx = bx + (int)(Math.sin(Math.PI*theta1/180)*l1);
//		my = by - (int)(Math.cos(Math.PI*theta1/180)*l1);
//		ex = mx + (int)(Math.cos(Math.PI*theta2/180)*l2);
//		ey = my - (int)(Math.sin(Math.PI*theta2/180)*l2);
		
		mx = bx + (int)(Math.sin(Math.PI*theta1/180)*l1);
		my = by - (int)(Math.cos(Math.PI*theta1/180)*l1);
		int arm2 = l2;
		if( holding ) arm2 += l3;
		if( holding2 ) arm2 += l4;
		ex = mx + (int)(Math.sin(Math.PI*theta2/180)*arm2);
		ey = my - (int)(Math.cos(Math.PI*theta2/180)*arm2);
	}
	
	//public double move( double angle1, double angle2 ) {
	public void move( double angle1, double angle2 ) {
		//double fitness = 0.0;
		
		theta1 += angle1;
		theta2 += angle2;
//		if(theta1 > 360) theta1 -= 360;
//		if(theta2 > 360) theta2 -= 360;
//		if(theta1 < -360) theta1 += 360;
//		if(theta2 < -360) theta2 += 360;
		

		setArmConfiguration();
		
		//Check if the arm is in position to pick up tool 1
		if(stickState != StickState.NO_STICK && isPick(1) ) {
			pick(1);
		}
		
		//Check if the arm is in position to pick up tool 2
		//Tool 2 can only be picked up if tool 2 is already picked up.
		if(stickState == StickState.TWO_STICKS && holding && isPick(2) ) {			
			pick(2);		
		}
		
//		if( isDrop() ) {
//			drop();
//		}
		
//		if( hasReach() )
//		{
//			//System.out.println("GOT IT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//			//fitness = 2;
//			fitness = 2;
//			//fitness = 300000.0;
//			resetTarget();
//		} 
//		//else if(theta1 == 90 || theta1 == -90 || theta2 == 90 || theta2 == -90) {
//			//fitness = 0;
//		//} 
//		else {
//			//double d = Math.pow((ex-tx), 2) + Math.pow((ey-ty), 2);
//			//fitness = 1/Math.sqrt(d) * 10000;
//		}
		//System.out.println("fitness: " + (int)fitness);
		//return fitness;
	}
	
	public double getDist2Target() {
		double d = 0.0;

		d = Math.pow((ex-tx), 2) + Math.pow((ey-ty), 2);
		d = Math.sqrt(d);

		return d;
	}

	
	public void resetTarget(StickState stickState) {
		int maxReach = l1 + l2;          // no stick
		if (stickState == StickState.ONE_STICK) maxReach += l3;
		if (stickState == StickState.TWO_STICKS) maxReach += l3 + l4;
		
		int minReach = Math.abs(l2 - l1);   // same as before for no stick
		if (stickState == StickState.ONE_STICK) minReach += l3;   // arm + stick 1 cannot bend closer than this
		if (stickState == StickState.TWO_STICKS) minReach += l3 + l4; // arm + both sticks



	    boolean valid = false;
	    while (!valid) {
	        int a = (int)(2 * maxReach * Math.random()) - maxReach;
	        int b = (int)(2 * maxReach * Math.random()) - maxReach;
	        tx = bx + a;
	        ty = by + b;

	        int distSq = (tx - bx)*(tx - bx) + (ty - by)*(ty - by);

	        // Test if random placement is within the minimum to maximum distance range
	        if (distSq > minReach * minReach && distSq < maxReach*maxReach) {
	           valid = true;
	        }

	        // no target on baseline
	        if (by - ty < 10)
	            valid = false;
	    }
	}
	
	public void resetTool() {
		boolean valid = false;
		int x = 0;
		int y = 0;
		while( !valid )
		{
			int a = (int)(2*(l1+l2)*Math.random())-(l1+l2);
			int b = (int)(2*(l1+l2)*Math.random())-(l1+l2);
			x = bx + a;
			y = by + b;
			if( (x-bx)*(x-bx) + (y-by)*(y-by) < (l1+l2)*(l1+l2) ) { // reachable
				if( l2 > l1 && ((tx-bx)*(tx-bx) + (ty-by)*(ty-by) > (l2 - l1)*(l2 - l1)) )
					valid = true;
			}
			if( by - y < 20 ) // no target on baseline
				valid = false;
		}
		//pick_x = bx + l1 + l2-50;
		//pick_y = by+30;
		pick_x = x;
		pick_y = y;
	}
	
	/**
	 * Find a new, random, valid location to place tool2
	 */
	public void resetTool2() {
		boolean valid = false;
		int x = 0;
		int y = 0;
		int lsum = l1 + l2 + l3;
		int lmax = (l1 > l2) ? ((l1 > l3) ? l1: l3)
				: ((l2 > l3) ? l2: l3);//Find max(l1, l2, l3)

		int rmin = (lmax > (lsum - lmax)) ? (lmax - (lsum - lmax)) : 0;
		
		while( !valid )//Continue picking random new locations until a valid reset position is found
		{
			int a = (int)(2*lsum*Math.random())-lsum;
			int b = (int)(2*lsum*Math.random())-lsum;
			x = bx + a;
			y = by + b;
			int distSquared = (x-bx)*(x-bx) + (y-by)*(y-by);
			if(distSquared  < lsum*lsum ) { // Tool2 must be reachable using at maximum the arm and the first tool
				
				if(distSquared > rmin*rmin) // Tool2 must be reachable outside of the minimum reachable distance from the arm and tool1 together.
					
					valid = true;
			}
			if(Math.abs(pick_x - x) < 10 && Math.abs(pick_y - y) < 10) //Tool1 and Tool2 cannot be in the same exact location
				valid = false;
			
			if( by - y < 20 ) // no target on baseline
				valid = false;
		}
		pick2_x = x;
		pick2_y = y;
	}
	
	public void setTarget(int x, int y) {
		tx = x; 
		ty = y;
	}
	
	public boolean hasReach() {
//		int x = tx + 3;
//		int y = ty + 3;
		int x = tx;
		int y = ty;
		
		if( Math.abs(ex-x) < hand_size && Math.abs(ey-y) < hand_size )
			return true;
		return false;
	}
	
	/**
	 * Finds if the arm is in position to pick up a tool
	 * @param toolNum Choose which tool to test pickup for. 1 = tool1, 2 = tool2.
	 * @return true if arm is in correct position to pick
	 */
	public boolean isPick(int toolNum) {
		int x,y;
		if(toolNum == 2) {//Tool 2
			if(holding2) return false; //If tool 2 is already picked up, it's not valid to be picked up again
			x = pick2_x;
			y = pick2_y;
		} else { //Tool 1
			if(holding) return false; //If tool 1 is already picked up, it's not valid to be picked up again
			x = pick_x;
			y = pick_y;
		}
		
		if( Math.abs(ex-x) < hand_size && Math.abs(ey-y) < hand_size )
			return true;
		return false;
	}
	
	/**
	 * Marks a tool as being picked up
	 * @param toolNum Choose which tool to pick up. 1 = tool1, 2 = tool2.
	 */
	public void pick(int toolNum) {
		if(toolNum == 1) {
			holding = true;
		}
		if(toolNum == 2) {
			holding2 = true;
		}
	}
	
	public boolean isDrop() {
		int x = drop_x;
		int y = drop_y;
		
		if( Math.abs(ex-x) < hand_size && Math.abs(ey-y) < hand_size )
			return true;
		return false;
	}
	
	public void drop() {
		holding = false;
	}

	public int bx, by;
	public int mx, my;
	public int ex, ey;
	public int tx, ty;
	public int l1, l2, l3, l4; // l1 = length of first arm segment, l2 = length of second arm segment, l3 = len of tool1, l4 = len of tool2
	public int hand_size; //radius of the hand size
	public double theta1, theta2;
	public final int box_len = 400;
	public final int box_wid = 400;
	public int pick_x, pick_y; //Position of tool1
	public int pick2_x, pick2_y; //Position of tool2
	public int drop_x, drop_y;
	public int real_l2;
	boolean holding, holding2; //Indicates whether tool1 or tool2 is currently being held
	public StickState stickState;
}
