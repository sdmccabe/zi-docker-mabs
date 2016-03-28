import random

class BuyerAgent:
    def __init__(self, max_buyer_value):
        self.value = random.random() * max_buyer_value
        self.traded = False
        self.lock = False

    def acquire(self):
        while self.lock:
            pass
        self.lock = True

    def release(self):
        self.lock = False

    def form_bid_price(self):
        return random.random() * self.value
