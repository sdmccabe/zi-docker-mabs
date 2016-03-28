import random


class SellerAgent:
    def __init__(self, max_seller_cost):
        self.cost = random.random() * max_seller_cost
        self.max_cost = max_seller_cost
        self.traded = False
        self.lock = False

    def acquire(self):
        while self.lock:
            pass
        self.lock = True

    def release(self):
        self.lock = False

    def form_ask_price(self):
        return self.cost + random.random() * (self.max_cost - self.cost)
