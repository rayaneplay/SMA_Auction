import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Acheteur extends Agent {
    private boolean waitingForSellerResponse = false;
    private int currentBid;
    private int maxBid;
    private int numBids;
    private AID sellerAgent;

    protected void setup() {
        // Enregistrer l'agent comme un acheteur
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Acheteur");
        sd.setName("Auction");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Buyer agent " + getAID().getLocalName() + " is ready.");

        // Initialize bidding parameters
        currentBid = 0;
        maxBid = (int) (Math.random() * 901) + 100;//class random
        numBids = 10;
        sellerAgent = new AID("Vendeur", AID.ISLOCALNAME);
        // Add behaviour to receive initial price message
        addBehaviour(new CyclicBehaviour() {

            public void action() {
                //receive bestbid from seller

                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (msg != null) {
                    int bestBid = Integer.parseInt(msg.getContent());
                    System.out.println("Buyer agent " + getAID().getLocalName() + " received price of " + bestBid);
                    System.out.println("Buyer agent " + getAID().getLocalName() + " current bid is " + currentBid);

                    if (bestBid >= maxBid)
                    {
                        ACLMessage QuitMessage = new ACLMessage(ACLMessage.INFORM);
                        QuitMessage.setContent("Buyer "+ getAID().getName()+ " is out");
                        QuitMessage.addReceiver(getAID("Vendeur"));
                        send(QuitMessage);
                        doDelete();
                    }
                    else {
                        if (waitingForSellerResponse || currentBid == bestBid) {
                            block();
                        }
                        else {
//                          currentBid = bestBid + (int)(Math.random() * ((maxBid - bestBid) /2));
                            currentBid = (int) Math.round(bestBid + Math.min(maxBid - bestBid, (0.05 * maxBid)));
                            ACLMessage CurrentBidMessage = new ACLMessage(ACLMessage.PROPOSE);
                            CurrentBidMessage.setContent(String.valueOf(currentBid));
                            CurrentBidMessage.addReceiver(getAID("Vendeur"));
                            send(CurrentBidMessage);
                            System.out.println("message sent from buyer " + getAID().getLocalName() +" to seller : "+ currentBid);
                            waitingForSellerResponse = true;

                        }

                    }

                }
                else {
                    System.out.println("buyer "+ getAID().getLocalName()+" is blocked");
                    block();
                }
            }
            public void handleMessage(ACLMessage msg) {
                try {
                    if (msg.getSender().equals(sellerAgent) && Integer.parseInt(msg.getContent()) > 0) {
                        // Handle response from seller
                        waitingForSellerResponse = false;
                    }
                } catch (NumberFormatException e) {
                    // Handle exception when the content of the message is not a valid integer
                    System.out.println("Error: Message content is not a valid integer.");
                }
            }

        });

        /*addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                // Check if max number of bids reached
                if (numBids <= 0) {
                    System.out.println("Buyer agent " + getAID().getName() + " reached max number of bids.");
                    doDelete();
                }
                else {
                    // Send bid message to seller agent
                    ACLMessage bidMsg = new ACLMessage(ACLMessage.PROPOSE);
                    bidMsg.setContent(Integer.toString(currentBid));
                    bidMsg.addReceiver(sellerAgent);
                    send(bidMsg);

                    // Receive reply message from seller agent
                    ACLMessage reply = receive();
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                            // Bid accepted
                            System.out.println("Buyer agent " + getAID().getName() + " won the auction with a bid of " + currentBid);
                            doDelete();
                        }
                        else {
                            // Bid rejected, increment bid
                            int newBid = Integer.parseInt(reply.getContent()) + 1;
                            if (newBid <= maxBid) {
                                currentBid = newBid;
                                System.out.println("Buyer agent " + getAID().getName() + " bid " + currentBid);
                            }
                            else {
                                System.out.println("Buyer agent " + getAID().getName() + " reached maximum bid.");
                                doDelete();
                            }
                        }
                        numBids--;
                    }
                    else {
                        // No reply received, wait a bit
                        block();
                    }
                }
            }
        });*/
    }

    protected void takeDown() {
        System.out.println("Buyer agent " + getAID().getName() + " is terminating.");
    }
}