package behaviours;

import agents.Waiter;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Pair;

public class ReplyToWaiter extends CyclicBehaviour {
    private static final long serialVersionUID = -7489339939177286595L;

    private Waiter myWaiter;

    public ReplyToWaiter(Waiter waiter) {
        myWaiter = waiter;
    }

    /*
     * Reason for a waiter to lie to another:
     * 
     * - Based on food availability (there's only 1-4 servings per dish available)
     * - Based on current tips (Lore: Waiter with most tip money gets bonus at the end)
     */
    @Override
    public void action() {
        ACLMessage msg;
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId("dish-details"));

        msg = myAgent.receive(template);

        if(msg != null) {
            Pair<AID, Boolean> requestingWaiter = myWaiter.getWaiter(msg.getSender());

            if(requestingWaiter != null && !requestingWaiter.getValue()) {
                myWaiter.printMessage("You've lied to me before, try someone else...");
                myWaiter.sendMessage(msg.getSender(), ACLMessage.REFUSE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                        msg.getConversationId(), msg.getContent());
                return;
            }

            myWaiter.printMessage("Hmm, let me think...");
            myWaiter.sendMessage(msg.getSender(), ACLMessage.AGREE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), msg.getContent());
            myWaiter.informAboutDish(msg.getSender(), msg.getContent());
        }
        else
            block();
    }
}