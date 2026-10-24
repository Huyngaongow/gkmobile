package com.example.midterm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.model.LoginLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoginLogAdapter extends RecyclerView.Adapter<LoginLogAdapter.LogViewHolder> {
    private final List<LoginLog> logList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault());

    public LoginLogAdapter(List<LoginLog> logList){
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_login_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position){
        LoginLog log = logList.get(position);
        String dateStr = dateFormat.format(new Date(log.getTimestamp()));
        holder.tvLogTime.setText(dateStr);
        holder.tvLogDevice.setText("Thiết bị: " + (log.getDeviceInfo() != null ? log.getDeviceInfo() : "Không rõ"));
    }

    @Override
    public int getItemCount(){
        return logList.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder{
        TextView tvLogTime, tvLogDevice;

        public LogViewHolder(@NonNull View itemView){
            super(itemView);
            tvLogTime = itemView.findViewById(R.id.tvLogTime);
            tvLogDevice = itemView.findViewById(R.id.tvLogDevice);
        }
    }
}