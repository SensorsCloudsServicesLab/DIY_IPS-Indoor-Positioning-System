from statistics import mean, stdev
import matplotlib.pyplot as plt
import numpy as np
import json
import os


# specify the location, direction, ssid of the records to compute rssi distribution
X = 1
Y = 1
DIRECTION = ""
SSID = "SCSLAB_AP_1_5GHZ"


"""
    Represent a single record in records.json
"""
class Fingerprint:
    """
        x: x position of the location
        y: y position of the location
        angle_predictor: a function return True if it is target direction
        ssid: ssid of WiFi
    """
    def __init__(self, x, y, angle_predictor, ssid):
        self.x = x
        self.y = y
        self.angle_predictor = angle_predictor
        self.ssid = ssid
    
    def is_match(self, json_obj):
        x = json_obj.get("ref_x")
        y = json_obj.get("ref_y")
        angle = json_obj.get("angle")
        if x == self.x and y == self.y and self.angle_predictor(angle):
            return True
        else:
            return False

def angle_predictor_for_joe(angle):
    if DIRECTION == "N":
        if angle > 315 or angle <= 45:
            return True
        else:
            return False
    elif DIRECTION == "E":
        if angle > 45 and angle <= 135:
            return True
        else:
            return False
    elif DIRECTION == "S":
        if angle > 135 and angle <= 225:
            return True
        else:
            return False
    elif DIRECTION == "W":
        if angle > 225 and angle <= 315:
            return True
        else:
            return False
    # means compute all 4 directions
    else:
        return True

def angle_predictor_for_riccardo(angle):
    if DIRECTION == "N":
        if angle > -45 and angle <= 45:
            return True
        else:
            return False
    elif DIRECTION == "E":
        if angle > 45 and angle <= 135:
            return True
        else:
            return False
    elif DIRECTION == "S":
        if (angle > 135 and angle <= 180) or (angle <= -135 and angle >= -180):
            return True
        else:
            return False
    elif DIRECTION == "W":
        if angle <= -45 and angle > -135:
            return True
        else:
            return False
    # means compute all 4 directions
    else:
        return True

"""
    load json from file
"""
def load_json_data(file):
    data = ""
    with open(file, "r") as f:
        data = f.read()
    return json.loads(data)

"""
    Create directories images
"""
def initDirectories():
    d1 = "images/joe"
    d2 = "images/riccardo"
    if not os.path.exists(d1):
        os.makedirs(d1)
    if not os.path.exists(d2):
        os.makedirs(d2)

def draw_distribution(input_file_name, output_file_name, target_fingerprint):
    json_array = load_json_data(input_file_name)
    rssi_observation_list = []
    for data in json_array:
        if target_fingerprint.is_match(data):
            rssi_observations = data.get("rssi_observations")
            for obs in rssi_observations:
                if obs.get("SSID") == target_fingerprint.ssid:
                    rssi_observation_list.append(obs.get("RSSI"))
                    break
    
    plt.hist(rssi_observation_list, 20)
    plt.xlabel("RSSI value")
    plt.ylabel("count")
    # plt.title(output_file_name)
    plt.show()
    # plt.savefig(output_file_name)
    plt.close()

if __name__ == '__main__':
    initDirectories()
    f_joe = Fingerprint(X, Y, angle_predictor_for_joe, SSID)
    f_ric = Fingerprint(X, Y, angle_predictor_for_riccardo, SSID)
    output_joe = "images/joe/({},{})_{}_{}.png".format(X, Y, DIRECTION, SSID)
    output_riccardo = "images/riccardo/({},{})_{}_{}.png".format(X, Y, DIRECTION, SSID)
    draw_distribution('joe_records.json', output_joe, f_joe)
    draw_distribution('riccardo_records.json', output_riccardo, f_ric)