package behaviours;

import agents.Waiter;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Dish;

public class TakeOrder extends CyclicBehaviour
{
    private static final long serialVersionUID = 7818256748738825651L;
    private int step = 0;
    private MessageTemplate template;
    private int customerMood;
    private Waiter myWaiter = (Waiter) myAgent;

    @Override
    public void action() {
        ACLMessage msg;

        switch(step) {
            //Receive request from customer
            case 0:
                //Other protocol (cfp ?): Waiter - customer
                template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), //TODO Change performative
                    MessageTemplate.MatchConversationId("order-request"));
                msg = myAgent.receive(template);

                if(msg != null) 
                    attendCustomer(msg);
                else
                    block();

                break;

            case 1:
                template = MessageTemplate.MatchConversationId("dish-details");
                msg = myAgent.receive(template);

                if(msg != null) 
                    receiveDishDetails(msg);
                else
                    block();

                break;

            case 2:
                template = MessageTemplate.MatchConversationId("start-dish");
                msg = myAgent.receive(template);

                if(msg != null) { //Message: <dish>
                    if(msg.getPerformative() == ACLMessage.REFUSE) {
                        myWaiter.getKnownDishes().get(myWaiter.getKnownDishIndex(msg.getContent())).setAvailability(0);

                        //TODO Get back to customer and start process again
                    }
                    else {
                        //TODO Create new timed behaviour to deliver meal
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

    private void evaluateDish(Dish dish) {
        //Customer mood += cookingTime - 15 | Customer mood += DishPreparation - 5
        if(dish.getAvailability() == 0) {
            //TODO Refuse and start process again
        } 
        else
            if(customerMood + dish.getCookingTime() - 15 <= 3 || customerMood + dish.getPreparation() - 5 <= 3) {
            //TODO Suggest something else (based on known dishes ?)
            }
            else {
                step = 2;
                myWaiter.relayRequestToKitchen(dish.getName());
            }
                
    }

    private void receiveDishDetails(ACLMessage msg) {
        String content = msg.getContent();
        String[] dishDetails = content.split(" "); //Message format: "dish availability cookigTime preparationRate"
        Dish dish = new Dish(dishDetails[0], Integer.parseInt(dishDetails[1]), Integer.parseInt(dishDetails[2]), 
            Integer.parseInt(dishDetails[3]), msg.getSender().getName().equals(myWaiter.getName()));
            
        if(myWaiter.getKnownDishes().contains(dish)) 
            myWaiter.updateKnowDish(dish);
        else
            myWaiter.getKnownDishes().add(dish);

        evaluateDish(dish);
    }

    private void attendCustomer(ACLMessage msg) {
       
        ACLMessage reply = msg.createReply();

        if(myWaiter.isBusy()) {
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("busy");
            myAgent.send(reply);
            return;
        }
        
        myWaiter.addCustomer(msg.getSender());
        reply.setPerformative(ACLMessage.AGREE);
        reply.setContent("Waiter " + myWaiter.getName() + " at your service.");
        myWaiter.send(reply);
        
        String[] customerDetails = msg.getContent().split(" "); //Message: <Dish Mood>
        //TODO Differentiate dish from quickest-dish request (?)
        String dish = customerDetails[0];
        int index;
        customerMood = Integer.parseInt(customerDetails[1]);

        if((index = myWaiter.getKnownDishIndex(customerDetails[0])) == -1) {
            //TODO Drop one point to customer mood if asking kitchen
            if(customerMood <= 5) { 
                //TODO Ask waiter
            } 
            else 
                myWaiter.askDishDetails(myWaiter.getKitchen(), dish);
            
            step = 1;
        }
        else
            evaluateDish(myWaiter.getKnownDishes().get(index));
    }
}