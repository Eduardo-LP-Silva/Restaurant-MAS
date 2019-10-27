package behaviours;

import agents.Customer;
import jade.core.AID;
import jade.core.behaviours.BaseInitiator;
import jade.lang.acl.ACLMessage;

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
        System.out.println("(customer) Waiting for my dish.");
    }

    @Override
    protected ACLMessage createInitiation() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(currentWaiter);
        msg.setLanguage("English");
        msg.setConversationId("waiter-request");
        msg.setContent(customer.getDesiredDish());
        customer.send(msg);
        return msg;
    }
}
