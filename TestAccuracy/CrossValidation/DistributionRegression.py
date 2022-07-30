import json
from os import access
import os
from GPRModel import GPRModel

FOLD = 10
base = "model"

"""
    create directories to put data
"""
def initDirectories():
    for i in range(1, FOLD + 1):
        d = "{}/{}".format(base, i)
        if not os.path.exists(d):
            os.makedirs(d)

"""
    training using distributions.json and generate north_distributions.json etc.
"""
def training(source_dir, target_dir):
    with open('{}/distributions.json'.format(source_dir), 'r') as file:
        data_json = json.load(file)

    for (direction, direction_data) in data_json.items():
        with open("{}/{}_distributions.json".format(target_dir, direction), 'w') as file:
            directional_data_json = {}
            for (access_point_name, access_point_data) in direction_data.items():
                coords = []
                location_values_raw = []
                scale_values_raw = []
                skew_values_raw = []
                for distribution in access_point_data:
                    coords.append((distribution["x"], distribution["y"]))
                    location_values_raw.append(distribution["loc"])
                    scale_values_raw.append(distribution["scale"])
                    skew_values_raw.append((distribution["skew"]))
    
                location_model = GPRModel(0.2)
                location_model.train(coords, location_values_raw)
                scale_model = GPRModel(0.2)
                scale_model.train(coords, scale_values_raw)
                skew_model = GPRModel(0.2)
                skew_model.train(coords, skew_values_raw)
                
                location_coords, location_values = location_model.get_distribution_data()
                scale_coords, scale_values = scale_model.get_distribution_data()
                skew_coords, skew_values = skew_model.get_distribution_data()

                distribution_data = []
                for i in range(0, len(location_coords)):
                    point_data = {
                        'x' : location_coords[i][0],
                        'y' : location_coords[i][1],
                        'loc' : location_values[i],
                        'scale' : scale_values[i],
                        'skew' : skew_values[i]
                    }
                    distribution_data.append(point_data)
                directional_data_json[access_point_name] = distribution_data
            
            json.dump(directional_data_json, file, separators=(',', ':'))

if __name__ == "__main__":
    initDirectories()
    for i in range(1, FOLD + 1):
        src_dir = "pre/{}".format(i)
        target_dir = "{}/{}".format(base, i)
        training(src_dir, target_dir)