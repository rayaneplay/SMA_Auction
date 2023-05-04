import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.core.behaviours.Behaviour;
public class Vendeur extends Agent {

    private String itemName;
    private int reservePrice;

    private int itemPrice;

    private int Bestbid;
    private boolean itemSold;
    private String winningBuyer;

    private int NumberofRounds;

    protected void setup() {

        // Initialize item for sale
        itemName = "Item 1";
        reservePrice = 500;
        itemPrice = 20;
        Bestbid = itemPrice;
        itemSold = false;
        winningBuyer = "";
        NumberofRounds = 3;


        System.out.println("Seller agent " + getAID().getLocalName() + " is ready.");
        // Send initial message with item price to Acheteur
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("Acheteur");
                sd.setName("Auction");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    for (DFAgentDescription dfAgentDescription : result) {

                            ACLMessage itemPriceMsg = new ACLMessage(ACLMessage.REQUEST);
                            itemPriceMsg.addReceiver(dfAgentDescription.getName());
                            itemPriceMsg.setContent(String.valueOf(itemPrice));
                            send(itemPriceMsg);
                            System.out.println("seller sent inital item price: "+ itemPrice);


                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });


        // Add behaviour to receive initial price message
        addBehaviour(new CyclicBehaviour() {

            public void action() {

                ACLMessage Bidmsg = receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
                if (Bidmsg != null) {
                    if (Bestbid < Integer.parseInt(Bidmsg.getContent()))
                    {
                        // Envoyer le nouveau meilleur offre à tous les acheteurs
                        winningBuyer = Bidmsg.getSender().getLocalName();
                        Bestbid = Integer.parseInt(Bidmsg.getContent());
                        DFAgentDescription template1 = new DFAgentDescription();
                        ServiceDescription sd1 = new ServiceDescription();
                        sd1.setType("Acheteur");
                        template1.addServices(sd1);
                        try {
                            DFAgentDescription[] result = DFService.search(myAgent, template1);
                            for (DFAgentDescription dfAgentDescription : result) {
                                if(!dfAgentDescription.getName().getLocalName().equals(winningBuyer)){
                                    ACLMessage priceMsg = new ACLMessage(ACLMessage.REQUEST);
                                    priceMsg.addReceiver(dfAgentDescription.getName());
                                    priceMsg.setContent(Integer.toString(Bestbid));
                                    send(priceMsg);
                                    System.out.println("seller sent best bid :" + Bestbid);
                                }

                            }
                        } catch (FIPAException fe) {
                            fe.printStackTrace();
                        }

                    }

                } else {
                    block();
                }



                ACLMessage Quitmsg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (Quitmsg != null) {
                    System.out.println(Quitmsg.getContent());
                } else {
                    block();
                }


            }



        });

    }
    protected void takeDown() {
        System.out.println("Seller agent " + getAID().getName() + " is terminating.");
    }
}
