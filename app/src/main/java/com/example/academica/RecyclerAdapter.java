package com.example.academica;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {
    private final ArrayList<RecyclerItem> itemList;
    public RecyclerAdapter(ArrayList<RecyclerItem> itemList){
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        RecyclerItem currentItem = itemList.get(position);
        holder.keyTextView.setText(currentItem.getKey());
        holder.nameTextView.setText(currentItem.getName());
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
}
