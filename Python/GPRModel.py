from itertools import product
import numpy as np
from matplotlib import pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

from sklearn.gaussian_process import GaussianProcessRegressor
from sklearn.gaussian_process.kernels import RBF

from RoomSimulator import RoomSimulator

class GPRModel:

    def __init__(self, outputResolution):
        self.outputResolution = outputResolution
        self.trained = False

        kernel = RBF([5,5], (2e-1, 2e1))
        self.gp = GaussianProcessRegressor(kernel=kernel, n_restarts_optimizer=15)

    def train(self, coords, values):
        X = np.array(coords)
        Z = np.array(values)

        #Increase the resolution of the output by a factor of the trained points
        self.x1_number_of_points = (int) ((X[:,0].max() - X[:,0].min()) / self.outputResolution) + 1
        self.x2_number_of_points = (int) ((X[:,1].max() - X[:,1].min()) / self.outputResolution) + 1

        #Create the prediction Input space
        x1 = np.linspace(X[:,0].min(), X[:,0].max(), self.x1_number_of_points) #p
        x2 = np.linspace(X[:,1].min(), X[:,1].max(), self.x2_number_of_points) #q

        #Train the model
        self.gp.fit(X, Z)

        #Predict the z values based at each xy coordinate of the prediction input space
        self.xy = np.array(list(product(x1, x2)))
        self.z_pred, MSE = self.gp.predict(self.xy, return_std=True)
        self.trained = True

    def plot2D(self):
        if not self.trained:
            return

        x_prediction = self.xy[:,0].reshape(self.x1_number_of_points, self.x2_number_of_points)
        y_prediction = self.xy[:,1].reshape(self.x1_number_of_points, self.x2_number_of_points)
        z_prediction = np.reshape(self.z_pred,(self.x1_number_of_points,self.x2_number_of_points))

        fig = plt.figure(figsize=(10,8))
        ax = fig.add_subplot(111)
        ax.pcolormesh(x_prediction, y_prediction, z_prediction)
        plt.show()

    def plot3D(self):
        if not self.trained:
            return

        x_prediction = self.xy[:,0].reshape(self.x1_number_of_points, self.x2_number_of_points)
        y_prediction = self.xy[:,1].reshape(self.x1_number_of_points, self.x2_number_of_points)
        z_prediction = np.reshape(self.z_pred,(self.x1_number_of_points,self.x2_number_of_points))

        fig = plt.figure(figsize=(10,8))
        ax = fig.add_subplot(111, projection='3d')            
        ax.plot_surface(x_prediction, y_prediction, z_prediction, rstride=1, cstride=1, cmap='jet', linewidth=0, antialiased=False)
        plt.show()

    def get_distribution_data(self):
        if not self.trained:
            return

        coords = []
        values = []
        for i in range(0, len(self.xy)):
            coords.append((round(self.xy[i][0], 1), round(self.xy[i][1], 1)))
            values.append(self.z_pred[i])

        return (coords, values)
