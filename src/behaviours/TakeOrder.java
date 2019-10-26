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
        Waiter myWaiter = (Waiter) myAgent;

        switch(step) {
            case 0:
                //Other protocol (cfp ?): Waiter - customer
                template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), //TODO Change performative
                    MessageTemplate.MatchConversationId("order-request"));

                msg = myAgent.receive(template);

                if(msg != null) {
                    reply = msg.createReply();

                    if(myWaiter.getNoCustomers() > 3) {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("busy");
                        myAgent.send(reply);
                        break;
                    }
                    else
                        myWaiter.addCustomer();

                    String[] customerDetails = msg.getContent().split(" "); //Message: <Dish Mood>
                    //TODO Differentiate dish from quickest-dish request (?)
                    String dish = customerDetails[0];
                    customerMood = Integer.parseInt(customerDetails[1]);

                    //TODO Drop one point to customer mood if asking kitchen
                    if(customerMood <= 5) { 
                        //TODO Ask waiter
                    } 
                    else {
                        ACLMessage kitchenRequest = new ACLMessage(ACLMessage.REQUEST);
                        kitchenRequest.addReceiver(myWaiter.getKitchen());
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
                    int[] dish = {Integer.parseInt(dishDetails[1]), Integer.parseInt(dishDetails[2]), 
                        Integer.parseInt(dishDetails[3])};
                        
                    //If it asked the kitchen, replace any inconsistencies
                    //else only update the quantity if lower than known
                    if(myWaiter.getKnownDishes().containsKey(dishDetails[0])) {
                        int[] knownDish = myWaiter.getKnownDishes().get(dishDetails[0]);

                        if(msg.getSender().getName().equals(myWaiter.getName())) {
                            boolean different = false;

                            for(int i = 0; i < knownDish.length; i++) 
                                if(knownDish[i] != dish[i]) {
                                    knownDish[i] = dish[i];
                                    different = true;
                                }

                            if(different)
                                myWaiter.getKnownDishes().put(dishDetails[0], knownDish);
                        }
                        else
                            if(dish[0] < knownDish[0]) {
                                knownDish[0] = dish[0];
                                myWaiter.getKnownDishes().put(dishDetails[0], knownDish);
                            }    
                    }
                    else
                        myWaiter.getKnownDishes().put(dishDetails[0], dish);
            
                    //Customer mood += cookingTime - 15 | Customer mood += DishPreparation - 5
                    if(Integer.parseInt(dishDetails[1]) == 0 
                    || customerMood + (Integer.parseInt(dishDetails[2]) - 15) <= 3 
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