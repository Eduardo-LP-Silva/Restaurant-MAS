package agents;

import java.util.ArrayList;
import behaviours.TakeOrder;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import utils.Dish;

public class Waiter extends RestaurantAgent
{
    private static final long serialVersionUID = 7110642579660810600L;
    private static final int MAX_CLIENT_NO = 3;
    private AID kitchen;
    private ArrayList<String> waiters = new ArrayList<>();
    private ArrayList<Dish> knownDishes = new ArrayList<>();
    private int noCustomers = 0;
    private int tips = 0;

    protected void setup() {
        role = "Waiter";

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

        return null;
    }

    public void addCustomer() {
        noCustomers++;
    }

    public void addTip(int tip) {
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

    public ArrayList<Dish> getKnownDishes() {
        return knownDishes;
    }

    public int getTips() {
        return tips;
    }

    public int getKnownDishIndex(String dishName) {
        for(int i = 0; i < knownDishes.size(); i++)
            if(knownDishes.get(i).getName().equals(dishName))
                return i;

        return -1;
    }
}