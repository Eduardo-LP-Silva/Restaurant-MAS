package utils;

import java.util.HashMap;
import java.util.Random;

public class Kitchen {
    private HashMap<String, Pair<Integer, Integer>> meals; //<Dish, <CookingTime, WellPreparedProbability>>
    private String[] dishes;

    public Kitchen() {
        meals = new HashMap<String, Pair<Integer, Integer>>();
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
    }

    private void generateMeals() {
        Random rand = new Random();
        Integer cookingTime, wellPreparedProb;

        for(int i = 0; i < dishes.length; i++) {
            cookingTime = rand.nextInt(85) + 5;
            wellPreparedProb = rand.nextInt(9) + 1;
            meals.put(dishes[i], new Pair<Integer,Integer>(cookingTime, wellPreparedProb));
        }
    }

    public Pair<Integer, Integer> getMealInfo(String dish) {
        return meals.get(dish);
    }

    public String selectRandomMeal() {
        Random rand = new Random();

        return dishes[rand.nextInt(dishes.length)];
    }
}