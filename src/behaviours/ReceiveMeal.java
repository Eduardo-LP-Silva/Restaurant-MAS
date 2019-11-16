package behaviours;

import agents.Customer;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class ReceiveMeal extends SimpleAchieveREResponder {
    private Customer customer;
    private Boolean done;

    ReceiveMeal(Customer c, MessageTemplate mt) {
        super(c, mt);
        customer = c;
        done = false;
    }

    // Receive meal, agree with tipping
    @Override
    protected ACLMessage prepareResponse(ACLMessage request) {
        ACLMessage response = request.createReply();
        response.setPerformative(ACLMessage.AGREE);
        response.setContent("ok");

        customer.printMessage("Thank you!");
        customer.printMessage("I'll pay now.");
        return response;
    }

    // Tip waiter
    // Customer always tips, although it can tip low
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
        Random rand = new Random();
        // Maximum tip: 5.99
        double tip = customer.getMood() * 0.5 + 0.01 * rand.nextInt(99);

        BigDecimal bd = BigDecimal.valueOf(tip);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        tip = bd.doubleValue();

        ACLMessage notification = request.createReply();
        notification.setPerformative(ACLMessage.INFORM);
        notification.setContent("" + tip);

        customer.printMessage("Here's your tip: " + tip + "€. See you next time!");
        done = true;
        customer.doDelete();
        return notification;
    }

    @Override
    public boolean done() {
        return done;
    }
}
