import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
public class GUI {
    private static int numBuyers;

//    public static int getNumBuyers() {
//        return numBuyers;
//    }
//
//    public static void setNumBuyers(int numBuyers) {
//        numBuyers = numBuyers;
//    }

    public static void main(String[] args) {
        try {

            // Launch Jade runtime
            Runtime rt = Runtime.instance();
            rt.setCloseVM(true);

            // Create main container
            Profile pMain = new ProfileImpl();
            pMain.setParameter(Profile.GUI, "true");
            AgentContainer mc = rt.createMainContainer(pMain);

            // Create seller agent
            AgentController seller = mc.createNewAgent("Vendeur", Vendeur.class.getName(), new Object[]{});
            seller.start();

            // Create buyer agents
            numBuyers = 3;
            for (int i = 0; i < numBuyers; i++) {
                AgentController buyer = mc.createNewAgent("Acheteur" + i, Acheteur.class.getName(), new Object[]{});
                buyer.start();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
