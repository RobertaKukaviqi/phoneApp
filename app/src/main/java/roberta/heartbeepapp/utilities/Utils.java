package roberta.heartbeepapp.utilities;

import android.content.Context;
import android.util.DisplayMetrics;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.util.ArrayList;
import java.util.Map;

public class Utils {

    public static LocalDate getWeekStart(LocalDate calendar){
        LocalDate date = calendar;
        date = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return date;
    }

    public static String timeToString(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"));
    }

    public static String formalTimeString(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm"));
    }

    public static String timeToShortString(LocalDate date){
        return date.format(DateTimeFormatter.ofPattern("dd MMM"));
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static ArrayList<Map.Entry<String, Object>> weekDataDescendingSort(ArrayList<Map.Entry<String, Object>> wd){
        ArrayList<Map.Entry<String, Object>> result = wd;

        for(int i = 0; i < result.size() - 1; i++){
            for(int j = i + 1; j < result.size(); j++){
                if(result.get(j).getKey().compareToIgnoreCase(result.get(i).getKey()) >= 0){
                    Map.Entry<String, Object> temp = result.get(i);
                    result.set(i, result.get(j));
                    result.set(j, temp);
                }
            }
        }


        return result;
    }

    public static float calculateAverage(Map<String, Object> values) {
        long sum = 0;
        for(Map.Entry<String, Object>entry: values.entrySet()) {
            sum += (long)entry.getValue();
        }

        return (float)sum/values.size();
    }

}
