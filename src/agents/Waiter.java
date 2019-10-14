package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import utils.Kitchen;

public class Waiter extends Agent 
{
    private static Kitchen kitchen;
    //List of waiters ?
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
        
        System.out.println("Waiter " + this.getAID().getName() + " at your service.");

        if(kitchen == null)
            kitchen = new Kitchen();
        
        //Wait for client requests
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }

        System.out.println("Waiter " + this.getAID().getName() + " going home.");
    }
}