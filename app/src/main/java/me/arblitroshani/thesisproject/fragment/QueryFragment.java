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

import java.text.DecimalFormat;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import me.arblitroshani.thesisproject.Constants;
import me.arblitroshani.thesisproject.R;

public class QueryFragment extends Fragment implements View.OnClickListener{

    private static final String REALTIME_PATH = "namesUploadTest";
    private static final String FIRESTORE_PATH = "namesTest";

    private Button bClear;
    private Button bRealtime1, bFirestore1, bRealm1, bRealtime2, bRealtime3;
    private TextView tvRealtime, tvFirestore, tvRealm;

    private DatabaseReference realtimeRef;
    private CollectionReference firestoreRef;
    private Realm realmRef;

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
        bRealm1 = view.findViewById(R.id.bRealm1);
        tvRealtime = view.findViewById(R.id.tvRealtime);
        tvFirestore = view.findViewById(R.id.tvFirestore);
        tvRealm = view.findViewById(R.id.tvRealm);

        bClear.setOnClickListener(this);
        bRealtime1.setOnClickListener(this);
        bRealtime2.setOnClickListener(this);
        bRealtime3.setOnClickListener(this);
        bFirestore1.setOnClickListener(this);
        bRealm1.setOnClickListener(this);

        realtimeRef = FirebaseDatabase.getInstance().getReference(REALTIME_PATH);
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

        switch (id) {
            case R.id.bClear:
                tvRealtime.setText("");
                tvFirestore.setText("");
                tvRealm.setText("");
                break;
            case R.id.bRealtime1:
                long startTime = System.nanoTime();
                realtimeRef.orderByChild("occurrences").startAt(23).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        double diff = (System.nanoTime() - startTime) / 1000000.0;
                        final String toPrintAve = new DecimalFormat("#0.000").format(diff) + " ms";
                        tvRealtime.setText("Realtime Database: " + toPrintAve + " (Q1)");
                    }
                    @Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
                    @Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                    @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                        tvRealtime.setText("Realtime Database: Error (Q1)");
                    }
                });
                break;
            case R.id.bRealtime2:
                long startTime1 = System.nanoTime();
                realtimeRef.orderByChild("name").startAt("Ar").endAt("Ar\uf8ff").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        double diff = (System.nanoTime() - startTime1) / 1000000.0;
                        final String toPrintAve = new DecimalFormat("#0.000").format(diff) + " ms";
                        tvRealtime.setText("Realtime Database: " + toPrintAve + " (Q2)");
                    }
                    @Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
                    @Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                    @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
                break;
            case R.id.bRealtime3:
                tvRealtime.setText("Realtime Database: Not possible (Q3)");
                break;
        }
    }
}