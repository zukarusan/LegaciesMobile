package com.legacies.bdm.Fragment;


import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.legacies.bdm.Adapter.DonorAdapter;
import com.legacies.bdm.Object.DonorItem;
import com.legacies.bdm.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class Log extends Fragment {

    View view;
    RecyclerView review;
    ArrayList<DonorItem> arrayList = new ArrayList<>();

    public Log() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        this.view = view;
        initialSetup();
        ambilData();
        return view;
    }

    private void initialSetup() {
        review = view.findViewById(R.id.reviewLog);

        review = view.findViewById(R.id.reviewLog);
        review.setLayoutManager(new LinearLayoutManager(view.getContext()));
        review.setHasFixedSize(true);
    }

    private void ambilData() {
        final ProgressDialog pdialog = new ProgressDialog(view.getContext());
        pdialog.setMessage("Memuat...");
        pdialog.setCancelable(false);
        pdialog.show();
        arrayList.clear();
        FirebaseDatabase.getInstance().getReference("LogAll").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        String nama = "Nama : "+ snap.child("Nama").getValue().toString();
                        String tempat = snap.child("Tempat").getValue().toString();
                        String tanggal = snap.child("Tanggal").getValue().toString();

                        arrayList.add(new DonorItem(nama,tempat,tanggal));
                    }
                    setAdapter();
                }

                pdialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setAdapter() {
        DonorAdapter adapter = new DonorAdapter(arrayList);
        review.setAdapter(adapter);
    }

}
