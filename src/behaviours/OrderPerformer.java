package behaviours;

import agents.Customer;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

public class OrderPerformer extends ContractNetInitiator {
    private static final long serialVersionUID = 2897989135282380056L;
    private Customer customer;

    public OrderPerformer(Customer c, ACLMessage cfp) {
        super(c, cfp);
        customer = c;
    }

    @Override
    protected void handleOutOfSequence(ACLMessage msg) {
        switch(msg.getPerformative()) {
            case ACLMessage.REFUSE:
                handleRefuse(msg);
                break;
        }
    }

    @Override
    protected void handleRefuse(ACLMessage msg) {
        if (customer.getAttempts() >= 3) {
            customer.printMessage("I've tried enough, leaving now...");
            customer.doDelete();
        }
        else {
            customer.printMessage("Okay, I'll take another look in the menu.");
            customer.incrementAttempts();
            customer.orderDish();
        }
    }

}
