import plotly.express as px
import pandas as pd

px.set_mapbox_access_token("<Mapbox access token>")

df = pd.read_csv("tiploc.csv")

with open("hours_info.txt") as hours_info_file:
    all_data = []
    for line in hours_info_file:
        data = line.split()
        for i in range(2, 25):
            all_data.append([data[0], i, int(data[i])])
    services_df = pd.DataFrame(data=all_data, columns=["stop_id", "hour", "count"])

combined_df = pd.merge(services_df, df, on="stop_id", how="left")

fig = px.scatter_mapbox(combined_df, lat="stop_lat", lon="stop_lon", hover_name="stop_name", size="count", animation_frame="hour", animation_group="count")
fig.update_layout(title="Stations")
fig.show()