package utils;

import jade.core.AID;

public class Dish {
    private String name;
    private int availability;
    private int cookingTime;
    private int preparation;
    private AID infoSrc;

    public Dish(String name, int availability, int cookingTime, int preparation, AID infoSrc) {
        this.name = name;
        this.availability = availability;
        this.cookingTime = cookingTime;
        this.preparation = preparation;
        this.infoSrc = infoSrc;
    }

    @Override
    public boolean equals(Object dish) {
        if(dish instanceof Dish) 
            return ((Dish) dish).getName().equals(name);
        else
            return false;
    }

    public boolean compareStaticDetails(String dishName, int ct, int prep) {
        return dishName.equals(name) && ct == cookingTime && prep == preparation;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public void decrementAvailability() {
        availability--;
    }

    public String getName() {
        return name;
    }

    public int getAvailability() {
        return availability;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public int getPreparation() {
        return preparation;
    }

    public AID getInfoSrc() {
        return infoSrc;
    }
}