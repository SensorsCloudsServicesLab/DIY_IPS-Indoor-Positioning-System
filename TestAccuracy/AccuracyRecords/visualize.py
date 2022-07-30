import json
import matplotlib.pyplot as plt
from Position import Position
from accuracy_test import get_position_error_map

def load_json_data(file):
    data = ""
    with open(file, "r") as f:
        data = f.read()
    return json.loads(data)

def draw_boxplot(file, joe_model):
    points = [Position(1,1), Position(1,5), Position(2,10), Position(5,4), Position(9,4), Position(9,1), Position(11,2), Position(6,1), Position(7,6), Position(4,9), Position(4,3), Position(6,3), Position(6,5), Position(4,5)]
    points_labels = [ p.__repr__() for p in points]
    position_error_map = get_position_error_map(file, joe_model)
    all_error = []
    for pos in points:
        error_list = position_error_map[pos]
        all_error.append(error_list)
    plt.boxplot(all_error, labels=points_labels)
    plt.ylim((0, 12))
    plt.xlabel('Points')
    plt.ylabel('Error')
    plt.show()

if __name__ == '__main__':
    # draw_boxplot('accuracy_records_joe_model.json', True)
    draw_boxplot('accuracy_records.json', False)