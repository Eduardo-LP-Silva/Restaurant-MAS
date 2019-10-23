package behaviours;

import agents.Customer;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class ServiceSearch extends SimpleBehaviour {
    private static final long serialVersionUID = -4766123904483710759L;
    private Customer customer;
    private int numberOfWaiters = 0;

    public ServiceSearch(Customer a) {
        this.customer = a;
    }

    @Override
    public void action() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("customer-service");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(myAgent, template);
            AID[] waiters = new AID[result.length];
            numberOfWaiters = result.length;
            for(int i=0; i<result.length; i++) {
                waiters[i] = result[i].getName();
                System.out.println("(customer) Found one waiter: " + waiters[i].getLocalName());
            }

            // found at least one waiter
            if(result.length > 0) {
                customer.setWaiters(waiters);
            }

        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    @Override
    public boolean done() {
        if(customer.getWaiters().length == numberOfWaiters) {
            return true;
        }

        return false;
    }
}
