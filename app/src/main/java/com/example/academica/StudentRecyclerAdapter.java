package com.example.academica;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StudentRecyclerAdapter extends RecyclerView.Adapter<StudentRecyclerAdapter.Viewholder> {

    String[] details = {"sfsf","sdfsdfsfd","sfssfdsfsf","sfwrjfhruifh"} ;
    Integer[] imge = {R.drawable.attendanceimg,R.drawable.attendanceimg,R.drawable.attendanceimg,R.drawable.attendanceimg};



    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.student_row,parent,false);


        return new Viewholder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {


        holder.textView.setText(details[position]);
        holder.imageView.setImageResource(imge[position]);


    }

    @Override
    public int getItemCount() {
        return 4;
    }

    class Viewholder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textView;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
        }
    }
}
