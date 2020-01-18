package app;

import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.ProfileImpl;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Restaurant {
    private static ContainerController container;

    public static void main(String[] args) {

        if(args.length < 1) {
            System.out.println("Usage: Restaurant <file_path>");
            return;
        }

        createContainer();
        readFile(args[0]);
    }

    private static void readFile(String file) {
        BufferedReader br;
        File agentList = new File(file);
        String agent;

        try
        {
            br = new BufferedReader(new FileReader(agentList));

            while((agent = br.readLine()) != null) {
                String[] agentParams = agent.split(" ");

                if(agentParams.length == 2)
                    createAgents(agentParams[0], Integer.parseInt(agentParams[1]));
            }

            br.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void createAgents(String agentType, int quantity) {
        String name, className;
        String[] args = new String[0];

        try {
            for(int i = 0; i < quantity; i++) {
                switch (agentType) {
                    case "Kitchen":
                        name = "K" + i;
                        className = "agents.Kitchen";
                        break;

                    case "Waiter":
                        Random rand = new Random();

                        name = "W" + i;
                        className = "agents.Waiter";
                        args = new String[] {String.valueOf(rand.nextBoolean())};
                        break;

                    case "Customer":
                        name = "C" + i;
                        className = "agents.Customer";
                        break;

                    default:
                        System.out.println("Unknown agent type: " + agentType);
                        return;
                }

                container.createNewAgent(name, className, args).start();
                TimeUnit.SECONDS.sleep(1);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createContainer() {
        //Get the JADE runtime interface (singleton)
        jade.core.Runtime runtime = jade.core.Runtime.instance();
        
        //Create a Profile, where the launch arguments are stored
        try {
            Profile profile = new ProfileImpl("launchprops.properties");

            //create a main agent container
            container = runtime.createMainContainer(profile);
        }
        catch(ProfileException e) {
            e.printStackTrace();
        }
    }
}
