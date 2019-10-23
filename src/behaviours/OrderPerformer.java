package behaviours;

import agents.Customer;
import jade.core.AID;
import jade.core.behaviours.BaseInitiator;
import jade.lang.acl.ACLMessage;

public class OrderPerformer extends BaseInitiator {
    private Customer customer;
    private AID currentWaiter;

    OrderPerformer(Customer c) {
        customer = c;
        currentWaiter = customer.getFirstWaiter();
    }

    @Override
    protected ACLMessage createInitiation() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(currentWaiter);
        msg.setLanguage("English");
        msg.setConversationId("order-request");
        msg.setContent(customer.getDesiredDish());
        customer.send(msg);
        return msg;
    }
}
