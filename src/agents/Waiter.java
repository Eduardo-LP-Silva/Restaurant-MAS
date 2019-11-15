package agents;

import java.util.ArrayList;
import java.util.Random;

import behaviours.ReplyToWaiter;
import behaviours.ServiceSearch;
import behaviours.TakeOrder;
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
    private static final int MAX_CLIENT_NO = 3;
    private AID kitchen;
    private ArrayList<Dish> knownDishes = new ArrayList<>();
    private ArrayList<Pair<AID, Boolean>> waiters = new ArrayList<>();
    private int noCustomers = 0;
    private double tips = 0;
    private boolean trusthworthy;
    private int waiterIndex = 0;

    protected void setup() {
        role = "Waiter";

        Object[] args = getArguments();

        if(args.length != 1) {
            System.out.println("Usage: Waiter <trustworthy>");
            doDelete();
            return;
        }

        trusthworthy = Boolean.parseBoolean((String) args[0]);

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

        this.addBehaviour(new TakeOrder(this));
        this.addBehaviour(new ServiceSearch(this, 1000));
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
        
        if(newDish.isReliable())
            knownDishes.set(dishIndex, newDish);
        else {
            Dish knownDish = knownDishes.get(dishIndex);

            if(knownDish.isReliable() && newDish.getAvailability() < knownDish.getAvailability())
                knownDish.setAvailability(newDish.getAvailability());
            else
                knownDishes.set(dishIndex, newDish); 
        }    
    }

    public Dish suggestOtherDish(Dish originalDish, int customerMood) {

        for(int i = 0; i < knownDishes.size(); i++)
            if(customerMood + knownDishes.get(i).getCookingTime() - 5 >= 3
                    && customerMood + knownDishes.get(i).getPreparation() - 5 >= 3)
                return knownDishes.get(i);

        return null;
    }

    public void informAboutDish(AID otherWaiter, String dishName) {
        int dishIndex = getKnownDishIndex(dishName);

        if(dishIndex == -1) {
            sendMessage(otherWaiter, ACLMessage.FAILURE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    "dish-details", dishName);
            printMessage("I don't know about that one, try someone else...");
        }
        else {
            Dish requestedDish = knownDishes.get(dishIndex);
            Random rand = new Random();
            String dishDetails = dishName + " ";

            //75% chance of lying
            if(!trusthworthy && rand.nextInt(99) + 1 <= 75) {
                if(requestedDish.getAvailability() == 0)
                    dishDetails += rand.nextInt(4) + 1; //To make the other waiter look bad when he finds out it's 0
                else
                    dishDetails += rand.nextInt(requestedDish.getAvailability());

                dishDetails += " " + rand.nextInt(requestedDish.getCookingTime() * 2) + " "
                        + rand.nextInt(requestedDish.getPreparation());
            }
            else
                dishDetails += requestedDish.getAvailability() + " " + requestedDish.getCookingTime() + " "
                        + requestedDish.getPreparation();

            printMessage("Yes, here you go");
            sendMessage(otherWaiter, ACLMessage.INFORM, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    "dish-details", dishDetails);
        }

    }

    public void addWaiters(AID[] newWaiters) {
        boolean found;

        for(AID newWaiter : newWaiters) {
            found = false;

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

    public void addCustomer() {
        noCustomers++;
    }

    public void addTip(double tip) {
        tips += tip;
    }

    public void removeCustomer() {
        noCustomers--;
    }

    public boolean isBusy() {
        return noCustomers >= MAX_CLIENT_NO;
    }

    public AID getKitchen() {
        return kitchen;
    }

    public int getWaiterIndex() {
        return waiterIndex;
    }

    public ArrayList<Pair<AID, Boolean>> getWaiters() {
        return waiters;
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
}