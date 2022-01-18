import math
import numpy as np

class RoomSimulator:

    def __init__(self, room_width, room_height, observations_at_each_point = 10, noise_stddev = 2):
        self.room_width = room_width + 1
        self.room_height = room_height + 1
        self.observations_at_each_point = observations_at_each_point
        self.noise_stddev = noise_stddev
        self.access_points = [
            (0, 0),
            (0, 0),
            (0, room_height),
            (0, room_height),
            (room_width, 0),
            (room_width, 0),
            (room_width, room_height),
            (room_width, room_height)
        ]
        self.results = []

    def distance_to_rssi(self, d):
        return (-6 * d) - 20

    def noise(self):
        return np.random.normal(0, self.noise_stddev)

    '''
    results = {
        AP1: {
            (0,0) : [0,1,2,3,1,2,0,1],
            (0,1) : [0,1,2,3,1,2,0,1],
            ...
        },
        AP2: {
            (0,0) : [0,1,2,3,1,2,0,1],
            (0,1) : [0,1,2,3,1,2,0,1],
            ...
        },
    }
    '''
    def simulate(self):
        for access_point in self.access_points:
            coordinate_observations = {}
            for x in range(0, self.room_width, 1):
                for y in range(0, self.room_height, 1):
                    observations = []
                    for observation in range(0, self.observations_at_each_point):
                        distance_from_access_point = math.dist((x, y), access_point)
                        rssi = self.distance_to_rssi(distance_from_access_point) + self.noise()
                        observations.append(rssi)
                    coordinate_observations[(x,y)] = observations
            self.results.append(coordinate_observations)

    def get_ap_data(self, ap_number):
        observation_index_points = []
        observations = []
        for (point, observation_list) in self.results[ap_number].items():
            for observation in observation_list:
                observation_index_points.append([point[0], point[1]])
                observations.append(observation)

        return (observation_index_points, observations)

# rs = RoomSimulator(2, 2)
# rs.simulate()
# print(rs.get_ap_data(0))