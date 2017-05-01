package com.brunoaybar.chatdemos;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brunoaybar.chatdemos.data.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brunoaybar on 04/03/2017.
 */

public class TextsAdapter extends RecyclerView.Adapter<TextsAdapter.ViewHolder> {

    private Context context;
    private List<Message> entries;

    public TextsAdapter(Context context){
        this.entries = new ArrayList<>();
        this.context = context;
    }

    public void add(Message entry){
        entries.add(0,entry);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_text_entry,parent));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message entry = entries.get(position);

        holder.tviValue.setText(entry.getValue());
        holder.tviSize.setText(entry.getSize());
        holder.tviDelay.setText(entry.getDelay());

    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tviValue, tviSize, tviDelay;

        public ViewHolder(View itemView) {
            super(itemView);
            tviValue = (TextView) itemView.findViewById(R.id.tviValue);
            tviSize = (TextView) itemView.findViewById(R.id.tviSize);
            tviDelay = (TextView) itemView.findViewById(R.id.tviDelay);
        }
    }
}
