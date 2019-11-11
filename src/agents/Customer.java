package agents;

import java.util.Random;

import behaviours.OrderPerformer;
import behaviours.ServiceSearch;
import jade.core.AID;
import jade.core.Agent;

public class Customer extends RestaurantAgent {
    private static final long serialVersionUID = 3921787877132989337L;
    private String desiredDish;
    private AID[] waiters = null;
    private boolean hasStartedOrder = false;
    private int mood;

    @Override
    protected void setup() {
        role = "Customer";

        Object[] args = getArguments();
        
        printMessage("Hello! Customer " + getAID().getLocalName() + " is ready.");

        if (args != null && args.length == 1) {
            desiredDish = (String) args[0];
            // standardizing dish name
            desiredDish =  desiredDish.replace("-", " ");
            desiredDish = desiredDish.toLowerCase();
            printMessage("I want to eat " + desiredDish + "!");

            Random random = new Random();
            mood = random.nextInt(9) + 1; //10 being very relaxed and 1 being very frustrated
        } else {
            printMessage("No dish specified!");
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

    public void startOrder() {
        if(!hasStartedOrder) {
            addBehaviour(new OrderPerformer(this));
            hasStartedOrder = true;
        }
    }

    public void printMessage(String message) {
        System.out.println("(Customer " + getAID().getLocalName() + ") " + message);
    }

    @Override
    protected void takeDown() {
        printMessage("Going home");
    }
}
