package com.example.midterm;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.model.User;
import com.example.midterm.utils.ImageUtils;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener{
        void onEdit(User user);
        void onDelete(User user);
        void onViewLogs(User user);
    }

    private final List<User> userList;
    private final OnUserActionListener listener;

    public UserAdapter(List<User> userList, OnUserActionListener listener){
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position){
        User user = userList.get(position);
        holder.tvUserFullName.setText(user.getFullName());
        holder.tvUsernameAndPhone.setText("@" + user.getUsername() + " • " + user.getPhone() + " (" + user.getAge() + " tuổi)");
        String role = user.getRole() != null ? user.getRole().toUpperCase() : "EMPLOYEE";
        holder.tvUserRole.setText(role);

        if("locked".equalsIgnoreCase(user.getStatus())){
            holder.tvUserStatus.setText("ĐÃ KHÓA");
            holder.tvUserStatus.setTextColor(Color.parseColor("#D32F2F"));
            holder.tvUserStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
        }
        else{
            holder.tvUserStatus.setText("BÌNH THƯỜNG");
            holder.tvUserStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.tvUserStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
        }

        if(user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty()){
            Bitmap bitmap = ImageUtils.base64ToBitmap(user.getAvatarUrl());
            if(bitmap != null){
                holder.ivUserAvatar.setImageBitmap(bitmap);
            }
            else{
                holder.ivUserAvatar.setImageResource(R.drawable.ic_user);
            }
        }
        else{
            holder.ivUserAvatar.setImageResource(R.drawable.ic_user);
        }

        if("admin".equalsIgnoreCase(user.getUsername())){
            holder.btnDeleteUser.setVisibility(View.GONE);
        }
        else{
            holder.btnDeleteUser.setVisibility(View.VISIBLE);
        }
        holder.btnEditUser.setOnClickListener(v -> listener.onEdit(user));
        holder.btnDeleteUser.setOnClickListener(v -> listener.onDelete(user));
        holder.btnViewLogs.setOnClickListener(v -> listener.onViewLogs(user));
    }

    @Override
    public int getItemCount(){
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder{
        ImageView ivUserAvatar;
        TextView tvUserFullName, tvUsernameAndPhone, tvUserRole, tvUserStatus;
        Button btnViewLogs, btnEditUser, btnDeleteUser;
        public UserViewHolder(@NonNull View itemView){
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserFullName = itemView.findViewById(R.id.tvUserFullName);
            tvUsernameAndPhone = itemView.findViewById(R.id.tvUsernameAndPhone);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
            btnViewLogs = itemView.findViewById(R.id.btnViewLogs);
            btnEditUser = itemView.findViewById(R.id.btnEditUser);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}