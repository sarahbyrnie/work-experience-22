import matplotlib.pyplot as plt
import numpy as np

station_data = {}

with open("services_info.txt") as services_info_file:
    for line in services_info_file:
        data = line.split()
        figures = [0, 0, 0, 0, 0, 0, 0]
        for i in range(1, 8):
            figures[i - 1] = int(data[i])
        station_data[data[0]] = figures

fig, ax = plt.subplots()

for key, value in station_data.items():
    ax.plot(range(7), value, label=key, marker="x")

# ax.legend(loc="upper left")

ax.set_xticks(range(7))
ax.set_xticklabels(["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"])

ax.set(ylim=(0, 1500))

plt.show()