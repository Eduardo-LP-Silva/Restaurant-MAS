package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class Customer extends Agent {
    private String desiredDish;
    protected AID[] waiterAgents;

    @Override
    protected void setup() {
        System.out.println("Hello! Customer agent " + getAID().getLocalName() + " is ready.");

        Object[] args = getArguments();
        if (args != null && args.length == 1) {
            desiredDish = (String) args[0];
            // standardizing dish name
            desiredDish =  desiredDish.replace("-", " ");
            desiredDish = desiredDish.toLowerCase();
            System.out.println("I want to eat " + desiredDish);


        } else {
            System.out.println("No dish specified!");
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("Customer agent " + getAID().getLocalName() + " is terminating.");
    }

    public class ServiceSearch extends OneShotBehaviour {
        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();

            /* Check in to see what description we're using to register customers */
            sd.setType("customer-service");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                waiterAgents = new AID[result.length];

            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }
}
