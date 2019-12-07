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
        String agent, name, agentClass;
        String[] agentDetails, args;

        try
        {
            br = new BufferedReader(new FileReader(agentList));

            while((agent = br.readLine()) != null)
            {
                agentDetails = agent.split(" ");

                switch (agentDetails[0]) {
                    case "Kitchen":
                        agentClass = "agents.Kitchen";
                        args = Arrays.copyOfRange(agentDetails, 2, agentDetails.length);
                        break;

                    case "Waiter":
                        agentClass = "agents.Waiter";
                        args = new String[] {agentDetails[2]};
                        break;

                    case "Customer":
                        agentClass = "agents.Customer";
                        args = new String[] {agentDetails[2], agentDetails[3]};
                        break;

                    default:
                        System.out.println("Unknown agent " + agentDetails[0]);
                        continue;
                }

                name = agentDetails[1];

                try {
                    container.createNewAgent(name, agentClass, args).start();
                }
                catch(StaleProxyException e) {
                    e.printStackTrace();
                }
            }

            br.close();
        }
        catch(IOException e)
        {
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
