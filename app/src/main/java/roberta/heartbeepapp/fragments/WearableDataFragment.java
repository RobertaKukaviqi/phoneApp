package roberta.heartbeepapp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import roberta.heartbeepapp.R;
import roberta.heartbeepapp.models.WeekEntity;
import roberta.heartbeepapp.utilities.Utils;

public class WearableDataFragment extends Fragment {

    public static final String ARG_WEAR_ID = "ARG_WEAR_ID";
    public static final int WEEK_SIZE = 7;

    private String wearId;
    private ArrayList<WeekEntity> weekAverageValues;
    private LineChart chart;

    public static WearableDataFragment newInstance(String wearId){
        WearableDataFragment fragment = new WearableDataFragment();
        Bundle b = new Bundle();
        b.putString(ARG_WEAR_ID, wearId);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        wearId = getArguments().getString(ARG_WEAR_ID);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wearable_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("WearId", wearId);
        chart = view.findViewById(R.id.lineChart);
        initGraph();
        getWeekData(view, LocalDate.now(), true, WEEK_SIZE);

        initHeartRateRiskCalculation();
    }

    private void getWeekData(final View view, LocalDate date, final boolean currentWeek, final int requiredDates) {
        FirebaseDatabase.getInstance().getReference("users")
                .child("wear")
                .child(wearId)
                .child("data")
                .child(Utils.getWeekStart(date).toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Object> wd = (Map<String,Object>) dataSnapshot.getValue();

                        if(wd == null || wd.isEmpty()){
                            return;
                        }

                        ArrayList<Map.Entry<String, Object>> weekData = new ArrayList<>(wd.entrySet());

                        weekData = Utils.weekDataDescendingSort(weekData);

                        if(currentWeek)
                            weekAverageValues = new ArrayList<>();

                        int weekLength = requiredDates;
                        if(weekData.size() < weekLength)
                            weekLength = weekData.size();

                        for(int index = 0; index < weekLength; index++) {
                            Map.Entry<String, Object>entry = weekData.get(index);

                            //separated days of the week
                            Map<String, Object> values = (Map<String, Object>) entry.getValue();

                            weekAverageValues.add(new WeekEntity(entry.getKey(), Utils.calculateAverage(values)));
                        }

                        if(currentWeek && weekAverageValues.size() < WEEK_SIZE){
                            getWeekData(view, LocalDate.now().minusWeeks(1), false, WEEK_SIZE - weekAverageValues.size());
                            setData(view);
                        }else{
                            setData(view);
                            weekAverageValues.clear();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void initGraph() {

        chart.setTouchEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setViewPortOffsets(Utils.convertDpToPixel(10, getContext()), 0f, Utils.convertDpToPixel(10, getContext()), 0f);

        Legend l = chart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setData(final View view) {
        ArrayList<Entry> values = new ArrayList<>();

        if(getContext() == null){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setData(view);
                }
            },5);
        }else{


            LocalDate date;
            if (weekAverageValues.isEmpty()) {
                date = LocalDate.now();
            } else {
                date = LocalDate.parse(weekAverageValues.get(weekAverageValues.size() - 1).getDate());
            }
            long x = TimeUnit.MILLISECONDS.toHours(date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
            LinearLayout container = view.findViewById(R.id.x_axes);
            for (int i = weekAverageValues.size() - 1; i >= 0; i--) {
                float y = (float) weekAverageValues.get(i).getValue();
                values.add(new Entry(x, y)); // add one entry
                x += TimeUnit.DAYS.toHours(1);

                TextView xAxesItem = (TextView) container.getChildAt(weekAverageValues.size() - 1 - i);
                if (xAxesItem != null) {
                    xAxesItem.setVisibility(View.VISIBLE);
                    xAxesItem.setText(Utils.timeToShortString(LocalDate.parse(weekAverageValues.get(i).getDate())));
                }
            }

            for (int i = weekAverageValues.size(); i < 7; i++) {
                TextView xAxesItem = (TextView) container.getChildAt(i);
                xAxesItem.setVisibility(View.GONE);
            }

            // create dataset
            LineDataSet set = new LineDataSet(values, "Values");
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setColor(ContextCompat.getColor(getContext(), R.color.light_blue));
            set.setValueTextColor(ContextCompat.getColor(getContext(), R.color.light_blue));
            set.setLineWidth(2.5f);
            set.setDrawCircles(true);
            set.setDrawValues(true);
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set.setDrawFilled(true);

            set.setFillAlpha(65);
            set.setFillColor(ColorTemplate.getHoloBlue());
            set.setCircleColor(ContextCompat.getColor(getContext(), R.color.green));
            set.setHighLightColor(Color.rgb(244, 117, 117));
            set.setDrawCircleHole(false);
            set.setCircleRadius(Utils.convertDpToPixel(1.0f, getContext()));
            set.setCircleHoleRadius(Utils.convertDpToPixel(0.8f, getContext()));

            LineData data = new LineData(set);
            data.setValueTextColor(ContextCompat.getColor(getContext(), R.color.green));
            data.setValueTextSize(9f);

            chart.setData(data);
            chart.animateX(1);
        }
    }

    private void initHeartRateRiskCalculation(){
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, HeartRiskCalculationFragment.getInstance(getActivity(), wearId))
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(chart != null)
            chart.animateX( 1);
    }

}
