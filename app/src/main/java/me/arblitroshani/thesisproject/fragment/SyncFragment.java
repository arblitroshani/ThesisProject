package me.arblitroshani.thesisproject.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.arblitroshani.thesisproject.R;

public class SyncFragment extends Fragment {

    public SyncFragment() {}

    public static SyncFragment newInstance() {
        SyncFragment fragment = new SyncFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sync, null, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

}