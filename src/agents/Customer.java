package agents;

import java.util.*;
import java.util.concurrent.TimeUnit;

import behaviours.OrderPerformer;
import behaviours.ServiceSearch;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

public class Customer extends RestaurantAgent {
    private static final long serialVersionUID = 3921787877132989337L;
    private String desiredDish;
    private HashSet<AID> unavailableWaiters = new HashSet<>();
    private boolean hasWaiter;
    private AID waiter;
    private int mood;
    private int attempts;

    @Override
    protected void setup() {
        role = "Customer";
        
        printMessage("Hello! Customer " + getAID().getLocalName() + " is ready.");

        Random random = new Random();
        mood = random.nextInt(9) + 1; //10 being very relaxed and 1 being very frustrated

        hasWaiter = false;
        attempts = 0;
        desiredDish = null;
        
        addBehaviour(new ServiceSearch(this, 1000));
    }

    @Override
    public void addWaiters(AID[] newWaiters) {
        this.setWaiters(new ArrayList<>(Arrays.asList(newWaiters)));

        if(!this.hasWaiter()) {
            this.getAvailableWaiter();
        }
    }

    private void setWaiters(ArrayList<AID> agents) {
        waiters = agents;
    }

    private boolean hasWaiter() {
        return hasWaiter;
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        attempts++;
    }

    private AID getCurrentWaiter() {
        for (AID aid : waiters) {
            if (!unavailableWaiters.contains(aid)) {
                return aid;
            }
        }
        return null;
    }

    // Step 0: Find an available waiter (corresponds to waiter's step 0)
    private void getAvailableWaiter() {
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
                waiter = currentWaiter;
                orderDish();
            }

            @Override
            protected void handleRefuse(ACLMessage msg) {
                unavailableWaiters.add(currentWaiter);
            }
        });
    }

    private void decideDish() {
        String oldDish = desiredDish;
        String[] dishes = Kitchen.getMenu();
        Random rand = new Random();

        desiredDish = dishes[rand.nextInt(dishes.length)];
        while(oldDish == desiredDish) {
            desiredDish = dishes[rand.nextInt(dishes.length)];
        }
    }

    // Step 1: Order dish (corresponds to waiter's step 1)
    public void orderDish() {
        // decideDish();
        // For testing purposes
        desiredDish = "lolada";

        printMessage("I would like to eat " + desiredDish + ".");
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.addReceiver(waiter);
        msg.setConversationId("order-request");
        msg.setContent(desiredDish + " " + mood); // Message: <Dish Mood>

        addBehaviour(new OrderPerformer(this, msg));
    }

    @Override
    protected void takeDown() {
        printMessage("Going home");
    }

}
