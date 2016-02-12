/*
 * AgentPopulation class
 *
 * CSS 610
 * George Mason University
 * Summer 2009
 *
 * Rob Axtell
 *
 */

import java.util.concurrent.atomic.*;

public class AgentPopulation {

    public BuyerAgent[] buyers;
    public SellerAgent[] sellers;
    int nBuyers;
    int nSellers;

    public AgentPopulation(final int numBuyers, final double maxValue, final int numSellers, final double maxCost) {

        nBuyers = numBuyers;
        nSellers = numSellers;

        buyers = new BuyerAgent[nBuyers];
	sellers = new SellerAgent[nSellers];

        for (int i = 0; i < numBuyers; i++) {
            buyers[i] = new BuyerAgent(maxValue);
        }

        for (int i = 0; i < numSellers; i++) {
            sellers[i] = new SellerAgent(maxCost);
        }
    }

    public int getActiveBuyers() {

        int count = 0;

        for (int i = 0; i < nBuyers; i++) {
            if (buyers[i].hasTraded()) {
                count++;
            }
        }
        return (count);
    }

    public int getActiveSellers() {

        int count = 0;

        for (int i = 0; i < nSellers; i++) {
            if (sellers[i].hasTraded()) {
                count++;
            }
        }
        return (count);
    }
}
