package behaviours;

import agents.Waiter;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.text.DecimalFormat;

public class ServeMeal extends WakerBehaviour {
    
    private static final long serialVersionUID = -1854723296124682854L;
    private AID customer;
    private Waiter myWaiter;
    private String dish;

    ServeMeal(Agent a, long timeout, AID customer, String dish) {
        super(a, timeout);

        myWaiter = (Waiter) a;
        this.customer = customer;
        this.dish = dish;
    }

    @Override
    public void onWake() {
        myWaiter.printMessage("A dose of " + dish + ", just like you ordered, " + customer.getLocalName() + ".");
        myWaiter.sendMessage(customer, ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_REQUEST,
                "meal-delivering", dish);
        getCustomerFeedback();
    }

    private void receiveTip(ACLMessage msg) {
        myWaiter.addTip(Double.parseDouble(msg.getContent()));
        myWaiter.printMessage("Thank you very much " + customer.getLocalName() + " for the " + msg.getContent() + "€!");

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        String totalTips = df.format(myWaiter.getTips());

        myWaiter.printMessage("I have collected " + totalTips + "€ in tips so far!");
    }

    private void getCustomerFeedback() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchConversationId("meal-delivering"),
                MessageTemplate.MatchPerformative(ACLMessage.AGREE));
        ACLMessage msg;

        do {
            msg = myWaiter.receive(template);

            if(msg == null)
                block();
        }
        while(msg == null);

        ACLMessage secondMessage;
        MessageTemplate secondTemplate =  MessageTemplate.and(MessageTemplate.MatchConversationId("meal-delivering"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        do {
            secondMessage = myWaiter.receive(secondTemplate);

            if(secondMessage == null)
                block();
        }
        while(secondMessage == null);

        receiveTip(secondMessage);
    }

}
