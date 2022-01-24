import json
from os import access

from GPRModel import GPRModel

with open('distributions.json', 'r') as file:
    data_json = json.load(file)

for (direction, direction_data) in data_json.items():

    with open(direction+"_distributions.json", 'w') as file:
        directional_data_json = {}
        for (access_point_name, access_point_data) in direction_data.items():
            
            print(direction + ": " + access_point_name)

            coords = []
            mu_values = []
            sigma_values = []
            for distribution in access_point_data:
                coords.append((distribution["ref_x"], distribution["ref_y"]))
                mu_values.append(distribution["mu"])
                sigma_values.append((distribution["sigma"]))

            muModel = GPRModel(0.1)
            muModel.train(coords, mu_values)
            sigmaModel = GPRModel(0.1)
            sigmaModel.train(coords, sigma_values)
            muCoords, muValues = muModel.get_distribution_data()
            sigmaCoords, sigmaValues = sigmaModel.get_distribution_data()

            distribution_data = []
            for i in range(0, len(muCoords)):
                point_data = {
                    'ref_x' : muCoords[i][0],
                    'ref_y' : muCoords[i][1],
                    'mu' : muValues[i],
                    'sigma' : sigmaValues[i]
                }
                distribution_data.append(point_data)
            directional_data_json[access_point_name] = distribution_data
        
        json.dump(directional_data_json, file, separators=(',', ':'))