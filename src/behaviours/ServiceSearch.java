package behaviours;

import agents.RestaurantAgent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class ServiceSearch extends TickerBehaviour {
    
    private static final long serialVersionUID = -4766123904483710759L;
    private RestaurantAgent myRestaurantAgent;

    public ServiceSearch(RestaurantAgent a, long period) {
        super(a, period);

        myRestaurantAgent = a;
    }

    @Override
    protected void onTick() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("customer-service");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(myAgent, template);
            AID[] waiters = new AID[result.length];
            for(int i=0; i<result.length; i++) {
                waiters[i] = result[i].getName();
            }

            // found at least one waiter
            if(result.length > 0)
                myRestaurantAgent.addWaiters(waiters);

        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}
