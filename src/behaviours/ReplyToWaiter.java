package behaviours;

import agents.Waiter;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReplyToWaiter extends CyclicBehaviour {
    private static final long serialVersionUID = -7489339939177286595L;

    private Waiter myWaiter;
    private int step = 0;

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
            ACLMessage reply = msg.createReply();
            myWaiter.sendMessage(msg.getSender(), ACLMessage.AGREE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), "");
            myWaiter.printMessage("Hmm, let me think...");
            myWaiter.informAboutDish(msg.getSender(), msg.getContent());
        }
        else
            block();
    }
}