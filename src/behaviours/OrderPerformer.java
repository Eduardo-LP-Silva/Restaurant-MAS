package behaviours;

import agents.Customer;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

import java.util.Random;

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

            case ACLMessage.PROPOSE:
                handlePropose(msg);
                break;
        }
    }

    // The dish ordered isn't available
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

    // The waiter proposes a dish (it could be the same I suggested if he agrees that it is a good choice)
    private void handlePropose(ACLMessage propose) {
        String proposedDish = propose.getContent().split(" - ")[0];
        if(!proposedDish.equals(customer.getDesiredDish())) {
            Random rand = new Random();
            int accept = rand.nextInt(1);
            if(accept == 0) {
                customer.sendMessage(propose.getSender(), ACLMessage.REJECT_PROPOSAL, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, "dish-feedback", "no");
                this.reset();
                customer.incrementAttempts();
                customer.orderDish();
            }
            else {
                customer.sendMessage(propose.getSender(), ACLMessage.ACCEPT_PROPOSAL, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, "dish-feedback", proposedDish);
                customer.printMessage("That sounds lovely!");
                customer.addBehaviour(new ReceiveMeal(customer, MessageTemplate.and(MessageTemplate.MatchSender(customer.getWaiter()), MessageTemplate.and(MessageTemplate.MatchConversationId("meal-delivering"), MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST)))));
                this.reset();
            }

        }
        else {
            customer.sendMessage(propose.getSender(), ACLMessage.ACCEPT_PROPOSAL, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, "dish-feedback", customer.getDesiredDish() + " - original");
            customer.addBehaviour(new ReceiveMeal(customer, MessageTemplate.and(MessageTemplate.MatchSender(customer.getWaiter()), MessageTemplate.and(MessageTemplate.MatchConversationId("meal-delivering"), MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST)))));
            this.reset();
        }
    }


}
