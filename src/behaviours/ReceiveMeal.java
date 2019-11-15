package behaviours;

import agents.Customer;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

import java.util.Random;

public class ReceiveMeal extends SimpleAchieveREResponder {
    Customer customer;
    Boolean done;

    public ReceiveMeal(Customer c, MessageTemplate mt) {
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
