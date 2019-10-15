package com.legacies.bdm.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.legacies.bdm.Fragment.Donor;
import com.legacies.bdm.Fragment.Log;
import com.legacies.bdm.Fragment.Recipient;
import com.legacies.bdm.R;
import com.legacies.bdm.Tool.SqliteSetting;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialSetup();
        checkAcceptance();
    }

    private void initialSetup() {
        bottomNavigationView = findViewById(R.id.bottomnavigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(R.id.nvDonor);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nvDonor:
                    Donor donor = new Donor(MainActivity.this);
                    FragmentTransaction donorTransaction = getSupportFragmentManager().beginTransaction();
                    donorTransaction.replace(R.id.content,donor);
                    donorTransaction.commit();
                    return true;
                case R.id.nvRecipient:
                    Recipient recipient = new Recipient();
                    FragmentTransaction recipientTransaction = getSupportFragmentManager().beginTransaction();
                    recipientTransaction.replace(R.id.content,recipient);
                    recipientTransaction.commit();
                    return true;
                case R.id.nvLog:
                    Log log = new Log();
                    FragmentTransaction logTransaction = getSupportFragmentManager().beginTransaction();
                    logTransaction.replace(R.id.content,log);
                    logTransaction.commit();
                    return true;

            }
            return false;
        }

    };

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void checkAcceptance() {
        SqliteSetting setting = new SqliteSetting(this);
        final String id = setting.ambil1("ID");

        final FirebaseDatabase instance = FirebaseDatabase.getInstance();

        instance.getReference("User/"+id+"/Accept").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String rsId = dataSnapshot.getValue().toString();

                    instance.getReference("RS/"+rsId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot rsSnap) {
                            if (rsSnap.exists()) {

                                String namaRs = rsSnap.child("Nama").getValue().toString();
                                final String latLong = rsSnap.child("LatLong").getValue().toString();

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Apakah anda ingin diarahkan menuju "+ namaRs +" ?");
                                builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri gmmIntentUri = Uri.parse("google.navigation:q="+latLong);
                                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                        mapIntent.setPackage("com.google.android.apps.maps");
                                        startActivity(mapIntent);
                                    }
                                });
                                builder.setNegativeButton("Tidak", null);
                                builder.create().show();

                                instance.getReference("User/"+id+"/Accept").removeValue();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
}
