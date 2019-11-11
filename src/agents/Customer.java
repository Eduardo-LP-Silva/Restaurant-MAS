package agents;

import java.util.HashSet;
import java.util.Random;

import behaviours.OrderPerformer;
import behaviours.ServiceSearch;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

public class Customer extends RestaurantAgent {
    private static final long serialVersionUID = 3921787877132989337L;
    private String desiredDish;
    private AID[] waiters = null;
    private HashSet<AID> unavailableWaiters = new HashSet<>();
    private boolean hasWaiter = false;
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

    public boolean hasWaiter() {
        return hasWaiter;
    }

    public AID getCurrentWaiter() {
        for(int i=0; i<waiters.length; i++) {
            if(!unavailableWaiters.contains(waiters[i])) {
                return waiters[i];
            }
        }
        return null;
    }

    public void getAvailableWaiter() {
        AID currentWaiter = getCurrentWaiter();

        if(currentWaiter == null) {
            printMessage("There isn't an available waiter :(");
            doDelete();
            return;
        }

        // get an available waiter
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        msg.addReceiver(currentWaiter);
        msg.setConversationId("waiter-request");
        msg.setContent("Be my waiter " + currentWaiter.getLocalName());

        printMessage("Are you available, " + currentWaiter.getLocalName() + "?");

        addBehaviour(new SimpleAchieveREInitiator(this, msg) {
            @Override
            protected void handleInform(ACLMessage inform) {
                hasWaiter = true;
            }

            @Override
            protected void handleRefuse(ACLMessage msg) {
                unavailableWaiters.add(currentWaiter);
            }
        });
    }

    @Override
    protected void takeDown() {
        printMessage("Going home");
    }
}
