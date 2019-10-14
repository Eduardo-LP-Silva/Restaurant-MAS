package agents;

import behaviours.ServiceSearch;
import jade.core.AID;
import jade.core.Agent;

public class Customer extends Agent {
    private String desiredDish;
    private AID waiter = null;

    @Override
    protected void setup() {
        System.out.println("Hello! Customer agent " + getAID().getLocalName() + " is ready.");

        Object[] args = getArguments();
        if (args != null && args.length == 1) {
            desiredDish = (String) args[0];
            // standardizing dish name
            desiredDish =  desiredDish.replace("-", " ");
            desiredDish = desiredDish.toLowerCase();
            System.out.println("I want to eat " + desiredDish);


        } else {
            System.out.println("No dish specified!");
            doDelete();
        }

        addBehaviour(new ServiceSearch(this));
    }

    public void setWaiter(AID agent) {
        waiter = agent;
    }

    public AID getWaiter() {
        return waiter;
    }

    @Override
    protected void takeDown() {
        System.out.println("Customer agent " + getAID().getLocalName() + " is terminating.");
    }
}
