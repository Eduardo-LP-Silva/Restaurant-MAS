package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

public abstract class RestaurantAgent extends Agent {
    protected String role;

    public void addWaiters(AID[] newWaiters) {
        for(AID newWaiter : newWaiters)
            if (!waiters.contains(newWaiter) && !newWaiter.equals(this.getAID()))
                waiters.add(newWaiter);
    }

    public abstract void addWaiters(AID[] newWaiters);

    public void sendMessage(AID aid, int performative, String protocol , String conversationID, String content) {
        ACLMessage msg = new ACLMessage(performative);
        msg.addReceiver(aid);
        msg.setLanguage("English");
        msg.setConversationId(conversationID);
        msg.setProtocol(protocol);
        msg.setContent(content);
        send(msg);
    }

    public void printMessage(String message) {
        System.out.println("(" + role + " " + getAID().getLocalName() + ") " + message);
    }
}
