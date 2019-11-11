package agents;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import behaviours.OrderPerformer;
import behaviours.ServiceSearch;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

public class Customer extends Agent {
    private static final long serialVersionUID = 3921787877132989337L;
    private String desiredDish;
    private AID[] waiters = null;
    private AID currentWaiter;
    private boolean hasStartedOrder = false;
    private int mood;

    @Override
    protected void setup() {
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
            /*
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            currentWaiter = waiters[0];

            // initial message with the desired dish
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            msg.addReceiver(currentWaiter);
            msg.setConversationId("waiter-request");
            msg.setContent(desiredDish);

            addBehaviour(new OrderPerformer(this, msg));
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
