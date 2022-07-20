import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String[] days = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        Date startDateObj = new Date();
        List<Map<String, Integer>> hashMaps = new ArrayList<Map<String, Integer>>();
        for (int queryDate = 220418; queryDate <= 220424; queryDate++) {
            System.out.println("Querying " + days[queryDate - 220418] + "...");
            Map<String, Integer> stationsHashMap = new HashMap<String, Integer>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("trains-060522-full.cif")));
                String line;
                boolean serviceValid = false;
                while ((line = br.readLine()) != null) {
                    switch (line.substring(0, 2)) {
                        case "BS":
                            if (line.charAt(79) != 'P') {
                                serviceValid = false;
                                continue;
                            }
                            char trainStatus = line.charAt(29);
                            if (!(trainStatus == 'P' || trainStatus == 'T' || trainStatus == '1' || trainStatus == '3')) {
                                serviceValid = false;
                                continue;
                            }
                            String startDate = line.substring(9, 15);
                            String endDate = line.substring(15, 21);
                            int startDateInt = Integer.parseInt(startDate);
                            int endDateInt = Integer.parseInt(endDate);
                            if (endDateInt < queryDate || startDateInt > queryDate) {
                                serviceValid = false;
                                continue;
                            }
                            if (line.charAt(21 + queryDate - 220418) != '1') {
                                serviceValid = false;
                                continue;
                            }
                            serviceValid = true;
                            break;
                        case "LI":
                        case "LO":
                        case "LT":
                            char stopCode = line.charAt(42);
                            if (line.substring(0, 2).equals("LI") && !(stopCode == 'T' || stopCode == 'U' || stopCode == 'D')) continue;
                            if (serviceValid) {
                                String stationId = line.substring(2, 9);
                                stationsHashMap.merge(stationId, 1, Integer::sum);
                            }
                            break;
                    }
                }
                br.close();
                hashMaps.add(stationsHashMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            PrintWriter writer = new PrintWriter("output.txt", StandardCharsets.UTF_8);
            for (String key : hashMaps.get(0).keySet()) {
                String arrayString = "";
                for (int i = 0; i < 7; i++) {
                    Integer amount = hashMaps.get(i).get(key);
                    arrayString += amount == null ? 0 : amount;
                    arrayString += (i == 6) ? "\n" : " ";
                }
                writer.write(key + " " + arrayString);
            }
            writer.close();

            Date endDate = new Date();
            long timeDiff = endDate.getTime() - startDateObj.getTime();
            System.out.println("Completed in " + timeDiff + "ms.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
