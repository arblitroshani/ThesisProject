package me.arblitroshani.thesisproject.fragment;

import android.app.ProgressDialog;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import me.arblitroshani.thesisproject.Constants;
import me.arblitroshani.thesisproject.R;
import me.arblitroshani.thesisproject.model.NameStat;
import me.arblitroshani.thesisproject.model.NameStatRealm;

public class UploadFragment extends Fragment implements View.OnClickListener {

    private static final String REALTIME_PATH = "namesUploadTest";
    private static final String FIRESTORE_PATH = "namesTest";

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
    private TextView tvAverage[];

    private List<ILineDataSet> dataSets;
    private LineData lineData;
    private List<Entry>[] entriesUpload;
    private LineDataSet[] dataSetUpload;

    private DatabaseReference realtimeRef;
    private CollectionReference firestoreRef;
    private Realm realmRef;

    private InputStream inputStream;
    private InputStreamReader inputReader;
    private BufferedReader bufferedReader;

    private int dataSetSize;
    private int numTrials;
    private NameStat ns;
    private NameStatRealm nsr;
    private String line;


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
        tvAverage = new TextView[3];
        tvAverage[REALTIME_INDEX] = view.findViewById(R.id.tvAverageRealtime);
        tvAverage[FIRESTORE_INDEX] = view.findViewById(R.id.tvAverageFirestore);
        tvAverage[REALM_INDEX] = view.findViewById(R.id.tvAverageRealm);

        bRealtime.setOnClickListener(this);
        bFirestore.setOnClickListener(this);
        bRealm.setOnClickListener(this);

        realtimeRef = FirebaseDatabase.getInstance().getReference(REALTIME_PATH);
        loginRealm();

        Description d = new Description();
        d.setText("Upload times (ms)");
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

        int touchIndex;

        if (id == R.id.bRealtime) touchIndex = REALTIME_INDEX;
        else if (id == R.id.bFirestore) touchIndex = FIRESTORE_INDEX;
        else if (id == R.id.bRealm) touchIndex = REALM_INDEX;
        else return;

        if (!getInput()) return;

        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setTitle("Calculating " + LABELS[touchIndex] + "!");
        pd.setMessage("Loading...");
        pd.setIndeterminate(false);
        pd.show();

        createLineDataSet(touchIndex);
        entriesUpload[touchIndex].clear();

        double[] differences = new double[100];

        Thread t = new Thread() {
            @Override
            public void run() {
                double weightedAverage = 0.0;

                for (int j = 0; j <= numTrials; j++) {
                    if (touchIndex == 0) {
                        realtimeRef.removeValue();
                    } else if (touchIndex == 1) {
                        firestoreRef = FirebaseFirestore.getInstance().collection(FIRESTORE_PATH + System.currentTimeMillis());
                    }

                    inputStream = getResources().openRawResource(R.raw.yob2017);
                    inputReader = new InputStreamReader(inputStream);
                    bufferedReader = new BufferedReader(inputReader);

                    long startTime = System.nanoTime();

                    if (touchIndex < 2) {
                        try {
                            for (int i = 0; i < dataSetSize; i++) {
                                line = bufferedReader.readLine();
                                List<String> list = Arrays.asList(line.split(","));
                                ns = new NameStat(list.get(0), 2017, Integer.parseInt(list.get(2)), list.get(1));
                                if (touchIndex == 0) {
                                    realtimeRef.push().setValue(ns);
                                } else if (touchIndex == 1) {
                                    firestoreRef.document().set(ns);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        long startTime1 = System.nanoTime();
                        int finalJ = j;
                        getActivity().runOnUiThread(() ->
                                realmRef.executeTransactionAsync(realm -> {
                                    try {
                                        for (int i = 0; i < dataSetSize; i++) {
                                            line = bufferedReader.readLine();
                                            List<String> list = Arrays.asList(line.split(","));
                                            nsr = new NameStatRealm();
                                            nsr.setName(list.get(0));
                                            nsr.setYear(2017);
                                            nsr.setOccurrences(Integer.parseInt(list.get(2)));
                                            nsr.setSex(list.get(1));
                                            realm.insert(nsr);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }, () -> {
                                    if (finalJ != 0) {
                                        long endTime1 = System.nanoTime();
                                        double diff1 = (endTime1 - startTime1) / 1000000.0;
                                        differences[finalJ] = diff1;
                                        if (finalJ == numTrials) {
                                            double newAverage = 0.0;
                                            entriesUpload[2].clear();
                                            // find moving average between values, show in chart and calculate average
                                            for (int i = 1; i <= numTrials; i++) {
                                                if (i == 1) {
                                                    newAverage = differences[1];
                                                } else {
                                                    newAverage = 0.8 * newAverage + 0.2 * differences[i];
                                                }
                                            }

                                            // smooth out values
//                                            for (int i = 1; i <= numTrials; i++) {
//                                                if (differences[i] < newAverage) differences[i] = newAverage -= 0.1*differences[i];
//                                                else differences[i] = newAverage += 0.1*differences[i];
//                                                entriesUpload[2].add(new Entry((float) i, (float) differences[i]));
//                                            }

                                            final String toPrintAve = new DecimalFormat("#0.000").format(newAverage) + " ms";
                                            getActivity().runOnUiThread(() -> {
                                                tvAverage[2].setText(toPrintAve);
                                                dataSetUpload[2].setValues(entriesUpload[2]);
                                                dataSets.set(2, dataSetUpload[2]);
                                                updateChart();
                                            });

                                            pd.dismiss();
                                        }
                                    }
                                }));
                    }

                    if (j == 0) continue;

                    long endTime = System.nanoTime();
                    double diff = (endTime - startTime) / 1000000.0;
                    if (j == 1) {
                        weightedAverage = diff;
                    } else {
                        weightedAverage = 0.8 * weightedAverage + 0.2 * diff;
                    }
                    entriesUpload[touchIndex].add(new Entry((float) j, (float) diff));
                }

                final String toPrintAve = new DecimalFormat("#0.000").format(weightedAverage) + " ms";

                getActivity().runOnUiThread(() -> tvAverage[touchIndex].setText(toPrintAve));

                dataSetUpload[touchIndex].setValues(entriesUpload[touchIndex]);
                dataSets.set(touchIndex, dataSetUpload[touchIndex]);
                updateChart();
                if (touchIndex < 2) pd.dismiss();
            }
        };
        t.start();
    }

    private boolean getInput() {
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

    private void loginRealm() {
        if (SyncUser.current() == null) {
            SyncCredentials credentials = SyncCredentials.nickname("arbli", false);
            SyncUser.logInAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback<SyncUser>() {
                @Override public void onSuccess(SyncUser user) { setupRealm(); }
                @Override public void onError(ObjectServerError error) { Log.e("Login error", error.toString()); }
            });
        } else {
            setupRealm();
        }
    }

    private void setupRealm() {
        SyncConfiguration configuration = SyncUser.current()
                .createConfiguration(Constants.REALM_BASE_URL + "/default")
                .build();
        realmRef = Realm.getInstance(configuration);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realmRef.close();
    }
}