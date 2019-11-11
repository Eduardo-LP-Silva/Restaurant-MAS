package behaviours;

import agents.Waiter;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Dish;

public class TakeOrder extends CyclicBehaviour{
    
    private static final long serialVersionUID = 7818256748738825651L;
    private int step = 0;
    private int customerMood;
    private AID customerID;
    private Waiter myWaiter;

    public TakeOrder(Waiter waiter) {
        myWaiter = waiter;
    }

    @Override
    public void action() {
        ACLMessage msg;
        MessageTemplate template;

        switch(step) {
            //Attend customer
            case 0:
                template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchConversationId("waiter-request"));
                msg = myWaiter.receive(template);

                if(msg != null) 
                    attendCustomer(msg);
                else
                    block();

                break;

            case 1:
                //Get order from customer
                template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                        MessageTemplate.MatchConversationId("order-request"));
                msg = myWaiter.receive(template);

                if(msg != null)
                    getOrder(msg);
                else
                    block();
                break;

            case 2:
                //Get dish details from another agent
                template = MessageTemplate.MatchConversationId("dish-details");
                msg = myWaiter.receive(template);

                if(msg != null) 
                    receiveDishDetails(msg);
                else
                    block();

                break;

            case 3:
                //Get feedback from customer
                template = MessageTemplate.MatchConversationId("dish-feedback");
                msg = myWaiter.receive(template);

                if(msg != null) //Message: <dish>
                    getCustomerFeedback(msg);
                else
                    block();

                break;

            case 4:
                //Get feedback from kitchen
                template = MessageTemplate.MatchConversationId("start-dish");
                msg = myWaiter.receive(template);

                if(msg != null) //Message: <dish quantity time>
                    getKitchenFinalCheck(msg);
                else
                    block();
                break;
        }
    }

    private void getKitchenFinalCheck(ACLMessage msg) {
        String[] dishInfo = msg.getContent().split(" ");

        if(msg.getPerformative() == ACLMessage.REFUSE) {
            myWaiter.getKnownDishes().get(myWaiter.getKnownDishIndex(msg.getContent())).setAvailability(0);
            myWaiter.sendMessage(customerID, ACLMessage.FAILURE, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                    "dish-final-check", "unavailable");
            myWaiter.printMessage("I'm sorry, but there's been a mistake. We're out of " + dishInfo[0] + ".");
            step = 1;
        }
        else {
            myWaiter.getKnownDishes().get(myWaiter.getKnownDishIndex(dishInfo[0])).setAvailability(Integer.parseInt(dishInfo[1]));
            myWaiter.printMessage("Your meal is being prepared.");
            myWaiter.addBehaviour(new ServeMeal(myAgent, Long.parseLong(dishInfo[2]), customerID, dishInfo[0]));
        }
    }

    private void getCustomerFeedback(ACLMessage msg) {
        if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
            Dish dish =  myWaiter.getKnownDishes().get(myWaiter.getKnownDishIndex(msg.getContent()));
            dish.decrementAvailability();
            myWaiter.sendMessage(myWaiter.getKitchen(), ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    "start-dish", dish.getName());
            myWaiter.printMessage("Right away!");
            step = 4;
        }
        else {
            /* TODO Customer-side idea: Maybe add static function to kitchen to serve as menu,
            at this point the customer picks a random dish if the one ordered isn't available, else prepare original dish*/

            step = 1;
        }
    }

    private void evaluateDish(Dish dish, String infoSource) {
        //Customer mood += cookingTime - 15 | Customer mood += DishPreparation - 5
        if(dish.getAvailability() == 0) {
            //TODO Don't refuse, suggest something else
            myWaiter.sendMessage(customerID, ACLMessage.REFUSE, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                    "order-request", "unavailable");
            myWaiter.printMessage("I'm sorry, it seems that we're all out of " + dish.getName() + ".");
            step = 1;
        } 
        else
            if(customerMood + dish.getCookingTime() - 15 <= 3 || customerMood + dish.getPreparation() - 5 <= 3) {
            //TODO Suggest something else (based on known dishes ?)
            }
            else {
                myWaiter.sendMessage(customerID, ACLMessage.PROPOSE, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                        "dish-feedback", dish.getName() + " " + infoSource);
                myWaiter.printMessage("Excellent choice!");
                step = 3;
            }
                
    }

    private void receiveDishDetails(ACLMessage msg) {
        String content = msg.getContent();
        String[] dishDetails = content.split(" "); //Message format: "dish availability cookingTime preparationRate"
        boolean reliable = msg.getSender().getName().equals(myWaiter.getKitchen().getName());
        Dish dish = new Dish(dishDetails[0], Integer.parseInt(dishDetails[1]), Integer.parseInt(dishDetails[2]), 
            Integer.parseInt(dishDetails[3]), reliable);
        String infoSrc;
            
        if(myWaiter.getKnownDishes().contains(dish)) 
            myWaiter.updateKnownDish(dish);
        else
            myWaiter.getKnownDishes().add(dish);

        if(reliable)
            infoSrc = "kitchen";
        else
            infoSrc = "waiter";

        evaluateDish(dish, infoSrc);
    }

    private void getOrder(ACLMessage msg) {
        String[] customerDetails = msg.getContent().split(" "); //Message: <Dish Mood>
        String dish = customerDetails[0];
        int index;

        customerMood = Integer.parseInt(customerDetails[1]);

        if((index = myWaiter.getKnownDishIndex(customerDetails[0])) == -1) {
            if(customerMood <= 5) {
                //TODO Ask waiter
            }
            else {
                customerMood--;
                myWaiter.sendMessage(myWaiter.getKitchen(), ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_REQUEST,
                        "dish-details", dish);
                myWaiter.printMessage("Hold on a minute, let me check with the kitchen staff.");
            }

            step = 2;
        }
        else
            evaluateDish(myWaiter.getKnownDishes().get(index), "kitchen");
    }

    private void attendCustomer(ACLMessage msg) {
        if(myWaiter.isBusy()) {
            myWaiter.sendMessage(msg.getSender(), ACLMessage.REFUSE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), "busy");
            myWaiter.printMessage("I'm sorry, I'm a bit busy at the moment.");
            return;
        }
        
        myWaiter.addCustomer();
        customerID = msg.getSender();
        myWaiter.sendMessage(msg.getSender(), ACLMessage.AGREE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                msg.getConversationId(), "ok");
        myWaiter.printMessage("I'll gladly be your waiter this evening, " + msg.getSender().getLocalName() + ".");
        myWaiter.sendMessage(msg.getSender(), ACLMessage.INFORM, FIPANames.InteractionProtocol.FIPA_REQUEST,
                msg.getConversationId(), "proceed");
        myWaiter.printMessage("Go ahead, what can I get you?");
        step = 1;
    }
}