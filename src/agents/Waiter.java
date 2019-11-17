package agents;

import java.util.ArrayList;
import java.util.Random;

import behaviours.AttendCustomer;
import behaviours.ReplyToWaiter;
import behaviours.ServiceSearch;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import utils.Dish;
import utils.Pair;

public class Waiter extends RestaurantAgent
{
    private static final long serialVersionUID = 7110642579660810600L;
    private AID kitchen;
    private ArrayList<Dish> knownDishes = new ArrayList<>();
    private ArrayList<Pair<AID, Boolean>> waiters = new ArrayList<>();
    private AID customerID;
    private double tips = 0;
    private boolean trustworthy;
    private int waiterIndex = 0;

    protected void setup() {
        role = "Waiter";

        Object[] args = getArguments();

        if(args.length != 1) {
            System.out.println("Usage: Waiter <trustworthy>");
            doDelete();
            return;
        }

        trustworthy = Boolean.parseBoolean((String) args[0]);

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        dfd.setName(this.getAID());
        sd.setType("customer-service");
        sd.setName("mas-restaurant");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }

        printMessage("Checking in.");

        if(!searchForKitchen())
            this.doDelete();

        this.addBehaviour(new ServiceSearch(this, 500));
        this.addBehaviour(new AttendCustomer(this));
        this.addBehaviour(new ReplyToWaiter(this));
    }

    private boolean searchForKitchen() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("kitchen-service");
        template.addServices(sd);

        try {
            DFAgentDescription[] kitchenSearch = DFService.search(this, template);

            if(kitchenSearch.length > 0)
                kitchen = kitchenSearch[0].getName();
            else {
                printMessage("Could't find the kitchen...");
                return false;
            }    
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }

        return true;
    }

    protected void takeDown() {
        deRegister();
        printMessage("Going home.");
    }

    private void deRegister()
    {
        try {
            DFService.deregister(this);
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates a previously known dish's details.
     * If the information is reliable, it replaces the known details.
     * Else if the previous information was reliable it only updates the quantity, otherwise it replaces all the details
     * @param newDish The known dish with possible new details
     */
    public void updateKnownDish(Dish newDish) {
        int dishIndex = knownDishes.indexOf(newDish);
        Dish knownDish = knownDishes.get(dishIndex);
        
        if(isDishInfoReliable(newDish) || !isDishInfoReliable(knownDish))
            knownDishes.set(dishIndex, newDish);
        else {
                if(newDish.getAvailability() < knownDish.getAvailability())
                    knownDish.setAvailability(newDish.getAvailability());
        }    
    }

    public boolean isDishInfoReliable(Dish dish) {
        return dish.getInfoSrc().equals(kitchen);
    }

    public Dish suggestOtherDish(Dish originalDish, int customerMood) {

        for (Dish knownDish : knownDishes)
            if (customerMood - knownDish.getCookingTime() - 5 >= 3
                    && customerMood + knownDish.getPreparation() - 5 >= 3
                    && !knownDish.getName().equals(originalDish.getName()))
                return knownDish;

        return null;
    }

    public void informAboutDish(AID otherWaiter, String dishName) {
        Random rand = new Random();
        int lie = rand.nextInt(99) + 1;
        int dishIndex = getKnownDishIndex(dishName);

        //75% chance of lying
        if((dishIndex == -1 && trustworthy) || (!trustworthy && lie > 75)) {
            printMessage("I don't know about that one, try someone else...");
            sendMessage(otherWaiter, ACLMessage.FAILURE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    "dish-details", dishName);
        }
        else {
            Dish requestedDish = null;

            if(dishIndex != -1)
                requestedDish = knownDishes.get(dishIndex);

            String dishDetails = dishName + " - ";
            String messageToPrint = "";

            if(!trustworthy) {
                if(requestedDish == null || requestedDish.getAvailability() == 0)
                    dishDetails += rand.nextInt(5) + 1; //To make the other waiter look bad when he finds out it's 0
                else
                    dishDetails += rand.nextInt(requestedDish.getAvailability());

                dishDetails += " - " + (rand.nextInt(6) + 5) + " - "
                        + rand.nextInt(rand.nextInt(5) + 1);
                messageToPrint = "*Lies* ";
            }
            else
                dishDetails += requestedDish.getAvailability() + " - " + requestedDish.getCookingTime() + " - "
                        + requestedDish.getPreparation();

            messageToPrint += "Yes, here you go: " + dishDetails;

            printMessage(messageToPrint);
            sendMessage(otherWaiter, ACLMessage.INFORM, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    "dish-details", dishDetails);
        }

    }

    @Override
    public void addWaiters(AID[] newWaiters) {
        boolean found;

        for(AID newWaiter : newWaiters) {
            found = false;

            if(newWaiter.equals(this.getAID()))
                continue;

            for(Pair<AID, Boolean> waiter : waiters)
                if(waiter.getKey().equals(newWaiter)) {
                    found = true;
                    break;
                }

            if(!found)
                waiters.add(new Pair<>(newWaiter, true));
        }
    }

    public void resetWaiterIndex() {
        for(int i = 0; i < waiters.size(); i++)
            if(waiters.get(i).getValue()) {
                waiterIndex = i;
                return;
            }
    }

    public AID getNextReliableWaiter() {
        Pair<AID, Boolean> waiter;

        if(waiters.size() == 0 || waiterIndex >= waiters.size())
            return null;

        do {
            waiter = waiters.get(waiterIndex);
            waiterIndex++;
        }
        while(!waiter.getValue() && waiterIndex < waiters.size());

        if(!waiter.getValue())
            return null;
        else
            return waiter.getKey();
    }

    public void addTip(double tip) {
        tips += tip;
    }

    public boolean isBusy() {
        return customerID != null;
    }

    public AID getKitchen() {
        return kitchen;
    }

    public AID getCustomerID() {
        return customerID;
    }

    public ArrayList<Pair<AID, Boolean>> getWaiters() {
        return waiters;
    }

    public Pair<AID, Boolean> getWaiter(AID waiter) {
        for(Pair<AID, Boolean> knownWaiter : waiters)
            if(knownWaiter.getKey().equals(waiter))
                return knownWaiter;

        return null;
    }

    public ArrayList<Dish> getKnownDishes() {
        return knownDishes;
    }

    public double getTips() {
        return tips;
    }

    public int getKnownDishIndex(String dishName) {
        for(int i = 0; i < knownDishes.size(); i++)
            if(knownDishes.get(i).getName().equals(dishName))
                return i;

        return -1;
    }

    public Dish getKnownDish(String dishName) {
        int index = getKnownDishIndex(dishName);

        if(index != -1)
            return knownDishes.get(index);
        else
            return null;
    }

    public void setCustomerID(AID cid) {
        customerID = cid;
    }
}