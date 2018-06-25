package me.arblitroshani.thesisproject.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;

import io.realm.ObjectServerError;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import me.arblitroshani.thesisproject.Constants;
import me.arblitroshani.thesisproject.R;
import me.arblitroshani.thesisproject.model.NameStatRealm;

public class QueryFragment extends Fragment implements View.OnClickListener{

    private static final String REALTIME_PATH = "namesUploadTest";
    private static final String FIRESTORE_PATH = "namesTest";

    private Button bClear;
    private Button
            bRealtime1, bRealtime2, bRealtime3,
            bFirestore1, bFirestore2, bFirestore3,
            bRealm1, bRealm2, bRealm3;

    private TextView tvResult;

    private DatabaseReference realtimeRef;
    private CollectionReference firestoreRef;
    private Realm realmRef;

    private long lastUpdate = 0;

    public QueryFragment() {}

    public static QueryFragment newInstance() {
        QueryFragment fragment = new QueryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_query, null, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        bClear = view.findViewById(R.id.bClear);
        bRealtime1 = view.findViewById(R.id.bRealtime1);
        bRealtime2 = view.findViewById(R.id.bRealtime2);
        bRealtime3 = view.findViewById(R.id.bRealtime3);
        bFirestore1 = view.findViewById(R.id.bFirestore1);
        bFirestore2 = view.findViewById(R.id.bFirestore2);
        bFirestore3 = view.findViewById(R.id.bFirestore3);
        bRealm1 = view.findViewById(R.id.bRealm1);
        bRealm2 = view.findViewById(R.id.bRealm2);
        bRealm3 = view.findViewById(R.id.bRealm3);
        tvResult = view.findViewById(R.id.tvRealtime);

        bClear.setOnClickListener(this);
        bRealtime1.setOnClickListener(this);
        bRealtime2.setOnClickListener(this);
        bRealtime3.setOnClickListener(this);
        bFirestore1.setOnClickListener(this);
        bFirestore2.setOnClickListener(this);
        bFirestore3.setOnClickListener(this);
        bRealm1.setOnClickListener(this);
        bRealm2.setOnClickListener(this);
        bRealm3.setOnClickListener(this);

        realtimeRef = FirebaseDatabase.getInstance().getReference(REALTIME_PATH);
        firestoreRef = FirebaseFirestore.getInstance().collection("namesTest1529851030688");
        loginRealm();
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

    @Override
    public void onClick(View view) {
        int id = view.getId();

        long startTime = System.nanoTime();

        switch (id) {
            case R.id.bClear:
                tvResult.setText("");
                break;
            case R.id.bRealtime1:
                realtimeRef.orderByChild("occurrences").startAt(23).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        double diff = (System.nanoTime() - startTime) / 1000000.0;
                        tvResult.setText("Realtime Database: " + getDecimalFormat(diff) + " (Q1)");
                    }
                    @Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
                    @Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                    @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                        tvResult.setText("Realtime Database: Error (Q1)");
                    }
                });
                break;
            case R.id.bRealtime2:
                realtimeRef.orderByChild("name").startAt("Ar").endAt("Ar\uf8ff").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        double diff = (System.nanoTime() - startTime) / 1000000.0;
                        tvResult.setText("Realtime Database: " + getDecimalFormat(diff) + " (Q2)");
                    }
                    @Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
                    @Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                    @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
                break;
            case R.id.bRealtime3:
                tvResult.setText("Realtime Database: Not possible (Q3)");
                break;
            case R.id.bFirestore1:
                firestoreRef.whereGreaterThanOrEqualTo("occurrences", 23)
                        .orderBy("occurrences", Query.Direction.ASCENDING)
                        .addSnapshotListener((queryDocumentSnapshots, e) -> {
                            double diff = (System.nanoTime() - startTime) / 1000000.0;
                            tvResult.setText("Firestore Database: " + getDecimalFormat(diff) + " (Q1)");
                        });
                break;
            case R.id.bFirestore2:
                firestoreRef.orderBy("name", Query.Direction.ASCENDING)
                        .whereGreaterThanOrEqualTo("name", "Ar")
                        .whereLessThanOrEqualTo("name", "Ar\uf8ff")
                        .addSnapshotListener((queryDocumentSnapshots, e) -> {
                            double diff = (System.nanoTime() - startTime) / 1000000.0;
                            tvResult.setText("Firestore Database: " + getDecimalFormat(diff) + " (Q2)");
                        });
                break;
            case R.id.bFirestore3:
                firestoreRef.orderBy("name", Query.Direction.ASCENDING)
                        .whereGreaterThanOrEqualTo("name", "Tr")
                        .whereLessThanOrEqualTo("name", "Tr\uf8ff")
                        .whereEqualTo("occurrences", 1846)
                        .addSnapshotListener((queryDocumentSnapshots, e) -> {
                            long now = System.currentTimeMillis();
                            if (now - lastUpdate < 1000) return;
                            lastUpdate = now;
                            double diff = (System.nanoTime() - startTime) / 1000000.0;
                            tvResult.setText("Firestore Database: " + getDecimalFormat(diff) + " (Q3)");
                        });
                break;
            case R.id.bRealm1:
                RealmResults<NameStatRealm> results = realmRef
                        .where(NameStatRealm.class)
                        .greaterThan("occurrences", 23)
                        .findAllAsync();
                results.addChangeListener((o, changeSet) -> {
                    double diff = (System.nanoTime() - startTime) / 1000000.0;
                    tvResult.setText("Realm: " + getDecimalFormat(diff) + " (Q1)");
                });
                break;
            case R.id.bRealm2:
                tvResult.setText("Realm: does not support string inequality (Q2)");
                break;
            case R.id.bRealm3:
                tvResult.setText("Realm: does not support string inequality (Q3)");
                break;
        }
    }

    public String getDecimalFormat(double diff) {
        return new DecimalFormat("#0.000").format(diff) + " ms";
    }
}