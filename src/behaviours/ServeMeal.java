package behaviours;

import agents.Waiter;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.RecordWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class ServeMeal extends WakerBehaviour {
    
    private static final long serialVersionUID = -1854723296124682854L;
    private AID customer;
    private Waiter myWaiter;
    private String dish;
    private int dishAvailability;
    private int dishCookingTime;
    private int dishQuality;

    ServeMeal(Agent a, long timeout, AID customer, String[] dish) {
        super(a, timeout);

        myWaiter = (Waiter) a;
        this.customer = customer;
        this.dish = dish[0];
        dishAvailability = Integer.parseInt(dish[1]);
        dishCookingTime = Integer.parseInt(dish[2]);
        dishQuality = Integer.parseInt(dish[3]);
    }

    @Override
    public void onWake() {
        myWaiter.printMessage("A dose of " + dish + ", just like you ordered, " + customer.getLocalName() + ".");
        myWaiter.sendMessage(customer, ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_REQUEST,
                "meal-delivering", dish);
        getCustomerFeedback();
    }

    private void recordData(int initialMood, int finalMood, double tip) {
        RecordWriter.writeHeaders("tip_records.csv", "Liar," +
                "Initial Mood," +
                "Final Mood," +
                "Dish Availability," +
                "Dish Cooking Time," +
                "Dish Quality," +
                "Tip\n");
        RecordWriter.write("tip_records.csv", myWaiter.getTrustworthy() + ","
                    + initialMood + ","
                    + finalMood + ","
                    + dishAvailability + ","
                    + dishCookingTime + ","
                    + dishQuality + ","
                    + tip + "\n");
    }

    private void receiveTip(ACLMessage msg) {
        String[] contents = msg.getContent().split("-");
        double tip = Double.parseDouble(contents[0]);
        int initialMood = Integer.parseInt(contents[1]);
        int finalMood = Integer.parseInt(contents[2]);
        myWaiter.addTip(tip);
        myWaiter.printMessage("Thank you very much " + customer.getLocalName() + " for the " + tip + "€!");

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        String totalTips = df.format(myWaiter.getTips());

        myWaiter.printMessage("I have collected " + totalTips + "€ in tips so far!");
        recordData(initialMood, finalMood, tip);
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
