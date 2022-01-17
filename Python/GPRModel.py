from itertools import product
import numpy as np
from matplotlib import pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

from sklearn.gaussian_process import GaussianProcessRegressor
from sklearn.gaussian_process.kernels import RBF, ConstantKernel as C

from RoomSimulator import RoomSimulator

class GPRModel:

    def __init__(self, width, height, inputResolution, outputResolution):
        pass

    def simulate_data(self):
        rs = RoomSimulator(4, 4)
        rs.simulate()
        return rs.get_ap_data(0)

    def train(self, coords, rssi):
        X = np.array(X)
        y = np.array(y)

        # Input space
        x1 = np.linspace(X[:,0].min(), X[:,0].max()) #p
        x2 = np.linspace(X[:,1].min(), X[:,1].max()) #q

        kernel = C(1.0, (1e-3, 1e3)) * RBF([5,5], (1e-2, 1e2))
        gp = GaussianProcessRegressor(kernel=kernel, n_restarts_optimizer=15)

        gp.fit(X, y)

        x1x2 = np.array(list(product(x1, x2)))
        y_pred, MSE = gp.predict(x1x2, return_std=True)

        X0p, X1p = x1x2[:,0].reshape(50,50), x1x2[:,1].reshape(50,50)
        Zp = np.reshape(y_pred,(50,50))

        fig = plt.figure(figsize=(10,8))
        # ax = fig.add_subplot(111)
        # ax.pcolormesh(X0p, X1p, Zp)

        ax = fig.add_subplot(111, projection='3d')            
        surf = ax.plot_surface(X0p, X1p, Zp, rstride=1, cstride=1, cmap='jet', linewidth=0, antialiased=False)

        plt.show()

    def get_gaussian_distribution_map(self):
        pass


model = GPRModel(8, 8, 0.5, 0.1)
coords, rssi = model.simulate_data()
model.train(coords, rssi)