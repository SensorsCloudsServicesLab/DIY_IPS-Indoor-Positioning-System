from itertools import product
import numpy as np
from matplotlib import pyplot as plt

from sklearn.gaussian_process import GaussianProcessRegressor
from sklearn.gaussian_process.kernels import RBF

class GPRModel:

    def __init__(self, outputResolution):
        self.outputResolution = outputResolution
        self.trained = False

        kernel = RBF(1.0)
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
        while True:
            self.gp.fit(X, Z)
            
            #Predict the z values based at each xy coordinate of the prediction input space
            self.xy = np.array(list(product(x1, x2)))
            self.z_pred= self.gp.predict(self.xy)
            self.trained = True

            #Sometimes the fitting goes wrong, and predicts all values to be approximately zero. This code detects this case and retrains the model if it happens
            #The strategy is to get the highest observed value, then predict a value very close to it. If the percentage difference is more than 25%, then it must be wrong.
            highest_abs_value = max(values, key=abs)
            coord_of_highest_abs_value = coords[values.index(highest_abs_value)]
            test_result = self.gp.predict([(coord_of_highest_abs_value[0] + 0.1, coord_of_highest_abs_value[1] + 0.1)])
            if abs((test_result - highest_abs_value)/highest_abs_value) < 0.25:
                self.plot3D()
                break
            else:
                print("Fitted incorrectly. Recalculating...")

    def plot2D(self):
        if not self.trained:
            return

        x_prediction = self.xy[:,0].reshape(self.x1_number_of_points, self.x2_number_of_points)
        y_prediction = self.xy[:,1].reshape(self.x1_number_of_points, self.x2_number_of_points)
        z_prediction = np.reshape(self.z_pred,(self.x1_number_of_points,self.x2_number_of_points))

        fig = plt.figure(figsize=(10,8))
        ax = fig.add_subplot(111)
        ax.pcolormesh(x_prediction, y_prediction, z_prediction, cmap='jet')
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
