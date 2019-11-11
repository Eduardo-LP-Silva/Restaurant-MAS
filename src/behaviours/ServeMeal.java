package behaviours;

import agents.Waiter;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ServeMeal extends WakerBehaviour {
    
    private static final long serialVersionUID = -1854723296124682854L;
    private AID customer;
    private Waiter myWaiter;
    private String dish;
    private int step = 0;

    ServeMeal(Agent a, long timeout, AID customer, String dish) {
        super(a, timeout);

        myWaiter = (Waiter) a;
        this.customer = customer;
        this.dish = dish;
    }

    @Override
    public void onWake() {
        switch(step) {
            case 0:
                myWaiter.sendMessage(customer, ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                        "meal-delivering", dish);
                myWaiter.printMessage("A dose of " + dish + ", just like you ordered, " + customer.getLocalName() + ".");
                step = 1;
                getCustomerFeedback();
                break;

            case 1:
                getCustomerFeedback();
                break;

            case 2:
                receiveTip();
                break;
        }

    }

    private void receiveTip() {
        MessageTemplate template = MessageTemplate.MatchConversationId("tip");
        ACLMessage msg = myWaiter.receive(template);

        if(msg == null) {
            block();
            reset();
        }

        if(msg.getPerformative() == ACLMessage.INFORM) {
            myWaiter.addTip(Integer.parseInt(msg.getContent()));
            myWaiter.printMessage("Thank you very much " + customer.getLocalName() + "for the " + msg.getContent() + "€!");
            myWaiter.removeCustomer();
        }

        myWaiter.printMessage("I have collected " + myWaiter.getTips() + "€ in tips so far!");
    }

    private void getCustomerFeedback() {
        MessageTemplate template = MessageTemplate.MatchConversationId("meal-delivering");
        ACLMessage msg = myWaiter.receive(template);

        if(msg == null) {
            block();
            reset();
        }

        step = 2;
        receiveTip();
    }
}
