package Default;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class Main {
    static int daysInFuture = 5;
    static int derivatives = 2;

    public static void main(String[] args) throws Exception {
        URL url_us = new URL("https://covid19-tracker-10h.herokuapp.com/v1/us/timeline");
        //URL url_global = new URL("https://covid19-tracker-10h.herokuapp.com/v1/global/timeline");

        URLConnection myURLConnection = url_us.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));

        String inputLine;
        String rawData = null;
        while ((inputLine = in.readLine()) != null){
            rawData = inputLine;
            System.out.println(rawData);
        }
        in.close();

        Object obj = new JSONParser().parse(rawData);
        JSONObject jo = (JSONObject) obj;
        List<Long> newCasesByDay = new ArrayList<>();
        List<Long> newDeathsByDay = new ArrayList<>();
        List<Long> totalCasesByDay = new ArrayList<>();
        List<Long> totalDeathsByDay = new ArrayList<>();
        List<Long> totalRecoveriesByDay = new ArrayList<>();
        ArrayList myList = new ArrayList(jo.keySet());
        Collections.sort(myList);

        for(Object ob : myList) {
            Map oneDay = ((Map) jo.get(ob));
            Iterator<Map.Entry> itr = oneDay.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry pair = itr.next();
                switch ((String) pair.getKey()) {
                    case "new_daily_cases":
                        newCasesByDay.add((long) pair.getValue());
                        break;
                    case "new_daily_deaths":
                        newDeathsByDay.add((long) pair.getValue());
                        break;
                    case "total_cases":
                        totalCasesByDay.add((long) pair.getValue());
                        break;
                    case "total_deaths":
                        totalDeathsByDay.add((long) pair.getValue());
                        break;
                    case "total_recoveries":
                        totalRecoveriesByDay.add((long) pair.getValue());
                        break;
                }

            }
        }
        List[] data = new List[] {newCasesByDay, newDeathsByDay, totalCasesByDay, totalDeathsByDay, totalRecoveriesByDay};
        List<Integer> predictions = new ArrayList<>();
        for(List l : data) {
            List<int[]> n = new ArrayList<>();
            n.add(convertLongs(l));
            System.out.println(n.size());
            for (int i = 0; i < derivatives; i++) {
                n.add(derivativeOf(n.get(i)));
            }

            float[] m = new float[n.size()];
            m[n.size()-1] = averageOf(n.get(n.size()-1)); // this might need to change
            for (int i = n.size()-2; i >= 0; i--) {
                m[i] = m[i + 1] * daysInFuture + n.get(i)[n.get(i).length - 1];
            }

            predictions.add(Math.round(m[0]));
//            for (int j = 0; j < n.size(); j++){
//                System.out.print("\nn[" + j + "]: ");
//                for (int i : n.get(j)) {
//                    System.out.print(i + " ");
//                }
//            }
//
//            System.out.println("\n");
//            for (float d : m) {
//                System.out.println("m: " +  d);
//            }
//            System.out.println("------------------------------------------------------------------------");
        }

        String output = "{\"new_daily_cases\":" + predictions[0] + "}";
    }

    private static float averageOf(int[] integerArray) {
        float average = 0;
        for (int i : integerArray){
            average += (float) i;
        }
        average /= (integerArray.length-1);
        return average;
    }

    private static int[] derivativeOf(int[] n_0) {
        int[] n_1 = new int[n_0.length];
        n_1[0] = 0;
        for (int i = 1; i < n_0.length; i ++) {
            n_1[i] = n_0[i] - n_0[i-1];
        }
        return n_1;
    }

    public static int[] convertLongs(List<Long> longs) {
        int[] ret = new int[longs.size()];
        Iterator<Long> iterator = longs.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }
}
