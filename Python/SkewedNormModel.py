from scipy.stats import skewnorm
import matplotlib.pyplot as plt
import numpy as np


class SkewedNormModel:

    def __init__(self, datapoints=None, alpha=None, loc=None, scale=None):
        if datapoints != None and (alpha != None or loc != None or scale != None):
            raise ValueError("You must either enter datapoints, or the alpha, loc, and scale parameters")

        if datapoints != None:
            self.datapoints = datapoints
            self.alpha, self.loc, self.scale = skewnorm.fit(datapoints)
        else:
            self.alpha = alpha
            self.loc = loc,
            self.scale = scale

    def get_probability(self, value):
        return skewnorm.pdf(value, self.alpha, self.loc, self.scale)

    def plot(self):
        fig, ax = plt.subplots(1, 1)
        x = np.linspace(self.loc - 10, self.loc + 10, 100)

        if (self.datapoints):
            plt.hist(data, bins=100, alpha=0.6, color='g')

        ax.plot(x, skewnorm.pdf(x, self.alpha, self.loc, self.scale) * 100,
            'r-', lw=5, alpha=0.6, label='skewnorm pdf')
        plt.show()

    def plotPDF(self):
        fig, ax = plt.subplots(1, 1)
        x = np.linspace(self.loc - 10, self.loc + 10, 100)

        ax.plot(x, skewnorm.pdf(x, self.alpha, self.loc, self.scale),
            'r-', lw=5, alpha=0.6, label='skewnorm pdf')
        plt.show()





data = [-69, -67, -68, -68, -67, -68, -68, -66, -70, -68, -71, -69, -69, -72, -70, -69, -68, -72, -70, -69, -67, -70, -72, -71, -67, -68, -67, -72, -67, -70, -71, -71, -69, -67, -68, -67, -70, -69, -67, -69, -68, -69, -69, -68, -67, -68, -68, -70, -70, -70, -71, -69, -69, -73, -68, -69, -69, -67, -70, -69, -68, -67, -70, -68, -67, -68, -66, -68, -70, -67, -68, -69, -67, -69, -70, -69, -69, -70, -69, -68, -67, -75, -70, -67, -71, -67, -68, -69, -67, -70, -69, -67, -71, -67, -68, -70, -69, -68, -69, -69]
model = SkewedNormModel(datapoints=data)
model.plot()
print(model.get_probability(-69))