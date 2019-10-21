package agents;

import behaviours.ServiceSearch;
import jade.core.AID;
import jade.core.Agent;

public class Customer extends Agent {
    private static final long serialVersionUID = 3921787877132989337L;
    private String desiredDish;
    private AID[] waiters = null;

    @Override
    protected void setup() {
        System.out.println("(customer) Hello! Customer " + getAID().getLocalName() + " is ready.");

        Object[] args = getArguments();
        if (args != null && args.length == 1) {
            desiredDish = (String) args[0];
            // standardizing dish name
            desiredDish =  desiredDish.replace("-", " ");
            desiredDish = desiredDish.toLowerCase();
            System.out.println("(customer) I want to eat " + desiredDish + "!");


        } else {
            System.out.println("(customer) No dish specified!");
            doDelete();
        }

        addBehaviour(new ServiceSearch(this));
    }

    public void setWaiters(AID[] agents) {
        waiters = agents;
    }

    public AID[] getWaiters() {
        return waiters;
    }

    public AID getFirstWaiter() {
        return waiters[0];
    }

    public String getDesiredDish() {
        return desiredDish;
    }

    @Override
    protected void takeDown() {
        System.out.println("(customer) Customer " + getAID().getLocalName() + " is going home.");
    }
}
