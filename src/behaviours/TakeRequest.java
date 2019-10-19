package behaviours;

import agents.Waiter;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class TakeRequest extends CyclicBehaviour
{
    private static final long serialVersionUID = 7818256748738825651L;

    @Override
    public void action() {
        Waiter myWaiter = (Waiter) myAgent;
        ACLMessage msg = myAgent.receive();

        if(msg != null) {
            ACLMessage reply = msg.createReply();

            //TODO Check if message is from client or waiter
            if(myWaiter.getNoCustomers() > 3) {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("busy");
            }

            String meal = msg.getContent();

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