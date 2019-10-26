package utils;

public class Dish {
    private String name;
    private int availability;
    private int cookingTime;
    private int preparation;
    private boolean reliable;

    public Dish(String name, int availability, int cookingTime, int preparation, boolean reliable) {
        this.name = name;
        this.availability = availability;
        this.cookingTime = cookingTime;
        this.preparation = preparation;
        this.reliable = reliable;
    }

    @Override
    public boolean equals(Object dish) {
        if(dish instanceof Dish) 
            return ((Dish) dish).getName().equals(name);
        else
            return false;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
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

    public boolean isReliable() {
        return reliable;
    }
}