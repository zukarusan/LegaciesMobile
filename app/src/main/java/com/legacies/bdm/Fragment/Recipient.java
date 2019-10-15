package com.legacies.bdm.Fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.legacies.bdm.R;
import com.legacies.bdm.Tool.SqliteSetting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class Recipient extends Fragment {

    EditText etIdRecipient, etIdRumahSakit, etJumlahDarah;
    Button btnRequest;
    TextView tvStatusRequest;

    View view;
    SqliteSetting setting;

    public Recipient() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipient, container, false);
        this.view = view;

        setting = new SqliteSetting(view.getContext());

        initialSetup();

        cekRequest();

        return view;
    }

    private void initialSetup(){
        etIdRecipient = view.findViewById(R.id.etIdRecipient);
        etIdRumahSakit = view.findViewById(R.id.etIdRumahSakit);
        etJumlahDarah = view.findViewById(R.id.etJumlahDarah);
        btnRequest = view.findViewById(R.id.btnRequestDarah);
        tvStatusRequest = view.findViewById(R.id.tvStatusRequest);

        setOnClick();
    }

    private void setOnClick() {
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idRecipient = etIdRecipient.getText().toString().trim();
                String idRumahSakit = etIdRumahSakit.getText().toString().trim();
                String sJumlahDarah = etJumlahDarah.getText().toString().trim();
                if (idRecipient.length() < 6) {
                    Toast.makeText(view.getContext(), "Mohon isi ID Penerima dengan benar.", Toast.LENGTH_SHORT).show();
                } else if (idRumahSakit.length() < 3) {
                    Toast.makeText(view.getContext(), "Mohon isi ID Rumah Sakit dengan benar.", Toast.LENGTH_SHORT).show();
                } else if (sJumlahDarah.isEmpty() || Integer.parseInt(sJumlahDarah) < 10) {
                    Toast.makeText(view.getContext(), "Mohon isi Jumlah Darah dengan benar.", Toast.LENGTH_SHORT).show();
                } else {
                    request(idRecipient,idRumahSakit,Integer.parseInt(sJumlahDarah));
                }
            }
        });
    }

    private void request(final String idRecipient, final String idRumahSakit, final int jumlahDarah){

        final ProgressDialog pdialog = new ProgressDialog(view.getContext());
        pdialog.setCancelable(false);
        pdialog.setMessage("Memuat...");
        pdialog.show();

        final FirebaseDatabase instance = FirebaseDatabase.getInstance();

        DatabaseReference recipientRef = instance.getReference("User/"+idRecipient.replace(".",""));
        recipientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot datasnapRecipient) {
                if (datasnapRecipient.exists()) {

                    instance.getReference("RS/"+idRumahSakit).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapRs) {
                            if (datasnapRs.exists()) {

                                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                                Date date = new Date();
                                final String idRequest = format.format(date);

                                String nama = datasnapRecipient.child("Nama").getValue().toString();
                                String goldar = datasnapRecipient.child("GolDar").getValue().toString();
                                String rhesus = datasnapRecipient.child("Rhesus").getValue().toString();
                                String gender = datasnapRecipient.child("Gender").getValue().toString();
                                final String rumahSakit = datasnapRs.child("Nama").getValue().toString();
                                String latLongRs = datasnapRs.child("LatLong").getValue().toString();

                                Map<String,Object> dataMap = new HashMap<>();
                                dataMap.put("ID", idRecipient);
                                dataMap.put("Nama", nama);
                                dataMap.put("RS", idRumahSakit);
                                //dataMap.put("LatLongRs", latLongRs);
                                dataMap.put("GolDar", goldar);
                                dataMap.put("Rhesus", rhesus);
                                dataMap.put("Jumlah", jumlahDarah);
                                dataMap.put("Status", 0);
                                dataMap.put("Gender", gender);

                                instance.getReference("Request/" + idRequest).updateChildren(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        pdialog.dismiss();
                                        if (task.isSuccessful()) {
                                            instance.getReference("User/"+setting.ambil1("ID")+"/Request").setValue(idRequest);
                                            cekRequest();
                                        } else {
                                            Toast.makeText(view.getContext(), "Request tidak bisa dilakukan. Periksa Koneksi.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            } else {
                                pdialog.dismiss();
                                Toast.makeText(view.getContext(), "ID Rumah Sakit tidak ditemukan.", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                } else {
                    pdialog.dismiss();
                    Toast.makeText(view.getContext(), "ID Penerima tidak ditemukan.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLayout(String idRecipient, String idRs, String jumlah, String status, String idRequest) {
        etIdRecipient.setText(idRecipient);
        etIdRecipient.setEnabled(false);

        etIdRumahSakit.setText(idRs);
        etIdRumahSakit.setEnabled(false);

        etJumlahDarah.setText(jumlah);
        etJumlahDarah.setEnabled(false);

        if (status.equals("0")) {
            tvStatusRequest.setText("Request menunggu persetujuan admin.");
        } else if (status.equals("1")){
            tvStatusRequest.setText("Request sudah diterima dan sedang diproses.");
        } else if (status.equals("2")){
            hapusRequest("Request anda ditolak.", idRequest);
        } else {
            hapusRequest("Request telah selesai diproses.", idRequest);
        }

        tvStatusRequest.setVisibility(View.VISIBLE);

        btnRequest.setVisibility(View.GONE);
    }

    private void cekRequest() {
        final ProgressDialog pdialog = new ProgressDialog(view.getContext());
        pdialog.setCancelable(false);
        pdialog.setMessage("Memuat...");
        pdialog.show();

        final FirebaseDatabase instance = FirebaseDatabase.getInstance();

        instance.getReference("User/" + setting.ambil1("ID")+"/Request").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final String idRequest = dataSnapshot.getValue().toString();

                    instance.getReference("Request/"+idRequest).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                final String id = dataSnapshot.child("ID").getValue().toString();
                                final String idRs = dataSnapshot.child("RS").getValue().toString();
                                final String jumlah = dataSnapshot.child("Jumlah").getValue().toString();
                                final String status = dataSnapshot.child("Status").getValue().toString();

                                instance.getReference("RS/" +idRs+"/Nama").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String namaRs = idRs;
                                        if (dataSnapshot.exists()) {
                                            namaRs = dataSnapshot.getValue().toString();
                                        }
                                        setLayout(id,namaRs,jumlah,status,idRequest);
                                        pdialog.dismiss();
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
                } else {
                    pdialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void hapusRequest(String pesan, final String idRequest) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Pemberitahuan");
        builder.setMessage(pesan);
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String id = setting.ambil1("ID");
                FirebaseDatabase.getInstance().getReference("User/"+id+"/Request").removeValue();
                FirebaseDatabase.getInstance().getReference("Request/"+idRequest).removeValue();
                resetLayout();
            }
        });

        builder.create().show();
    }

    private void resetLayout() {
        etIdRecipient.setText("");
        etIdRecipient.setEnabled(true);

        etIdRumahSakit.setText("");
        etIdRumahSakit.setEnabled(true);

        etJumlahDarah.setText("");
        etJumlahDarah.setEnabled(true);

        tvStatusRequest.setText("");
        tvStatusRequest.setVisibility(View.GONE);

        btnRequest.setVisibility(View.VISIBLE);
    }

}
