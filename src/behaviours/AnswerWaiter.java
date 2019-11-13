package behaviours;

import agents.Waiter;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AnswerWaiter extends CyclicBehaviour {
    private Waiter myWaiter;
    private int step = 0;

    public AnswerWaiter(Waiter waiter) {
        myWaiter = waiter;
    }

    @Override
    public void action() {
        ACLMessage msg;
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId("dish-details"));

        msg = myAgent.receive(template);

        if(msg != null)
            myWaiter.informAboutDish(msg.getSender(), msg.getContent());
        else
            block();
    }
}
