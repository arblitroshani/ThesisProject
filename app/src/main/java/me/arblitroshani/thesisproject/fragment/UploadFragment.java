package me.arblitroshani.thesisproject.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.arblitroshani.thesisproject.R;
import me.arblitroshani.thesisproject.model.NameStat;

public class UploadFragment extends Fragment implements View.OnClickListener {

    private static final String REALTIME_PATH = "namesUploadTest";

    private static final String[] LABELS = {"Realtime Database", "Cloud Firestore", "Realm Platform"};
    private static final int[] LINE_COLORS = {Color.BLUE, Color.GREEN, Color.GRAY};

    private static final int REALTIME_INDEX = 0;
    private static final int FIRESTORE_INDEX = 1;
    private static final int REALM_INDEX = 2;

    private static final int DEFAULT_LABEL_COLOR = Color.RED;

    private static final int DATASET_MIN = 1;
    private static final int DATASET_MAX = 10000;
    private static final int NUM_TRIALS_MIN = 1;
    private static final int NUM_TRIALS_MAX = 100;

    private LineChart chart;
    private Button bRealtime, bFirestore, bRealm;
    private EditText etDataSet, etNumTrials;
    private TextView tvAverageRealtime, tvAverageFirestore, tvAverageRealm;

    private List<ILineDataSet> dataSets;
    private LineData lineData;
    private List<Entry>[] entriesUpload;
    private LineDataSet[] dataSetUpload;

    private DatabaseReference realtimeRef;
    private int dataSetSize;
    private int numTrials;


    public UploadFragment() {}

    public static UploadFragment newInstance() {
        UploadFragment fragment = new UploadFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload, null, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        chart = view.findViewById(R.id.chart);

        bRealtime = view.findViewById(R.id.bRealtime);
        bFirestore = view.findViewById(R.id.bFirestore);
        bRealm = view.findViewById(R.id.bRealm);
        etDataSet = view.findViewById(R.id.etDataSet);
        etNumTrials = view.findViewById(R.id.etNumTrials);
        tvAverageRealtime = view.findViewById(R.id.tvAverageRealtime);
        tvAverageFirestore = view.findViewById(R.id.tvAverageFirestore);
        tvAverageRealm = view.findViewById(R.id.tvAverageRealm);

        bRealtime.setOnClickListener(this);
        bFirestore.setOnClickListener(this);
        bRealm.setOnClickListener(this);

        realtimeRef = FirebaseDatabase.getInstance().getReference(REALTIME_PATH);

        Description d = new Description();
        d.setText("Chart description");
        chart.setDescription(d);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(1f);
        xAxis.setGranularity(1f); // only intervals of 1 unit

        dataSets = new ArrayList<>();
        entriesUpload = new ArrayList[3];
        dataSetUpload = new LineDataSet[3];

        for (int i = 0; i < 3; i++) {
            entriesUpload[i] = new ArrayList<>();
            createLineDataSet(i);
            dataSets.add(dataSetUpload[i]);
            entriesUpload[i].add(new Entry((float) 0.0, (float) 0.0));
            dataSetUpload[i].setValues(entriesUpload[i]);
            dataSets.set(i, dataSetUpload[i]);
        }

        chart.setData(new LineData(dataSets));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (view.getId()) {
            case R.id.bRealtime:
                if (!getInput()) return;

                createLineDataSet(REALTIME_INDEX);
                entriesUpload[REALTIME_INDEX].clear();

                InputStream inputStream;
                InputStreamReader inputReader;
                BufferedReader bufferedReader;

                double weightedAverage = 0.0;

                try {
                    for (int j = 0; j < numTrials + 1; j++) {
                        realtimeRef.removeValue();
                        inputStream = getResources().openRawResource(R.raw.yob2017);
                        inputReader = new InputStreamReader(inputStream);
                        bufferedReader = new BufferedReader(inputReader);
                        // get start time
                        long startTime = System.nanoTime();
                        NameStat ns;
                        String line;
                        for (int i = 0; i < dataSetSize; i++) {
                            line = bufferedReader.readLine();
                            List<String> list = Arrays.asList(line.split(","));
                            ns = new NameStat(list.get(0), 2017, Integer.parseInt(list.get(2)), list.get(1));
                            realtimeRef.push().setValue(ns);
                        }
                        if (j == 0) continue;
                        // get end time
                        long endTime = System.nanoTime();
                        double diff = (endTime - startTime) / 1000000.0;
                        if (j == 1) {
                            weightedAverage = diff;
                        } else {
                            weightedAverage = 0.8 * weightedAverage + 0.2 * diff;
                        }
                        // add to array
                        entriesUpload[REALTIME_INDEX].add(new Entry((float) j, (float) diff));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                tvAverageRealtime.setText(new DecimalFormat("#0.000").format(weightedAverage) + " ms");

                dataSetUpload[REALTIME_INDEX].setValues(entriesUpload[REALTIME_INDEX]);
                dataSets.set(REALTIME_INDEX, dataSetUpload[REALTIME_INDEX]);
                updateChart();

                break;
            case R.id.bFirestore:
                if (!getInput()) return;

                createLineDataSet(FIRESTORE_INDEX);
                entriesUpload[FIRESTORE_INDEX].clear();

                entriesUpload[FIRESTORE_INDEX].add(new Entry((float) 1.0, (float) 8.0));
                entriesUpload[FIRESTORE_INDEX].add(new Entry((float) 2.0, (float) 10.0));

                dataSetUpload[FIRESTORE_INDEX].setValues(entriesUpload[FIRESTORE_INDEX]);
                dataSets.set(FIRESTORE_INDEX, dataSetUpload[FIRESTORE_INDEX]);
                updateChart();

                break;
        }
    }

    private boolean getInput() {
        // get inputs, check if within bounds
        dataSetSize = Integer.parseInt(etDataSet.getText().toString());
        numTrials = Integer.parseInt(etNumTrials.getText().toString());
        if (dataSetSize < DATASET_MIN || dataSetSize > DATASET_MAX) {
            Toast.makeText(this.getContext(), "Dataset not within bounds", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (numTrials < NUM_TRIALS_MIN || numTrials > NUM_TRIALS_MAX) {
            Toast.makeText(this.getContext(), "Number of trials not within bounds", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateChart() {
        lineData = new LineData(dataSets);
        chart.setData(lineData);
        chart.invalidate();
    }

    private void createLineDataSet(int index) {
        dataSetUpload[index] = new LineDataSet(entriesUpload[index], LABELS[index]);
        dataSetUpload[index].setColor(LINE_COLORS[index]);
        dataSetUpload[index].setValueTextColor(DEFAULT_LABEL_COLOR);
    }
}