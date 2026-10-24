package com.example.midterm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.model.Certificate;

import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertViewHolder> {

    public interface OnCertificateActionListener {
        void onEdit(Certificate certificate);
        void onDelete(Certificate certificate);
    }

    private final List<Certificate> certificateList;
    private final boolean isEditable;
    private final OnCertificateActionListener listener;

    public CertificateAdapter(List<Certificate> certificateList, boolean isEditable, OnCertificateActionListener listener){
        this.certificateList = certificateList;
        this.isEditable = isEditable;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certificate, parent, false);
        return new CertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertViewHolder holder, int position){
        Certificate cert = certificateList.get(position);

        holder.tvCertTitle.setText(cert.getTitle());
        holder.tvCertIssuedBy.setText("Nơi cấp: " + cert.getIssuedBy());

        String datesStr = "Ngày cấp: " + cert.getIssueDate();

        if(cert.getExpiryDate() != null && !cert.getExpiryDate().isEmpty()){
            datesStr += " - Hạn: " + cert.getExpiryDate();
        }

        holder.tvCertDates.setText(datesStr);

        if(cert.getDescription() != null && !cert.getDescription().trim().isEmpty()){
            holder.tvCertDescription.setVisibility(View.VISIBLE);
            holder.tvCertDescription.setText(cert.getDescription());
        }
        else{
            holder.tvCertDescription.setVisibility(View.GONE);
        }

        if(!isEditable){
            holder.layoutCertActions.setVisibility(View.GONE);
        }
        else{
            holder.layoutCertActions.setVisibility(View.VISIBLE);
        }

        holder.btnEditCert.setOnClickListener(v -> listener.onEdit(cert));
        holder.btnDeleteCert.setOnClickListener(v -> listener.onDelete(cert));
    }

    @Override
    public int getItemCount(){
        return certificateList.size();
    }

    static class CertViewHolder extends RecyclerView.ViewHolder{
        TextView tvCertTitle, tvCertIssuedBy, tvCertDates, tvCertDescription;
        LinearLayout layoutCertActions;
        Button btnEditCert, btnDeleteCert;

        public CertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCertTitle = itemView.findViewById(R.id.tvCertTitle);
            tvCertIssuedBy = itemView.findViewById(R.id.tvCertIssuedBy);
            tvCertDates = itemView.findViewById(R.id.tvCertDates);
            tvCertDescription = itemView.findViewById(R.id.tvCertDescription);
            layoutCertActions = itemView.findViewById(R.id.layoutCertActions);
            btnEditCert = itemView.findViewById(R.id.btnEditCert);
            btnDeleteCert = itemView.findViewById(R.id.btnDeleteCert);
        }
    }
}