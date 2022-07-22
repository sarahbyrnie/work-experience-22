import plotly.express as px
import pandas as pd

px.set_mapbox_access_token("<Mapbox access token>")

def use_plotly(df):
    fig = px.scatter_mapbox(df, lat="stop_lat", lon="stop_lon", hover_name="stop_name", size="count", animation_frame="day", animation_group="count")
    fig.update_layout(title="Stations")
    fig.show()


with open("services_info.txt") as services_info_file:
    all_data = []
    for line in services_info_file:
        data = line.split()
        for i in range(1, 8):
            all_data.append([data[0], i, int(data[i])])
    services_df = pd.DataFrame(data=all_data, columns=["stop_id", "day", "count"])


with open("stoptypes_info.txt") as stoptypes_info_file:
    all_data = []
    for line in stoptypes_info_file:
        station_data = []
        data = line.split()
        days = data[1:]
        for day in days:
            if day == "null":
                station_data.append("LI")
                continue
            day = day[1:len(day)-1]
            day_data = day.split(",")
            day_data[0] = int(day_data[0])
            day_data[1] = int(day_data[1])
            day_data[2] = int(day_data[2])
            if max(day_data) == day_data[0]:
                station_data.append("LO")
            elif max(day_data) == day_data[1]:
                station_data.append("LI")
            else:
                station_data.append("LT")
        for i in range(len(station_data)):
            all_data.append([data[0], i, station_data[i]])
    stoptypes_df = pd.DataFrame(data=all_data, columns=["stop_id", "day", "type"])


df = pd.read_csv("tiploc.csv")
geo_and_services_df = pd.merge(services_df, df, on="stop_id", how="left")
# geo_services_and_stoptypes_df = pd.merge(geo_and_services_df, stoptypes_df, on="stop_id", how="left")

use_plotly(geo_and_services_df)
