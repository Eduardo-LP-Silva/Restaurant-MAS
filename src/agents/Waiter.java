package agents;

import java.util.ArrayList;
import java.util.HashMap;

import behaviours.TakeOrder;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Waiter extends Agent 
{
    private AID kitchen;
    private ArrayList<String> waiters = new ArrayList<String>();
    private HashMap<AID, String> customers = new HashMap<AID, String>();
    private static final int MAX_CLIENT_NO = 3;
    private static final long serialVersionUID = 7110642579660810600L;

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

        System.out.println("Waiter " + this.getAID().getLocalName() + " at your service.");

        if(!searchForKitchen())
            this.doDelete();

        
        
        this.addBehaviour(new TakeOrder());
    }

    private Integer searchKitchen(String dish, String property) {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        
        request.setContent("");

        return 0;
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

    public void deRegister()
    {
        try {
            DFService.deregister(this);
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    protected void takeDown() {
        deRegister();
        System.out.println("Waiter " + this.getAID().getLocalName() + " going home.");
    }

    public int getNoCustomers() {
        return customers.size();
    }  

    public AID getKitchen() {
        return kitchen;
    }
}