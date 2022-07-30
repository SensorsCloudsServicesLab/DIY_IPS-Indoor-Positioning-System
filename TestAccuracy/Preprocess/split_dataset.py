import json

north_json_array = []
east_json_array = []
south_json_array = []
west_json_array = []

def load_json_data(file):
    data = ""
    with open(file, "r") as f:
        data = f.read()
    return json.loads(data)

def get_direction(angle):
    if angle > 315 or angle <= 45:
        return north_json_array
    if angle > 45 and angle <= 135:
        return east_json_array
    if angle > 135 and angle <= 225:
        return south_json_array
    if angle > 225 and angle <= 315:
        return west_json_array

def split_dataset(file_name):
    json_array = load_json_data(file_name)
    for json_obj in json_array:
        angle = json_obj.get('angle')
        direction_json_array = get_direction(angle)
        direction_json_array.append(json_obj)
    
    # write to files
    with open('north.json', 'w') as f:
        f.write(json.dumps(north_json_array))
    with open('east.json', 'w') as f:
        f.write(json.dumps(east_json_array))
    with open('south.json', 'w') as f:
        f.write(json.dumps(south_json_array))
    with open('west.json', 'w') as f:
        f.write(json.dumps(west_json_array))

if __name__ == "__main__":
    split_dataset('new.json')