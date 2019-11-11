package behaviours;

import agents.Kitchen;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class TakeRequest extends CyclicBehaviour{
    
    private static final long serialVersionUID = 3055341223034464997L;

    @Override
    public void action() {
        Kitchen myKitchen = (Kitchen) myAgent;
        ACLMessage request = myAgent.receive();

        if(request != null) {
            ACLMessage reply = request.createReply();
            String meal = request.getContent();

            //TODO: Check if meal exists (if not, deny)
            //TODO: Get information about meal (time + quality)
            //TODO: Build reply with meal info
            //TODO: Send reply
        }
        else
            block();
    }
}