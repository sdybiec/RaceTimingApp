package com.racingtimer.android.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.racingtimer.android.R;
import com.racingtimer.model.TimingEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for Fast Tap results table.
 * Shows both assigned and unassigned timing entries.
 */
public class FastTapResultsAdapter extends RecyclerView.Adapter<FastTapResultsAdapter.ViewHolder> {

    private final List<TimingEntry> entries = new ArrayList<>();
    private int unassignedCount = 0;

    public void setEntries(List<TimingEntry> newEntries, int unassignedCount) {
        this.entries.clear();
        this.entries.addAll(newEntries);
        this.unassignedCount = unassignedCount;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timing_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimingEntry entry = entries.get(position);

        // Determine if this is an unassigned entry
        boolean isUnassigned = position >= (entries.size() - unassignedCount);

        holder.tvSequence.setText(String.valueOf(entry.getSequenceNumber()));

        if (isUnassigned) {
            // Unassigned entry - show placeholder
            holder.tvBibNumber.setText("-");
            holder.tvBibNumber.setBackgroundColor(0xFFCCCCCC);
            holder.tvCrewName.setText("Select a bib # above");
            holder.tvCrewName.setTextColor(Color.GRAY);
            holder.tvCategory.setText("");
            holder.itemView.setBackgroundColor(0xFFFFF8DC); // Light yellow
        } else {
            // Assigned entry - show data
            holder.tvBibNumber.setText(String.valueOf(entry.getBibNumber()));
            holder.tvBibNumber.setBackgroundColor(0xFFF8E71C);
            holder.tvCrewName.setText(entry.getCrewName() != null ? entry.getCrewName() : "");
            holder.tvCrewName.setTextColor(Color.BLACK);
            holder.tvCategory.setText(entry.getCategory() != null ? entry.getCategory() : "");
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.tvRaceClock.setText(entry.getFormattedRaceTime());
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSequence;
        TextView tvBibNumber;
        TextView tvCrewName;
        TextView tvCategory;
        TextView tvRaceClock;

        ViewHolder(View itemView) {
            super(itemView);
            tvSequence = itemView.findViewById(R.id.tvSequence);
            tvBibNumber = itemView.findViewById(R.id.tvBibNumber);
            tvCrewName = itemView.findViewById(R.id.tvCrewName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvRaceClock = itemView.findViewById(R.id.tvRaceClock);
        }
    }
}
