package behaviours;

import agents.Waiter;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class TakeOrder extends CyclicBehaviour
{
    private int step = 0;
    private MessageTemplate template;
    private static final long serialVersionUID = 7818256748738825651L;

    @Override
    public void action() {
        ACLMessage msg, reply;
        String content;

        switch(step) {
            case 0:
                //Other protocol (cfp ?): Waiter - customer
                template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), //TODO Change performative
                    MessageTemplate.MatchConversationId("order-request"));

                msg = myAgent.receive(template);

                if(msg != null) {
                    reply = msg.createReply();

                    if(((Waiter) myAgent).getNoCustomers() > 3) {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("busy");
                        myAgent.send(reply);
                        break;
                    }

                    //TODO Get additional info from client and split message
                    content = msg.getContent();
                    //TODO Evaluate client's mood to decide if to ask the kitchen or another waiter
                    //Add max waiting time / minminum meal quality to customer ?
                    //Add boolean gotDesiredMeal to customer

                    //Assuming it asks the kitchen                    
                    ACLMessage kitchenRequest = new ACLMessage(ACLMessage.REQUEST);
                    kitchenRequest.addReceiver(((Waiter) myAgent).getKitchen());
                    kitchenRequest.setConversationId("dish-details");
                    kitchenRequest.setContent(content); //TODO Change to only have meal
                    myAgent.send(kitchenRequest);
                    step = 1;
                }
                else
                    block();

                break;

            case 1:
                template = MessageTemplate.MatchConversationId("dish-details");

                msg = myAgent.receive(template);

                if(msg != null) {
                    content = msg.getContent();
                    String[] dishDetails = content.split(" "); //Message format: "dish cookigTime preparationRate" 

                    //TODO Advise based on obtained information
                }
                else
                    block();

                break;
                //FIPA-REQUEST: waiter - waiter, waiter - kitchen
            default:
                return;
        }
    }
}