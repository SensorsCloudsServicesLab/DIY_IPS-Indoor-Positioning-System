import json

def load_json_data(file):
    data = ""
    with open(file, "r") as f:
        data = f.read()
    return json.loads(data)

"""
    Check if all samples in dataset has all 8 WiFi APs
"""
def is_clean(file):
    json_array = load_json_data(file)
    count = 0
    points = dict()
    for json_obj in json_array:
        rssi_observations = json_obj.get("rssi_observations")
        if len(rssi_observations) != 8:
            x = json_obj.get("ref_x")
            y = json_obj.get("ref_y")
            points[x] = y
            count+=1
    if count != 0:
        print("{} is not clean, there are {} samples don't have all 8 APs".format(file, count))
        result = []
        for key in points.keys():
            result.append((key, points[key]))
        print("The following points have missing APs: {}".format(result))
    else:
        print("{} is clean".format(file))

"""
    Remove the samples with less that 8 APs
"""
def clean_dataset(input_file, output_file):
    json_array = load_json_data(input_file)
    new_json_array = []
    for json_obj in json_array:
        rssi_observations = json_obj.get("rssi_observations")
        if len(rssi_observations) == 8:
            new_json_array.append(json_obj)
    with open(output_file, 'w') as f:
        f.write(json.dumps(new_json_array))

if __name__ == '__main__':
    is_clean('local_records.json')