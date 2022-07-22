import plotly.express as px
import pandas as pd

px.set_mapbox_access_token("<Mapbox access token>")

df = pd.read_csv("tiploc.csv")

with open("stoptypes_info.txt") as stoptypes_info_file:
    all_data = []
    for line in stoptypes_info_file:
        data = line.split()
        discrepancy_data = data[2]
        discrepancy_data = discrepancy_data[1:len(discrepancy_data)-1].split(",")
        for i in range(len(discrepancy_data)):
            discrepancy_data[i] = int(discrepancy_data[i])
        # positive means more arrivals, negative means more departures
        discrepancy = discrepancy_data[0] - discrepancy_data[2]
        all_data.append([data[0], discrepancy, abs(discrepancy)])
    discrepancy_df = pd.DataFrame(data=all_data, columns=["stop_id", "discrepancy", "abs_discrepancy"])

combined_df = pd.merge(discrepancy_df, df, on="stop_id", how="left")

# Remove all stations where there isn't any discrepancy (can comment out if don't want it)
combined_df = combined_df[combined_df["discrepancy"] != 0]

fig = px.scatter_mapbox(combined_df,
                        lat="stop_lat",
                        lon="stop_lon",
                        hover_name="stop_name",
                        size="abs_discrepancy",
                        color="discrepancy",
                        range_color=[-15, 15],
                        color_continuous_scale=["red", "white", "blue"],
                     )
fig.update_layout(title="Stations (blue represents more arrivals, red represents more departures)")
fig.show()