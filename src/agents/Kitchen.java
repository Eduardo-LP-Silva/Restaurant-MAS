package agents;

import java.util.HashMap;
import java.util.Random;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import utils.Pair;

public class Kitchen extends RestaurantAgent
{
    private static final long serialVersionUID = 1L;
    private HashMap<String, int[]> meals; //<Dish, <Availability, CookingTime, WellPreparedProbability>>
    private String[] dishes;

    protected void setup() {
        role = "Kitchen";

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        dfd.setName(this.getAID());
        sd.setType("kitchen-service");
        sd.setName("mas-restaurant");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }

        meals = new HashMap<String, int[]>();
        dishes = new String[] {
            "onion soup",
            "escargots",
            "steak tartare",
            "roasted baby beets salad",
            "octopus",
            "fried chicken",
            "hamburger",
            "salmon with fries",
            "king crab",
            "pork ribs",
            "fried rice",
            "goat cheese",
            "hot dog",
            "francesinha",
            "lamb",
            "vegan burger",
            "omelette"
        };

        this.generateMeals();
        
        System.out.println("(kitchen) Kitchen " + this.getAID().getLocalName() + " at your service.");
        
        //Wait for client requests
    }

    private void generateMeals() {
        Random rand = new Random();
        Integer cookingTime, wellPreparedProb, availability;

        for(int i = 0; i < dishes.length; i++) {
            cookingTime = rand.nextInt(85) + 5;
            wellPreparedProb = rand.nextInt(9) + 1;
            availability = rand.nextInt(3) + 1;
            meals.put(dishes[i], new int[] {availability, cookingTime, wellPreparedProb});
        }
    }

    public int[] getMealInfo(String dish) {
        return meals.get(dish);
    }

    public String selectRandomMeal() {
        Random rand = new Random();

        return dishes[rand.nextInt(dishes.length)];
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }

        System.out.println("(kitchen) Kitchen " + this.getAID().getLocalName() + " shutting down.");
    }
}