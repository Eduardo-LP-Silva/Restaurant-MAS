package agents;

import java.util.Random;

import behaviours.ServiceSearch;
import jade.core.AID;
import jade.core.Agent;

public class Customer extends Agent {
    private static final long serialVersionUID = 3921787877132989337L;
    private String desiredDish;
    private AID[] waiters = null;
    private int maxWaitingTime;
    private int minMealQuality;
    private boolean gotDesiredMeal = false;

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
            maxWaitingTime = random.nextInt(105) + 15;
            minMealQuality = random.nextInt(9) + 1;
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
