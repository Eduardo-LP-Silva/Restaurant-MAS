package behaviours;

import agents.Waiter;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Dish;
import utils.Pair;

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

                if(msg != null) //Message: <dish quantity time prep>
                    getKitchenFinalCheck(msg);
                else
                    block();
                break;

            case 5:
                template = MessageTemplate.or(MessageTemplate.MatchConversationId("dish-details"),
                        MessageTemplate.MatchConversationId("start-dish"));

                msg = myWaiter.receive(template);

                if(msg != null)
                    receiveInfoAck(msg);
                else
                    block();
                break;
        }
    }

    private void getKitchenFinalCheck(ACLMessage msg) {
        String[] dishInfo = msg.getContent().split(" - ");
        Dish dish = myWaiter.getKnownDish(dishInfo[0]);

        if(dish != null && !dish.compareStaticDetails(dishInfo[0], Integer.parseInt(dishInfo[2]),
                Integer.parseInt(dishInfo[3]))) {
            Pair<AID, Boolean> otherWaiter = myWaiter.getWaiter(dish.getInfoSrc());

            if(otherWaiter != null)
                otherWaiter.setValue(false);
        }

        if(msg.getPerformative() == ACLMessage.REFUSE) {
            myWaiter.getKnownDish(msg.getContent()).setAvailability(0);
            myWaiter.printMessage("I'm sorry, but there's been a mistake. We're out of " + dishInfo[0] + ".");
            myWaiter.sendMessage(customerID, ACLMessage.FAILURE, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                    "order-request", "unavailable");
            step = 1;
        }
        else {
            myWaiter.getKnownDish(dishInfo[0]).setAvailability(Integer.parseInt(dishInfo[1]));
            myWaiter.printMessage("Your meal is being prepared.");
            myWaiter.sendMessage(customerID, ACLMessage.INFORM, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                    "order-request", dishInfo[2] + " - " + dishInfo[3]);
            myWaiter.addBehaviour(new ServeMeal(myAgent, Long.parseLong(dishInfo[2]) * 1000, customerID, dishInfo[0]));
            step = 0;
        }
    }

    private void getCustomerFeedback(ACLMessage msg) {
        String[] msgDetails = msg.getContent().split(" - ");

        if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL
                || (msgDetails.length > 1 && msgDetails[1].equals("original"))) {
            Dish dish =  myWaiter.getKnownDish(msgDetails[0]);
            dish.decrementAvailability();
            myWaiter.printMessage("Right away!");
            myWaiter.sendMessage(myWaiter.getKitchen(), ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    "start-dish", dish.getName());
            step = 5;
        }
        else {
            myWaiter.printMessage("Okay, what's it going to be then?");
            step = 1;
        }
    }

    private void evaluateDish(Dish dish, String infoSource) {
        //Customer mood += cookingTime - 5 | Customer mood += DishPreparation - 5
        Dish suggestion;

        if(dish.getAvailability() == 0) {
            myWaiter.printMessage("I'm sorry, it seems that we're all out of " + dish.getName() + ".");
            myWaiter.sendMessage(customerID, ACLMessage.REFUSE, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                    "order-request", "unavailable");
            step = 1;
        } 
        else
            if((customerMood + dish.getCookingTime() - 5 <= 3 || customerMood + dish.getPreparation() - 5 <= 3)
                && (suggestion = myWaiter.suggestOtherDish(dish, customerMood)) != null) {

                //String suggestionInfoSrc = suggestion.isReliable() ? "kitchen" : "waiter";

                myWaiter.printMessage("How about " + suggestion.getName() + "?");
                myWaiter.sendMessage(customerID, ACLMessage.PROPOSE, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                        "order-request", suggestion.getName() + " - " + infoSource);
                step = 3;
            }
            else {
                myWaiter.printMessage("Excellent choice!");
                myWaiter.sendMessage(customerID, ACLMessage.PROPOSE, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                        "order-request", dish.getName() + " - " + infoSource);
                step = 3;
            }
    }

    private void receiveInfoAck(ACLMessage msg) {
        if(msg.getPerformative() == ACLMessage.AGREE) {
            if(msg.getConversationId().equals("dish-details"))
                step = 2;
            else
                step = 4;
        }
        else {
            if(!msg.getSender().equals(myWaiter.getKitchen())) {
                AID nextAgent = myWaiter.getNextReliableWaiter();

                if(nextAgent == null) {
                    nextAgent = myWaiter.getKitchen();
                    myWaiter.printMessage("It seems nobody reliable wants to talk to me, guess I'll ask the kitchen staff");
                }
                else
                    myWaiter.printMessage("Okay... How about you " +  nextAgent.getLocalName() + "?");

                myWaiter.sendMessage(nextAgent, ACLMessage.REQUEST,
                        FIPANames.InteractionProtocol.FIPA_REQUEST, "dish-details", msg.getContent());
                step = 5;
            }
            else
                System.out.println("Error: Kitchen refused request");
        }
    }

    private void receiveDishDetails(ACLMessage msg) {
        String content = msg.getContent();

        if(msg.getPerformative() == ACLMessage.FAILURE) {
            if(msg.getSender().equals(myWaiter.getKitchen())) {
                myWaiter.printMessage("I'm afraid we don't serve that dish in here. Try another one.");
                myWaiter.sendMessage(customerID, ACLMessage.REFUSE, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                        "order-request", "not-found");
                step = 1;
                return;
            }
            else {
                AID nextAgent = myWaiter.getNextReliableWaiter();

                if(nextAgent == null) {
                    myWaiter.printMessage("It seems I'll have to ask the kitchen staff after all...");
                    myWaiter.sendMessage(myWaiter.getKitchen(), ACLMessage.REQUEST,
                            FIPANames.InteractionProtocol.FIPA_REQUEST, "dish-details", msg.getContent());
                }
                else {
                    myWaiter.printMessage("How about you "
                            + myWaiter.getWaiters().get(myWaiter.getWaiterIndex()).getKey().getLocalName() + "?");
                    myWaiter.sendMessage(nextAgent, ACLMessage.REQUEST,
                            FIPANames.InteractionProtocol.FIPA_REQUEST, "dish-details", msg.getContent());
                }

                step = 5;
                return;
            }
        }

        String[] dishDetails = content.split(" - "); //Message format: "dish - availability - cookingTime - preparationRate"
        Dish dish = new Dish(dishDetails[0], Integer.parseInt(dishDetails[1]), Integer.parseInt(dishDetails[2]), 
            Integer.parseInt(dishDetails[3]), msg.getSender());
        String infoSrc;
            
        if(myWaiter.getKnownDishes().contains(dish)) 
            myWaiter.updateKnownDish(dish);
        else
            myWaiter.getKnownDishes().add(dish);

        if(myWaiter.isDishInfoReliable(dish))
            infoSrc = "kitchen";
        else
            infoSrc = "waiter";

        evaluateDish(dish, infoSrc);
    }

    private void getOrder(ACLMessage msg) {
        String[] customerDetails = msg.getContent().split(" - "); //Message: <Dish - Mood>
        String dish = customerDetails[0];
        int index;

        myWaiter.resetWaiterIndex();
        customerMood = Integer.parseInt(customerDetails[1]);

        if((index = myWaiter.getKnownDishIndex(customerDetails[0])) == -1) {
            step = 5;

            if(customerMood <= 5 && myWaiter.getWaiters().size() > 0) {
                myWaiter.printMessage("Hold on a minute, let me ask my colleague.");
                myWaiter.sendMessage(myWaiter.getNextReliableWaiter(), ACLMessage.REQUEST,
                        FIPANames.InteractionProtocol.FIPA_REQUEST, "dish-details", dish);
            }
            else {
                customerMood--;
                myWaiter.printMessage("Hold on a minute, let me check with the kitchen staff.");
                myWaiter.sendMessage(myWaiter.getKitchen(), ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_REQUEST,
                        "dish-details", dish);
            }
        }
        else
            evaluateDish(myWaiter.getKnownDishes().get(index), "kitchen");
    }

    private void attendCustomer(ACLMessage msg) {
        if(myWaiter.isBusy()) {
            myWaiter.printMessage("I'm sorry, I'm a bit busy at the moment.");
            myWaiter.sendMessage(msg.getSender(), ACLMessage.REFUSE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), "busy");
            return;
        }
        
        myWaiter.addCustomer();
        customerID = msg.getSender();
        myWaiter.printMessage("I'll gladly be your waiter this evening, " + msg.getSender().getLocalName() + ".");
        myWaiter.sendMessage(msg.getSender(), ACLMessage.AGREE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                msg.getConversationId(), "ok");
        myWaiter.printMessage("Go ahead, what can I get you?");
        myWaiter.sendMessage(msg.getSender(), ACLMessage.INFORM, FIPANames.InteractionProtocol.FIPA_REQUEST,
                msg.getConversationId(), "proceed");
        step = 1;
    }
}