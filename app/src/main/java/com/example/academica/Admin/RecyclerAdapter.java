package com.example.academica.Admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.example.academica.R;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {
    private final ArrayList<RecyclerItem> itemList;
    public boolean colorFlag = false;  // to separate newly added items by changing color
    public RecyclerAdapter(ArrayList<RecyclerItem> itemList){
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        if(colorFlag){
            // change the background to separate the view
            view.setBackground(AppCompatResources.getDrawable(parent.getContext(), R.drawable.recycler_item_background_green));
        }
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        if(itemList != null && itemList.size() > 0){
            RecyclerItem currentItem = itemList.get(position);
            holder.keyTextView.setText(currentItem.getKey());
            holder.nameTextView.setText(currentItem.getName());
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder{
        public TextView keyTextView, nameTextView;
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            keyTextView = itemView.findViewById(R.id.item_key_textView);
            nameTextView = itemView.findViewById(R.id.item_name_textView);
        }
    }

    public void changeColorFlag(){
        this.colorFlag = true;
    }
}
