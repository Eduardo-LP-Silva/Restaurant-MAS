package agents;

import java.util.ArrayList;
import behaviours.TakeOrder;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import utils.Dish;

public class Waiter extends Agent 
{
    private static final long serialVersionUID = 7110642579660810600L;
    private static final int MAX_CLIENT_NO = 3;
    private AID kitchen;
    private ArrayList<String> waiters = new ArrayList<String>();
    private ArrayList<Dish> knownDishes = new ArrayList<Dish>();
    private ArrayList<AID> customers = new ArrayList<AID>();
    private int tips = 0;

    protected void setup() {        
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
        
        System.out.println("(waiter) Waiter " + this.getAID().getLocalName() + " at your service.");

        if(!searchForKitchen())
            this.doDelete();

        this.addBehaviour(new TakeOrder());
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
                System.out.println("Could't find the kitchen - Waiter " + getName());
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
        System.out.println("Waiter " + this.getAID().getLocalName() + " going home.");
    }

    public void deRegister()
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
    public void updateKnowDish(Dish newDish) {
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

    public void addCustomer(AID customer) {
        customers.add(customer);
    }

    public boolean isBusy() {
        return customers.size() >= MAX_CLIENT_NO;
    }

    public int getNoCustomers() {
        return customers.size();
    }  

    public AID getKitchen() {
        return kitchen;
    }

    public ArrayList<Dish> getKnownDishes() {
        return knownDishes;
    }
}