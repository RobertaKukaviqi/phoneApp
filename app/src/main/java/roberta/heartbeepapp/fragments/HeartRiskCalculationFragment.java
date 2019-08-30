package roberta.heartbeepapp.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Map;

import roberta.heartbeepapp.R;
import roberta.heartbeepapp.models.HeartRateValueEntity;
import roberta.heartbeepapp.utilities.Utils;

public class HeartRiskCalculationFragment extends Fragment {

    private static final String EXTRA_WEAR_ID = "EXTRA_WEAR_ID";
    private static final int WEEK_SIZE = 7;
    private static final int LEARNING_DAYS_SIZE = 14;
    private final double LOW_RISK_CONSTANT = 1.5f;
    private final double MEDIUM_RISK_CONSTANT = 2.25f;
    private final double HIGH_RISK_CONSTANT = 3f;
    private ArrayList<HeartRateValueEntity> latestSevenDaysData;
    private ArrayList<HeartRateValueEntity> learningDaysData;

    private double q1;
    private double q3;
    private double iqr;
    private int latestWeekDaysCount = 0;
    private int learningDaysCount = 0;
    private String wearId = "";

    public static Fragment getInstance(Activity activity, String wearId) {
        HeartRiskCalculationFragment fragment = new HeartRiskCalculationFragment();
        Bundle b = new Bundle();
        b.putString(EXTRA_WEAR_ID, wearId);
        fragment.setArguments(b);
        return fragment;
    }

    public HeartRiskCalculationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        wearId = getArguments().getString(EXTRA_WEAR_ID);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_heart_risk_calculation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        latestSevenDaysData = new ArrayList<>();
        initCalculations(view);
        getWeekData(view, LocalDate.now(), true, WEEK_SIZE);
    }

    private void initCalculations(View view) {
        View container = view.findViewById(R.id.container);
        View noEnoughDataTv = view.findViewById(R.id.no_data_tv);

        if (latestSevenDaysData != null) {
            if (wearId.equals("116976445062639913111"))
                Log.e("LatestWeekDaysCount", "" + latestSevenDaysData.size() + " " + latestWeekDaysCount);
        } else {
            if (wearId.equals("116976445062639913111"))
                Log.e("LatestWeekDaysCount", " " + latestWeekDaysCount);
        }
        if (latestWeekDaysCount == 0) {
            container.setVisibility(View.GONE);
            noEnoughDataTv.setVisibility(View.VISIBLE);
        } else {
            measureHeartRisk(view);
            container.setVisibility(View.VISIBLE);
            noEnoughDataTv.setVisibility(View.GONE);

        }
    }

    private void getWeekData(final View view, final LocalDate date, final boolean currentWeek, final int requiredDates) {
        FirebaseDatabase.getInstance().getReference("users")
                .child("wear")
                .child(wearId)
                .child("data")
                .child(Utils.getWeekStart(date).toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Object> wd = (Map<String, Object>) dataSnapshot.getValue();

                        if (wd == null || wd.isEmpty()) {
                            return;
                        }

                        ArrayList<Map.Entry<String, Object>> weekData = new ArrayList<>(wd.entrySet());

                        weekData = Utils.weekDataDescendingSort(weekData);

                        if (currentWeek)
                            latestWeekDaysCount = 0;

                        int weekLength = requiredDates;
                        if (weekData.size() < weekLength)
                            weekLength = weekData.size();

                        for (int index = 0; index < weekLength; index++) {
                            Map.Entry<String, Object> entry = weekData.get(index);

                            //separated days of the week
                            Map<String, Object> values = (Map<String, Object>) entry.getValue();

                            for (Map.Entry<String, Object> day : values.entrySet()) {
                                int indexOfSpace = day.getKey().indexOf(" ");
                                LocalDate date = LocalDate.parse(day.getKey().substring(0, indexOfSpace));
                                LocalTime time = LocalTime.parse(day.getKey().substring(indexOfSpace + 1));
                                latestSevenDaysData.add(new HeartRateValueEntity(LocalDateTime.of(date, time), (Long) day.getValue()));
                                if (wearId.equals("116976445062639913111"))
                                    Log.e("Data", LocalDateTime.of(date, time).toString() + " " + (Long) day.getValue());
                            }

                            latestWeekDaysCount++;
                        }

                        if (currentWeek && latestWeekDaysCount < WEEK_SIZE) {
                            getWeekData(view, date.minusWeeks(1), false, WEEK_SIZE - latestWeekDaysCount);
                        } else {
                            learningDaysData = new ArrayList<>();
                            if (weekData.size() > 0) {
                                LocalDate weekStart = Utils.getWeekStart(LocalDate.parse(weekData.get(weekData.size() - 1).getKey()));

                                //start collecting learning data from last week
                                getLearningWeeksData(view, weekStart, true, LEARNING_DAYS_SIZE);
                            } else {
                                learningDaysData.addAll(latestSevenDaysData);
                                initCalculations(view);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getLearningWeeksData(final View view, final LocalDate date, final boolean currentWeek, final int requiredDates) {
        FirebaseDatabase.getInstance().getReference("users")
                .child("wear")
                .child(wearId)
                .child("data")
                .child(Utils.getWeekStart(date).toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Object> wd = (Map<String, Object>) dataSnapshot.getValue();

                        if (wd == null || wd.isEmpty()) {
                            return;
                        }

                        ArrayList<Map.Entry<String, Object>> weekData = new ArrayList<>(wd.entrySet());

                        weekData = Utils.weekDataDescendingSort(weekData);

                        if (currentWeek)
                            learningDaysCount = 0;

                        int weekLength = requiredDates;
                        if (weekData.size() < weekLength)
                            weekLength = weekData.size();

                        for (int index = 0; index < weekLength; index++) {
                            Map.Entry<String, Object> entry = weekData.get(index);

                            //separated days of the week
                            Map<String, Object> values = (Map<String, Object>) entry.getValue();

                            for (Map.Entry<String, Object> day : values.entrySet()) {
                                int indexOfSpace = day.getKey().indexOf(" ");
                                LocalDate date = LocalDate.parse(day.getKey().substring(0, indexOfSpace));
                                LocalTime time = LocalTime.parse(day.getKey().substring(indexOfSpace + 1));
                                learningDaysData.add(new HeartRateValueEntity(LocalDateTime.of(date, time), (Long) day.getValue()));
                            }

                            learningDaysCount++;
                        }

                        Log.e("date", currentWeek + " " + latestWeekDaysCount);
                        if (currentWeek && latestWeekDaysCount < LEARNING_DAYS_SIZE) {
                            getLearningWeeksData(view, date.minusWeeks(1), false, LEARNING_DAYS_SIZE - learningDaysCount);
                        } else {
                            initCalculations(view);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private int measureHeartRisk(View view) {

        /*
         * train with learning data
         * */
        //sort data
        quickSort(learningDaysData, 0, learningDaysData.size() - 1);

        //find Q1
        q1 = findMedian(learningDaysData, 0, learningDaysData.size() / 2 - 1);

        //find Q3
        q3 = findMedian(learningDaysData, learningDaysData.size() / 2, learningDaysData.size() - 1);

        //find IQR
        iqr = q3 - q1;

        Log.d("Values", "q1 " + q1);
        Log.d("Values", "q3 " + q3);
        Log.d("Values", "IQR " + iqr);

        int lowRiskCount = 0;
        int mediumRiskCount = 0;
        int highRiskCount = 0;
        int noRiskCount = 0;

        Log.e("Risk", lowRiskCount + " " + mediumRiskCount + " " + highRiskCount + " " + noRiskCount);

        /*
         * Analise new data
         * */

        for (int i = 0; i < latestSevenDaysData.size(); i++) {
            double heartRateValue = latestSevenDaysData.get(i).getValue();

            if (heartRateValue > upperThresholdLowRisk() && heartRateValue <= upperThresholdMediumRisk()) {
                lowRiskCount++;
            } else if (heartRateValue > upperThresholdMediumRisk() && heartRateValue <= upperThresholdHighRisk()) {
                mediumRiskCount++;
            } else if (heartRateValue > upperThresholdHighRisk()) {
                highRiskCount++;
            } else if (heartRateValue < lowerThresholdLowRisk() && heartRateValue >= upperThresholdMediumRisk()) {
                lowRiskCount++;
            } else if (heartRateValue < lowerThresholdMediumRisk() && heartRateValue >= lowerThresholdHighRisk()) {
                mediumRiskCount++;
            } else if (heartRateValue < lowerThresholdHighRisk()) {
                highRiskCount++;
            } else {
                noRiskCount++;
            }

            updateViews(view, latestSevenDaysData, lowRiskCount, mediumRiskCount, highRiskCount, noRiskCount);
        }

        return 0;
    }

    private void updateViews(View view, final ArrayList<HeartRateValueEntity> data, int lowRiskCount, int mediumRiskCount, int highRiskCount, int noRiskCount) {
        if (getContext() == null) return;

        TextView normalTv = view.findViewById(R.id.normal_value);
        TextView lowRiskTv = view.findViewById(R.id.low_risk_value);
        TextView mediumRiskTv = view.findViewById(R.id.medium_risk_value);
        TextView highRiskTv = view.findViewById(R.id.high_risk_value);
        TextView riskTypeTv = view.findViewById(R.id.risk_type_tv);
        final TextView currentHeartRateTv = view.findViewById(R.id.bpm_value);
        final TextView currentHeartRateTimeTv = view.findViewById(R.id.last_update);
        ConstraintLayout currentHrContainer = view.findViewById(R.id.bpm_container);

        if (data.size() > 0) {
            currentHrContainer.setVisibility(View.VISIBLE);
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (data.size() == 0) return;
                    HeartRateValueEntity latestValue = data.get(0);
                    for (int i = 1; i < data.size(); i++) {
                        if (ChronoUnit.SECONDS.between(data.get(i).getDate(), latestValue.getDate()) < 0) {
                            latestValue = data.get(i);
                        }
                    }
                    currentHeartRateTv.setText(latestValue.getValue().toString());
                    currentHeartRateTimeTv.setText("Last update: " + Utils.formalTimeString(latestValue.getDate()));

                    latestSevenDaysData.clear();
                    latestSevenDaysData.clear();

                }
            });
        } else {
            currentHrContainer.setVisibility(View.GONE);
        }

        normalTv.setText("" + noRiskCount);
        lowRiskTv.setText("" + lowRiskCount);
        mediumRiskTv.setText("" + mediumRiskCount);
        highRiskTv.setText("" + highRiskCount);

        int dataSize = data.size();

        if (highRiskCount > dataSize * 0.40) {
            riskTypeTv.setText(getString(R.string.high_risk));
            riskTypeTv.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        } else if (mediumRiskCount > dataSize * 0.50) {
            riskTypeTv.setText(getString(R.string.medium_risk));
            riskTypeTv.setTextColor(ContextCompat.getColor(getContext(), R.color.orange));
        } else if (lowRiskCount > dataSize * 0.75) {
            riskTypeTv.setText(getString(R.string.low_risk));
            riskTypeTv.setTextColor(ContextCompat.getColor(getContext(), R.color.yellow));
        } else if (highRiskCount > dataSize * 0.35 && mediumRiskCount > dataSize * 0.35) {
            riskTypeTv.setText(getString(R.string.high_risk));
            riskTypeTv.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        } else if (mediumRiskCount > dataSize * 0.35 && highRiskCount > 0.20) {
            riskTypeTv.setText(getString(R.string.medium_risk));
            riskTypeTv.setTextColor(ContextCompat.getColor(getContext(), R.color.orange));
        } else if (lowRiskCount > dataSize * 0.20 && (mediumRiskCount > dataSize * 0.20 || highRiskCount > 0.20)) {
            riskTypeTv.setText(getString(R.string.low_risk));
            riskTypeTv.setTextColor(ContextCompat.getColor(getContext(), R.color.yellow));
        } else {
            riskTypeTv.setText(getString(R.string.no_risk));
            riskTypeTv.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        }
    }

    private double lowerThresholdLowRisk() {
        return q1 - (LOW_RISK_CONSTANT * iqr);
    }

    private double upperThresholdLowRisk() {
        return q3 + (LOW_RISK_CONSTANT * iqr);
    }

    private double lowerThresholdMediumRisk() {
        return q1 - (MEDIUM_RISK_CONSTANT * iqr);
    }

    private double upperThresholdMediumRisk() {
        return q3 + (MEDIUM_RISK_CONSTANT * iqr);
    }

    private double lowerThresholdHighRisk() {
        return q1 - (HIGH_RISK_CONSTANT * iqr);
    }

    private double upperThresholdHighRisk() {
        return q3 + (HIGH_RISK_CONSTANT * iqr);
    }

    private void quickSort(ArrayList<HeartRateValueEntity> data, int leftMostIndex, int rightMostIndex) {
        if (leftMostIndex >= rightMostIndex) {
            return;
        }

        HeartRateValueEntity selected = data.get(rightMostIndex);

        int split = leftMostIndex;

        for (int index = leftMostIndex; index <= rightMostIndex; index++) {
            if (data.get(index).getValue() <= selected.getValue()) {
                HeartRateValueEntity temporary = data.get(split);
                data.set(split, data.get(index));
                data.set(index, temporary);

                split++;
            }
        }

        quickSort(data, leftMostIndex, split - 2);
        quickSort(data, split, rightMostIndex);
    }

    private double findMedian(ArrayList<HeartRateValueEntity> sortedData, int startPosition, int endPosition) {
        int size = endPosition - startPosition;
        int q = (endPosition + startPosition) / 2;
        if (q >= sortedData.size() - 1) return 0;
        double result;
        if (size % 2 == 0)
            result = (sortedData.get(q).getValue() + sortedData.get(q + 1).getValue()) / 2f;
        else result = sortedData.get(q).getValue();
        return result;
    }

}
