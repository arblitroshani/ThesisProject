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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.arblitroshani.thesisproject.R;
import me.arblitroshani.thesisproject.model.NameStat;

public class UploadFragment extends Fragment implements View.OnClickListener {

    private static final String REALTIME_PATH = "namesUploadTest";

    private static final String LABEL_REALTIME = "Realtime Database";
    private static final String LABEL_FIRESTORE = "Cloud Firestore";
    private static final String LABEL_REALM = "Realm Platform";

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

    private List<ILineDataSet> dataSets;
    private LineData lineData;
    private List<Entry> entriesUploadRealtime;
    private List<Entry> entriesUploadFirestore;
    private List<Entry> entriesUploadRealm;
    private LineDataSet dataSetUploadRealtime;
    private LineDataSet dataSetUploadFirestore;
    private LineDataSet dataSetUploadRealm;

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

        bRealtime.setOnClickListener(this);
        bFirestore.setOnClickListener(this);
        bRealm.setOnClickListener(this);

        etDataSet = view.findViewById(R.id.etDataSet);
        etNumTrials = view.findViewById(R.id.etNumTrials);

        realtimeRef = FirebaseDatabase.getInstance().getReference(REALTIME_PATH);

        Description d = new Description();
        d.setText("Chart description");
        chart.setDescription(d);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(1f);
        xAxis.setGranularity(1f); // only intervals of 1 unit

        dataSets = new ArrayList<>();

        entriesUploadRealtime = new ArrayList<>();
        entriesUploadFirestore = new ArrayList<>();
        entriesUploadRealm = new ArrayList<>();

        createLineDataSet(REALTIME_INDEX);
        createLineDataSet(FIRESTORE_INDEX);
        createLineDataSet(REALM_INDEX);

        dataSets.add(dataSetUploadRealtime);
        dataSets.add(dataSetUploadFirestore);
        dataSets.add(dataSetUploadRealm);

        entriesUploadRealtime.add(new Entry((float) 0.0, (float) 0.0));
        entriesUploadFirestore.add(new Entry((float) 0.0, (float) 0.0));
        entriesUploadRealm.add(new Entry((float) 0.0, (float) 0.0));

        dataSetUploadRealtime.setValues(entriesUploadRealtime);
        dataSetUploadFirestore.setValues(entriesUploadFirestore);
        dataSetUploadRealm.setValues(entriesUploadRealm);

        dataSets.set(0, dataSetUploadRealtime);
        dataSets.set(1, dataSetUploadFirestore);
        dataSets.set(2, dataSetUploadRealm);

        chart.setData(new LineData(dataSets));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (view.getId()) {
            case R.id.bRealtime:
                if (!getInput()) return;

                createLineDataSet(REALTIME_INDEX);
                entriesUploadRealtime.clear();

                InputStream inputStream;
                InputStreamReader inputReader;
                BufferedReader bufferedReader;

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
                        // add to array
                        entriesUploadRealtime.add(new Entry((float) j, (float) diff));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                dataSetUploadRealtime.setValues(entriesUploadRealtime);
                dataSets.set(0, dataSetUploadRealtime);
                updateChart();

                break;
            case R.id.bFirestore:
                if (!getInput()) return;

                createLineDataSet(FIRESTORE_INDEX);
                entriesUploadFirestore.clear();

                entriesUploadFirestore.add(new Entry((float) 1.0, (float) 8.0));
                entriesUploadFirestore.add(new Entry((float) 2.0, (float) 10.0));

                dataSetUploadFirestore.setValues(entriesUploadFirestore);
                dataSets.set(1, dataSetUploadFirestore);
                updateChart();

                break;
        }
    }

    private void updateChart() {
        lineData = new LineData(dataSets);
        chart.setData(lineData);
        chart.invalidate();
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

    private void createLineDataSet(int index) {
        if (index == 0) {
            dataSetUploadRealtime = new LineDataSet(entriesUploadRealtime, LABEL_REALTIME);
            dataSetUploadRealtime.setColor(Color.BLUE);
            dataSetUploadRealtime.setValueTextColor(DEFAULT_LABEL_COLOR);
        } else if (index == 1) {
            dataSetUploadFirestore = new LineDataSet(entriesUploadFirestore, LABEL_FIRESTORE);
            dataSetUploadFirestore.setColor(Color.GREEN);
            dataSetUploadFirestore.setValueTextColor(DEFAULT_LABEL_COLOR);
        } else if (index == 2) {
            dataSetUploadRealm = new LineDataSet(entriesUploadRealm, LABEL_REALM);
            dataSetUploadRealm.setColor(Color.GRAY);
            dataSetUploadRealm.setValueTextColor(DEFAULT_LABEL_COLOR);
        }
    }
}