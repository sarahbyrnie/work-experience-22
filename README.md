# 2022 DAFNI Work Eperience
This is a repository for work completed by the 2022 work eperience student in DAFNI. Work will be towards the rail data ingestion epic.


## Planned Work
The initial plan for the work experience week is to create graphs to somehow display the data held in CIF files. Below are some initial ideas, which may be expanded on or disregarded in favour of other ideas as the week progresses:
 - graphs showing the number of trains run per day from varying operators
 - a heat map of arrivals and departures by region or nationwide


## Helpful links
Some links to helpful resources/documentation
 - [Pandas data frames](https://pandas.pydata.org/docs/reference/api/pandas.DataFrame.html)
 - [CIF schedule records](https://wiki.openraildata.com/index.php/CIF_Schedule_Records)
 - [matplotlib](https://matplotlib.org/) - with a link on the main page to examples including code blocks


# DOCKER
Build the container by being in the same dir as `Dockerfile` and running `docker build -t trains-model .`

Run the container with `docker run trains-model -v inputs/:/data/inputs/ -v outputs/:data/outputs/`

TODO:
- Add comments to the java file for so future devs can make changes
- Make the date info that is currently hardcoded either get its info from the cif header, or take in as an env variable
- Check the expected names of the input and output files
-
