import java.io.*;
import java.util.Arrays;

/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */ 
public class Node { 
    
    public static final int INFINITY = 9999;
    
    int[] lkcost;		/*The link cost between this node and other nodes*/
    int[][] costs;  		/*Define distance table*/
    int nodename;               /*Name of this node*/
    
    int[] currentMinCost; /*mincost array used to send in packets*/
    
    /* Class constructor */
    public Node() { }
    
    /* students to write the following two routines, and maybe some others */
    void rtinit(int nodename, int[] initial_lkcost) { 
    	// Initialize lkcost by copying the parameter's
    	this.lkcost = new int[4];
    	this.lkcost = initial_lkcost.clone();
    	
    	// Initialize the mincost to be lkcost for now in order to be used in future comparisons
    	this.currentMinCost = new int[4];
    	this.currentMinCost = this.lkcost.clone();
    	
    	// Initialize the nodename using given nodename parameter
    	this.nodename = nodename;
    	
    	// Initialize the costs "Distance Table" by using the lkcosts and INFINITY
    	this.costs = new int[4][4]; // distance table
    	for (int i = 0; i < 4; i++) {
    		for (int j = 0; j < 4; j++) {
    			// If the dest node i is the same as the lkcost j set it to that
    			if (i == j) {
    				this.costs[i][j] = this.lkcost[j];
    			} else { // if it doesn't correspond, then just set it to infinity as initial for now
    				this.costs[i][j] = INFINITY;    				
    			}
    		}
    	}
    	System.out.println("Node " + this.nodename + " is being initialized. Distance table:");
    	printdt();
    	
    	/* Now we need to send initial update packets to all neighboring nodes
    	 * Check that it is a neighboring node if the link cost in lkcost is not itself / infinity
    	 */
    	for (int i = 0; i < 4; i++) {
    		if (i != this.nodename && this.lkcost[i] != INFINITY) {
    			Packet updatePacket = new Packet(this.nodename, i, this.lkcost);
    			System.out.println(this.nodename + " is sending an initial mincost to neighbor " + i);
    			NetworkSimulator.tolayer2(updatePacket);
    		}
    	}
    }    
    
    void rtupdate(Packet rcvdpkt) {  
    	int newPath; // used to keep track of each iteration of new min path for comparison
    	int[] newMinCost = new int[4];
    	int minimum;
    	
    	// Update the current node's distance table with the new path received within the packet
    	for (int i = 0; i < 4; i++) {
    		// set the new temp path of going through the received packet's node's mincosts for comparison
    		newPath = this.lkcost[rcvdpkt.sourceid] + rcvdpkt.mincost[i];
    		// if the new path is not infinity -- meaning reachable, and its not already there - then update the distance table
    		if (newPath < INFINITY && newPath != this.costs[i][rcvdpkt.sourceid]) {
    			this.costs[i][rcvdpkt.sourceid] = newPath;
    		}
    	}
    	System.out.println("Distance Table for node " + this.nodename + " updated:");
    	printdt();
    	
    	// Now that the distance table is updated, check to see if the current node's mincost array is changed
    	// First create the new temp mincost table to be compared with the original
    	for (int j = 0; j < 4; j++) {
    		minimum = this.currentMinCost[j];
    		for (int k = 0; k < 4; k++) {
    			if (this.costs[j][k] < minimum) {
    				minimum = this.costs[j][k];
    			}
    		}
    		newMinCost[j] = minimum;
    	}
    	
    	// If the change in distance table has truly changed the node's mincost, then send an update packet to neighbors
    	if (!(Arrays.equals(this.currentMinCost, newMinCost))) {
    		this.currentMinCost = newMinCost;
    		// send a new packet using my own helper function!
    		sendUpdate();
    	}
    }
    
    /* I made this helper function to just condense code and make it easier to see. I am using this to send new update packet
     * after seeing that we have updated the mincost[] of the node after receiving a packet.
     */
    void sendUpdate() {
    	int[] poisonedMin = new int[4];
    	for (int i = 0; i < 4; i++) {
    		poisonedMin = this.currentMinCost.clone();
    		// only send to neighbors this new update packet
    		if (i != this.nodename && this.lkcost[i] != INFINITY) {
    			// we must check if we must lie to the neighboring node about any distances - using Poisoned Reverse
    			// We do this by checking whether any changed mincost value is because of a route through neighbor node
    			for (int j = 0; j < 4; j++) {
    				// if any of the four min cost routes are routing through current neighbor, set those routes to infinity
    				if (this.lkcost[j] != this.currentMinCost[j] && this.currentMinCost[j] == this.costs[j][i]) {
    					poisonedMin[j] = INFINITY;
    				}
    			}
    			// After figuring out whether the mincost should be poisoned, send the final version to the neighbor
    			// poisonedMin can either be the original mincost or it can be actually poisoned based on the above if statement
    			Packet updatepkt = new Packet(this.nodename, i, poisonedMin);
    			System.out.println(this.nodename + " is sending updated mincost to " + i + " after receiving update");
    			NetworkSimulator.tolayer2(updatepkt);
    		}
    	}
    }
    
    /* called when cost from the node to linkid changes from current value to newcost*/
    void linkhandler(int linkid, int newcost) {  }    


    /* Prints the current costs to reaching other nodes in the network */
    void printdt() {
        switch(nodename) {
	
	case 0:
	    System.out.printf("                via     \n");
	    System.out.printf("   D0 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     1|  %3d   %3d \n",costs[1][1], costs[1][2]);
	    System.out.printf("dest 2|  %3d   %3d \n",costs[2][1], costs[2][2]);
	    System.out.printf("     3|  %3d   %3d \n",costs[3][1], costs[3][2]);
	    break;
	case 1:
	    System.out.printf("                via     \n");
	    System.out.printf("   D1 |    0     2    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][2],costs[0][3]);
	    System.out.printf("dest 2|  %3d   %3d   %3d\n",costs[2][0], costs[2][2],costs[2][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][2],costs[3][3]);
	    break;    
	case 2:
	    System.out.printf("                via     \n");
	    System.out.printf("   D2 |    0     1    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][1],costs[0][3]);
	    System.out.printf("dest 1|  %3d   %3d   %3d\n",costs[1][0], costs[1][1],costs[1][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][1],costs[3][3]);
	    break;
	case 3:
	    System.out.printf("                via     \n");
	    System.out.printf("   D3 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d\n",costs[0][1],costs[0][2]);
	    System.out.printf("dest 1|  %3d   %3d\n",costs[1][1],costs[1][2]);
	    System.out.printf("     2|  %3d   %3d\n",costs[2][1],costs[2][2]);
	    break;
        }
    }
    
}
