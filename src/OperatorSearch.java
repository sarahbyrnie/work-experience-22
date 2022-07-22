import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

// To compile, run: javac OperatorSearch.java

public class OperatorSearch {
    public static void main(String[] args) {
        List<Map<String, Integer>> operatorQuantities = new ArrayList<Map<String, Integer>>();

        // Hard-coded station and date to query (not ideal)
        String queryStation = "BHAMNWS";
        int queryDate = 220418;

        // Loop through all the hours
        for (int timeHourStart = 0; timeHourStart < 24; timeHourStart++) {
            try {
                System.out.println(timeHourStart + "h");
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("trains-060522-full.cif")));
                String line;
                boolean serviceValid = false;
                String thisServiceOperator = "";
                Map<String, Integer> thisHourOperators = new HashMap<String, Integer>();
                // Loop through all lines in the file
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
                            thisServiceOperator = "";
                            break;
                        case "BX":
                            // Whenever a BX line is encountered, store the operator
                            // It's used for subsequent LO/LI/LT lines
                            thisServiceOperator = line.substring(11, 13);
                            break;
                        case "LI":
                        case "LO":
                        case "LT":
                            // Only look for stops where the train is a regular stop (i.e., not a pass through)
                            char stopCode = line.charAt(42);
                            if (line.substring(0, 2).equals("LI") && !(stopCode == 'T' || stopCode == 'U' || stopCode == 'D')) continue;
                            if (serviceValid) {
                                // If the stop isn't for the right station, we don't want it
                                String stationId = line.substring(2, 9).trim();
                                if (!stationId.equals(queryStation)) continue;

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
                                // If it has a departure time, use that. If not, then arrival. If not, then pass.
                                int serviceTime = -1;
                                if (schDep != -1) {
                                    serviceTime = schDep;
                                } else if (schArr != -1) {
                                    serviceTime = schArr;
                                } else {
                                    serviceTime = schPass;
                                }
                                // If the service time is within the current hour being queried, add one to the count of services for the operator
                                if (serviceTime >= timeHourStart * 100 && serviceTime <= (timeHourStart + 1) * 100) {
                                    thisHourOperators.merge(thisServiceOperator, 1, Integer::sum);
                                }
                            }
                            break;
                    }
                }
                br.close();
                // Add the hash map for this hour to a list of all the hash maps for all the hours
                operatorQuantities.add(thisHourOperators);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(operatorQuantities);
        try {
            // Reformat the data so it is stored primarily by operator rather than by hour
            // i.e., make it so e.g. VT stores [0, 1, 2, 3, etc. rather than 0h stores {VT=1, AW=3, etc.
            PrintWriter writer = new PrintWriter("operators_output.txt", StandardCharsets.UTF_8);
            Map<String, Integer[]> collatedData = new HashMap<String, Integer[]>();
            for (int i = 0; i < 24; i++) {
                // Loop through each of the hash maps for each of the hours
                Map<String, Integer> currentMap = operatorQuantities.get(i);
                for (String key : currentMap.keySet()) {
                    // Loop through all the operators in this hour
                    // If collatedData already has the array for the operator, update it
                    // if not, initialise the array
                    if (collatedData.containsKey(key)) {
                        collatedData.get(key)[i] = currentMap.get(key);
                    } else {
                        Integer[] blankArray = new Integer[24];
                        for (int j = 0; j < 24; j++) blankArray[j] = (j == i) ? currentMap.get(key) : 0;
                        collatedData.put(key, blankArray);
                    }
                }
            }
            // Make the arrays look nicer (remove brackets, commas)
            for (String key : collatedData.keySet()) {
                writer.write(key + " " + Arrays.toString(collatedData.get(key)).replace(" ", "").replace("[", "").replace("]", "").replace(",", " ") + "\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
