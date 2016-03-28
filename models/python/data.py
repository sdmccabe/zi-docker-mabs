class Data:
    def __init__(self):
        self.N = 0
        self.min = 1000000000.0
        self.max = 0.0
        self.sum = 0.0
        self.sum2 = 0.0

    def consolidate(self, data):
        for d in data:
            self.N += d.N
            self.min = min(self.min, d.min)
            self.max = max(self.max, d.max)
            self.sum += d.sum
            self.sum2 += d.sum2


    def add_datuum(self, datuum):
        self.N += 1
        if datuum < self.min:
            self.min = datuum
        elif datuum > self.max:
            self.max = datuum
        self.sum += datuum
        self.sum2 += datuum ** 2

    def get_average(self):
        if self.N > 0:
            return self.sum / self.N
        else:
            return 0.0

    def get_variance(self):
        if self.N > 1:
            avg = self.get_average()
            arg = self.sum2 - self.N * avg ** 2
            return arg / (self.N - 1)
        else:
            return 0.0

    def get_std_dev(self):
        return self.get_variance() ** 0.5

