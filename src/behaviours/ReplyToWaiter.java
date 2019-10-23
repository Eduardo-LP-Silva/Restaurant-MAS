package behaviours;

import jade.core.behaviours.CyclicBehaviour;

public class ReplyToWaiter extends CyclicBehaviour {
    private static final long serialVersionUID = -7489339939177286595L;

    /*
     * Reason for a waiter to lie to another:
     * 
     * - Based on food availability (there's only 1-4 servings per dish available)
     * - Based on current tips (Lore: Waiter with most tip money gets bonus at the end)
     */
    @Override
    public void action() {
        
    }
}