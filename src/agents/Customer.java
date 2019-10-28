package agents;

import java.util.Random;

import behaviours.OrderPerformer;
import behaviours.ServiceSearch;
import jade.core.AID;
import jade.core.Agent;

public class Customer extends Agent {
    private static final long serialVersionUID = 3921787877132989337L;
    private String desiredDish;
    private AID[] waiters = null;
    private int mood;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        
        System.out.println("(customer) Hello! Customer " + getAID().getLocalName() + " is ready.");

        if (args != null && args.length == 1) {
            desiredDish = (String) args[0];
            // standardizing dish name
            desiredDish =  desiredDish.replace("-", " ");
            desiredDish = desiredDish.toLowerCase();
            System.out.println("(customer) I want to eat " + desiredDish + "!");

            Random random = new Random();
            mood = random.nextInt(9) + 1; //10 being very relaxed and 1 being very frustrated
        } else {
            System.out.println("(customer) No dish specified!");
            doDelete();
        }

        addBehaviour(new ServiceSearch(this, 1000));
    }

    public void setWaiters(AID[] agents) {
        waiters = agents;
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

    public void startOrder() {
        // addBehaviour(new OrderPerformer(this));
    }
}
