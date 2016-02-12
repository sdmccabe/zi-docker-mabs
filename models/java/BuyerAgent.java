/*
 * BuyerAgent class
 *
 * CSS 610	
 * George Mason University			
 * Spring 2005
 *
 * Rob Axtell
 *
 */

import java.util.concurrent.*;

public class BuyerAgent {

    private double value;
    private boolean traded;
    public double price;
    public Semaphore lock;

    public BuyerAgent(double maxBuyerValue) {
        value = ThreadLocalRandom.current().nextDouble(0, 1) * maxBuyerValue;
        traded = false;
	lock = new Semaphore(1);
    }

    public void setTraded(boolean t) {
        traded = t;
    }

    public boolean hasTraded() {
        return (traded);
    }

    public double formBidPrice() {
        //return Math.random() * value;
        return ThreadLocalRandom.current().nextDouble(0, 1) * value;
    }
}
