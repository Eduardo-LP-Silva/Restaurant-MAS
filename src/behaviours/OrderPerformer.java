package behaviours;

import agents.Customer;
import jade.core.AID;
import jade.core.behaviours.BaseInitiator;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;

import java.util.concurrent.TimeUnit;

public class OrderPerformer extends BaseInitiator {
    private static final long serialVersionUID = 2897989135282380056L;
    private Customer customer;
    private AID currentWaiter;

    public OrderPerformer(Customer c) {
        customer = c;
        currentWaiter = customer.getFirstWaiter();
    }

    @Override
    protected void handleAgree(ACLMessage agree) {
        customer.printMessage("Waiting for my dish.");
    }

    @Override
    protected ACLMessage createInitiation() {
        /*
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(currentWaiter);
        msg.setConversationId("waiter-request");
        msg.setContent(customer.getDesiredDish());
        customer.send(msg);
        return msg;
    }
}
