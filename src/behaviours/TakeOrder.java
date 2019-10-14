package behaviours;

import agents.Waiter;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class TakeOrder extends CyclicBehaviour
{
    @Override
    public void action() {
        Waiter myWaiter = (Waiter) myAgent;
        ACLMessage order = myAgent.receive();

        if(order != null) {
            ACLMessage reply = order.createReply();

            if(myWaiter.getNoCustomers() > 3) {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("busy");
            }

            String meal = order.getContent();

            //TODO Check kitchen if meal is available
        }
        else
            block();
    }
}