import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

// To compile, run: javac TimeSearch.java

public class TimeSearch {
    public static void main(String[] args) {
        List<Map<String, Integer>> hashMaps = new ArrayList<Map<String, Integer>>();

        // Search for all services on this date (yymmdd)
        int queryDate = 220418;

        // Loop through every hour of the day
        for (int timeHourStart = 0; timeHourStart < 24; timeHourStart++) {
            System.out.println("Querying " + timeHourStart + "h");
            // Hash map stores station ID as key, and the number of services at this station
            // in that hour as the value
            Map<String, Integer> thisHourCounts = new HashMap<String, Integer>();
            try {
                // Read in every line of the file
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("trains-060522-full.cif")));
                String line;
                boolean serviceValid = false;
                while ((line = br.readLine()) != null) {
                    switch (line.substring(0, 2)) {
                        case "BS":
                            // Check if the BS header refers to a new entry (i.e., not cancellation)
                            if (line.charAt(79) != 'P') {
                                serviceValid = false;
                                continue;
                            }
                            // Check if the service is a passenger service
                            char trainStatus = line.charAt(29);
                            if (!(trainStatus == 'P' || trainStatus == 'T' || trainStatus == '1' || trainStatus == '3')) {
                                serviceValid = false;
                                continue;
                            }
                            // Check if the date range in the BS header indicates that the service will run on the queryDate
                            String startDate = line.substring(9, 15);
                            String endDate = line.substring(15, 21);
                            int startDateInt = Integer.parseInt(startDate);
                            int endDateInt = Integer.parseInt(endDate);
                            if (endDateInt < queryDate || startDateInt > queryDate) {
                                serviceValid = false;
                                continue;
                            }
                            // Check if the weekday 0s and 1s mean that the service will run on the queryDate
                            if (line.charAt(21 + queryDate - 220418) != '1') {
                                serviceValid = false;
                                continue;
                            }
                            // If everything above passes, the service is OK to get data from
                            serviceValid = true;
                            break;
                        case "LI":
                        case "LO":
                        case "LT":
                            // Only look for stops where the train is a regular stop (i.e., not a pass through)
                            char stopCode = line.charAt(42);
                            if (line.substring(0, 2).equals("LI") && !(stopCode == 'T' || stopCode == 'U' || stopCode == 'D')) continue;
                            if (serviceValid) {
                                // Extract info from line: station ID, scheduled arrival, scheduled departure, scheduled pass
                                String stationId = line.substring(2, 9);
                                String schArrString = line.substring(10, 14);
                                String schDepString = line.substring(15, 19);
                                String schPassString = line.substring(20, 24);
                                // Cast to integers
                                int schArr = -1;
                                int schDep = -1;
                                int schPass = -1;
                                try {
                                    schArr = Integer.parseInt(schArrString);
                                } catch (Exception e) {}
                                try {
                                    schDep = Integer.parseInt(schDepString);
                                } catch (Exception e) {}
                                try {
                                    schPass = Integer.parseInt(schPassString);
                                } catch (Exception e) {}
                                // If the service has a departure time, use that. If not, then arrival. If not, then pass.
                                int serviceTime = -1;
                                if (schDep != -1) {
                                    serviceTime = schDep;
                                } else if (schArr != -1) {
                                    serviceTime = schArr;
                                } else {
                                    serviceTime = schPass;
                                }
                                // If the service time is within the current hour being queried, add one to the count of services at the station
                                if (serviceTime >= timeHourStart * 100 && serviceTime <= (timeHourStart + 1) * 100) {
                                    thisHourCounts.merge(stationId, 1, Integer::sum);
                                }
                            }
                            break;
                    }
                }
                br.close();
                // Add the hash map for this hour to a list of all the hash maps for all the hours
                hashMaps.add(thisHourCounts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            PrintWriter writer = new PrintWriter("hours_output.txt", StandardCharsets.UTF_8);
            // Change the way the data is stored so that it is appropriate to write to file.
            // collatedData stores the station ID as key, and an array of integers as value.
            // The array stores all the counts for all the hours.
            // e.g., key WATRLMN will have value [3, 0, 0, 1, 5, 10, etc.
            Map<String, Integer[]> collatedData = new HashMap<String, Integer[]>();
            for (int i = 0; i < 24; i++) {
                Map<String, Integer> currentMap = hashMaps.get(i);
                for (String key : currentMap.keySet()) {
                    if (collatedData.containsKey(key)) {
                        collatedData.get(key)[i] = currentMap.get(key);
                    } else {
                        Integer[] blankArray = new Integer[24];
                        for (int j = 0; j < 24; j++) blankArray[j] = (j == i) ? currentMap.get(key) : 0;
                        collatedData.put(key, blankArray);
                    }
                }
            }
            // For each of the keys, write the array data to file
            // The nasty replace calls change the format from e.g. [3, 1, 2] to 3 1 2
            for (String key : collatedData.keySet()) {
                writer.write(key + " " + Arrays.toString(collatedData.get(key)).replace(" ", "").replace("[", "").replace("]", "").replace(",", " ") + "\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
