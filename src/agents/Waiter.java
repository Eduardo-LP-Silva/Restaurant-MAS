package agents;

import jade.core.Agent;
import utils.Kitchen;

public class Waiter extends Agent 
{
    private static Kitchen kitchen;
    //List of waiters ?
    private static final long serialVersionUID = 7110642579660810600L;

    protected void setup() {        
        System.out.println("Waiter " + this.getAID().getName() + " at your service.");

        if(kitchen == null)
            kitchen = new Kitchen();
        
        //Wait for client requests
    }

    protected void takeDown() {
        System.out.println("Waiter " + this.getAID().getName() + " going home.");
    }
}