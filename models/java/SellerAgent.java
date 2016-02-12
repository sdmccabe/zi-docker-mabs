/*
 * SellerAgent class
 *
 * CSS 610	
 * George Mason University			
 * Spring 2005
 *
 * Rob Axtell
 *
 */

import java.util.concurrent.*;
public class SellerAgent {

    private double cost;
    private double maxCost;
    public boolean traded;
    public double price;
    public Semaphore lock;

    public SellerAgent(double maxSellerCost) {
        cost = ThreadLocalRandom.current().nextDouble(0, 1) * maxSellerCost;
        maxCost = maxSellerCost;
        traded = false;
	lock = new Semaphore(1);
    }

    public void setTraded(boolean t) {
        traded = t;
    }

    public boolean hasTraded() {
        return (traded);
    }

    public double formAskPrice() {
        //return cost + Math.random() * (maxCost - cost);
        return cost + ThreadLocalRandom.current().nextDouble(0, 1) * (maxCost - cost);
    }
}
