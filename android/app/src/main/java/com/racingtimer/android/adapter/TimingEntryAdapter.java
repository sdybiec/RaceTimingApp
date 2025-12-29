package com.racingtimer.android.adapter;

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
 * RecyclerView adapter for timing entries table.
 */
public class TimingEntryAdapter extends RecyclerView.Adapter<TimingEntryAdapter.ViewHolder> {

    private final List<TimingEntry> entries = new ArrayList<>();
    private int selectedPosition = -1;
    private OnEntryClickListener listener;

    public interface OnEntryClickListener {
        void onEntryClick(int position);
    }

    public void setOnEntryClickListener(OnEntryClickListener listener) {
        this.listener = listener;
    }

    public void setEntries(List<TimingEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        notifyDataSetChanged();
    }

    public void addEntry(TimingEntry entry) {
        entries.add(entry);
        notifyItemInserted(entries.size() - 1);
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;

        if (previousPosition >= 0) {
            notifyItemChanged(previousPosition);
        }
        if (selectedPosition >= 0) {
            notifyItemChanged(selectedPosition);
        }
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

        holder.tvSequence.setText(String.valueOf(entry.getSequenceNumber()));

        if (entry.getBibNumber() != null) {
            holder.tvBibNumber.setText(String.valueOf(entry.getBibNumber()));
            holder.tvBibNumber.setBackgroundColor(0xFFF8E71C); // Yellow
        } else {
            holder.tvBibNumber.setText("?");
            holder.tvBibNumber.setBackgroundColor(0xFFCCCCCC); // Gray
        }

        holder.tvCrewName.setText(entry.getCrewName() != null ? entry.getCrewName() : "");
        holder.tvCategory.setText(entry.getCategory() != null ? entry.getCategory() : "");
        holder.tvRaceClock.setText(entry.getFormattedRaceTime());

        // Highlight selected row
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(0xFFE8F4FD); // Light blue
        } else {
            holder.itemView.setBackgroundColor(0xFFFFFFFF); // White
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEntryClick(position);
            }
        });
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
