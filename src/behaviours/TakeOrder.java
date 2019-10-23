package behaviours;

import agents.Waiter;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class TakeOrder extends CyclicBehaviour
{
    private static final long serialVersionUID = 7818256748738825651L;
    private int step = 0;
    private MessageTemplate template;
    private int customerMood;

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
                    else
                        ((Waiter) myAgent).addCustomer();

                    String[] customerDetails = msg.getContent().split(" "); //Message: <Dish Mood>
                    //TODO Differentiate dish from quickest-dish request (?)
                    String dish = customerDetails[0];
                    customerMood = Integer.parseInt(customerDetails[1]);

                    if(customerMood <= 5) { 
                        //TODO Ask waiter
                    } 
                    else {
                        ACLMessage kitchenRequest = new ACLMessage(ACLMessage.REQUEST);
                        kitchenRequest.addReceiver(((Waiter) myAgent).getKitchen());
                        kitchenRequest.setConversationId("dish-details");
                        kitchenRequest.setContent(dish);
                        myAgent.send(kitchenRequest);
                    }
                                    
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
                    String[] dishDetails = content.split(" "); //Message format: "dish availability cookigTime preparationRate" 

                    //Customer mood drops 1 point each 10 mins | Customer mood += DishPreparation - 5
                    if(Integer.parseInt(dishDetails[1]) == 0 
                    || customerMood - (Integer.parseInt(dishDetails[2]) / 10) <= 3 
                    || customerMood + (Integer.parseInt(dishDetails[3]) - 5) <= 3) {
                        //TODO Suggest something else (based on known dishes ?)
                    }
                    else {
                        //TODO Order food
                    }
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