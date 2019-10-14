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

            //FIPA-REQUEST: waiter - waiter, waiter - kitchen
            //Other protocol (cfp ?): Waiter - customer
            //TODO Ask kitchen / waiter about meal
            //TODO Advise based on obtained information
            //Add max waiting time / minminum meal quality to customer ?
            //Add boolean gotDesiredMeal to customer
        }
        else
            block();
    }
}