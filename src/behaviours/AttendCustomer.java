package behaviours;

import agents.Waiter;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AttendCustomer extends CyclicBehaviour {
    private Waiter myWaiter;

    public AttendCustomer(Waiter waiter) {
        myWaiter = waiter;
    }

    @Override
    public void action() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId("waiter-request"));
        ACLMessage msg = myWaiter.receive(template);

        if(msg != null)
            attendCustomer(msg);
        else
            block();
    }

    private void attendCustomer(ACLMessage msg) {
        if(myWaiter.isBusy()) {
            myWaiter.printMessage("I'm sorry, I'm a bit busy at the moment, " + msg.getSender().getLocalName() + ".");
            myWaiter.sendMessage(msg.getSender(), ACLMessage.REFUSE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), "busy");
        }
        else {
            myWaiter.setCustomerID(msg.getSender());
            myWaiter.printMessage("I'll gladly be your waiter this evening, " + msg.getSender().getLocalName() + ".");
            myWaiter.sendMessage(msg.getSender(), ACLMessage.AGREE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), "ok");
            myWaiter.addBehaviour(new TakeOrder(myWaiter));
            myWaiter.printMessage("Go ahead, what can I get you?");
            myWaiter.sendMessage(msg.getSender(), ACLMessage.INFORM, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), "proceed");
        }
    }
}
