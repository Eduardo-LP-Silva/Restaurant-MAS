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
            case ACLMessage.FAILURE:
                handleFailure(msg);
                break;
            case ACLMessage.INFORM:
                handleInform(msg);
                break;
            default:
                break;
        }
    }

    private void orderAgain() {
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

    // The dish ordered isn't available
    @Override
    protected void handleRefuse(ACLMessage msg) {
        orderAgain();
    }

    // The waiter proposes a dish (it could be the same I suggested if he agrees that it is a good choice)
    private void handlePropose(ACLMessage propose) {
        String proposedDish = propose.getContent().split(" - ")[0];
        String infoSource = propose.getContent().split(" - ")[1];

        if (infoSource.equals("kitchen")) {
           customer.decrementMood();
           customer.printMessage("Hey, it took a while...");
        }

        if(!proposedDish.equals(customer.getDesiredDish())) {
            Random rand = new Random();
            int accept = rand.nextInt(1);

            if(accept == 0) {
                customer.sendMessage(propose.getSender(), ACLMessage.REJECT_PROPOSAL, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, "dish-feedback", "no");
                this.reset();
                customer.printMessage("That doesn't sound so good...");
                orderAgain();
            }
            else {
                customer.decrementMood();
                customer.sendMessage(propose.getSender(), ACLMessage.ACCEPT_PROPOSAL, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, "dish-feedback", proposedDish);
                customer.printMessage("That sounds good!");
            }

        }
        else {
            customer.printMessage("Perfect, just what I wanted!");
            customer.sendMessage(propose.getSender(), ACLMessage.ACCEPT_PROPOSAL, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, "dish-feedback", customer.getDesiredDish() + " - original");
        }
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        handleRefuse(failure);
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        String[] mealInfo = inform.getContent().split(" - "); // < preparationTime - wellCooked >
        int preparationTime = Integer.parseInt(mealInfo[0]); // 1 to 10, the higher the value, the lower the mood
        int wellCooked = Integer.parseInt(mealInfo[1]); // 1 to 10, the lower the value, the lower the mood

        if (preparationTime < 5) {
            customer.incrementMood();
        }
        else if (preparationTime > 5) {
            customer.decrementMood();
        }

        if (wellCooked < 5) {
            customer.decrementMood();
        }
        else if (wellCooked > 5) {
            customer.incrementMood();
        }

        ReceiveMeal receiveMeal = new ReceiveMeal(customer, MessageTemplate.and(MessageTemplate.MatchSender(customer.getWaiter()), MessageTemplate.and(MessageTemplate.MatchConversationId("meal-delivering"), MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST))));
        customer.addBehaviour(receiveMeal);
        this.reset();
    }
}
