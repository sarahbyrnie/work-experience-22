import matplotlib.pyplot as plt
import pandas as pd

station_data = {}

column_names = ["operator_id"]
for i in range(24):
    column_names.append("hour_" + str(i))

with open("operators_info.txt") as operators_info_file:
    for line in operators_info_file:
        data = line.split()
        for i in range(1, 25):
            data[i] = int(data[i])
        station_data[data[0]] = data[1:]
        station_data[data[0]] = station_data[data[0]][2:] + station_data[data[0]][:2]

fig, ax = plt.subplots()

for key, value in station_data.items():
    print(value)
    ax.plot(range(2, 26), value, label=key, marker="x")

ax.legend(loc="upper left")
ax.set_xlabel("hour of day")
ax.set_ylabel("number of trains")
ax.set_title("Trains per operator at Birmingham New Street")

ax.set_xticks(range(2, 26))

plt.show()