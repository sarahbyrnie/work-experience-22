import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

// To compile, run: javac Main.java

public class Main {
    public static void main(String[] args) {
        String[] days = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        // This code creates two output files:
        // ungrouped_output.txt has all the stations with the total number of all services across a week
        // stoptypes_output.txt has all the stations with the number of all services across a week, broken down by the stop type (LO, LI, or LT)

        Date startDateObj = new Date(); // used to check performance of code (not necessary)

        List<Map<String, Integer>> hashMaps = new ArrayList<Map<String, Integer>>();

        // For each station, it stores an array of the counts of the number of services of different types.
        // e.g. for WATRLMN it might store [300, 100, 300]
        // These are the LO, LI, and LT services respectively
        Map<String, Integer> arrayIndices = new HashMap<String, Integer>();
        arrayIndices.put("LO", 0);
        arrayIndices.put("LI", 1);
        arrayIndices.put("LT", 2);

        // Initialise the list to store all the hash maps
        // (one hash map in the list is for one day)
        List<Map<String, Integer[]>> stopTypesHashMaps = new ArrayList<Map<String, Integer[]>>();
        for (int i = 0; i < 7; i++) {
            stopTypesHashMaps.add(new HashMap<String, Integer[]>());
        }
        for (int queryDate = 220418; queryDate <= 220424; queryDate++) {
            System.out.println("Querying " + days[queryDate - 220418] + "...");
            Map<String, Integer> stationsHashMap = new HashMap<String, Integer>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("trains-060522-full.cif")));
                String line;
                boolean serviceValid = false;
                // Loop through the lines of the file
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
                                String stationId = line.substring(2, 9);
                                // For ungrouped_output.txt:
                                stationsHashMap.merge(stationId, 1, Integer::sum); // Add 1 to the count for this station

                                // For stoptypes_output.txt:
                                if (stopTypesHashMaps.get(queryDate - 220418).containsKey(stationId)) {
                                    // If the array has already been initialised, add one to the correct array item
                                    // depending on whether this stop is LO, LI, or LT
                                    stopTypesHashMaps.get(queryDate - 220418).get(stationId)[arrayIndices.get(line.substring(0, 2))]++;
                                } else {
                                    // If it hasn't been initialised, initialise it
                                    Integer[] blankArray = new Integer[]{0, 0, 0};
                                    blankArray[arrayIndices.get(line.substring(0, 2))]++;
                                    stopTypesHashMaps.get(queryDate - 220418).put(stationId, blankArray);
                                }
                            }
                            break;
                    }
                }
                br.close();
                // Add the hash map for this day to the list for the whole week
                hashMaps.add(stationsHashMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            PrintWriter writer = new PrintWriter("ungrouped_output.txt", StandardCharsets.UTF_8);
            for (String key : hashMaps.get(0).keySet()) {
                // Format the data for each station into the output format
                // i.e., at the moment it is stored as seven hash maps
                // Get all the data from all the hash maps for each station
                String arrayString = "";
                for (int i = 0; i < 7; i++) {
                    Integer amount = hashMaps.get(i).get(key);
                    arrayString += amount == null ? 0 : amount;
                    arrayString += (i == 6) ? "\n" : " ";
                }
                writer.write(key + " " + arrayString);
            }
            writer.close();
            PrintWriter writer2 = new PrintWriter("stoptypes_output.txt", StandardCharsets.UTF_8);
            for (String key : stopTypesHashMaps.get(0).keySet()) {
                // Again, get all the data from all the hash maps
                // (and remove the spaces from the array so it's slightly easier to parse)
                String arrayString = "";
                for (int i = 0; i < 7; i++) {
                    arrayString += stopTypesHashMaps.get(i).get(key) == null ? "[0,0,0]" : Arrays.toString(stopTypesHashMaps.get(i).get(key)).replace(" ", "");
                    arrayString += (i == 6) ? "\n" : " ";
                }
                writer2.write(key + " " + arrayString);
            }
            writer2.close();

            // Below is not necessary, just produces output of how long the code took to run
            Date endDate = new Date();
            long timeDiff = endDate.getTime() - startDateObj.getTime();
            System.out.println("Completed in " + timeDiff + "ms.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
