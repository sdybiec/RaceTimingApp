package com.racingtimer.android;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.racingtimer.android.adapter.FastTapResultsAdapter;
import com.racingtimer.android.storage.SQLiteTimingStorage;
import com.racingtimer.model.Crew;
import com.racingtimer.model.Race;
import com.racingtimer.model.TimingEntry;
import com.racingtimer.service.TimingService;
import com.racingtimer.storage.StorageCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fast Tap Activity - separates timestamp capture from bib assignment.
 * Allows multiple timestamps to be captured quickly, then bibs assigned in order.
 */
public class FastTapActivity extends AppCompatActivity {

    private TextView tvClock;
    private GridLayout bibButtonGrid;
    private RecyclerView rvResultsTable;
    private TextView tvStatus;

    private TimingService timingService;
    private SQLiteTimingStorage storage;

    private Race currentRace;
    private final List<TimingEntry> unassignedEntries = new ArrayList<>();
    private final List<TimingEntry> assignedEntries = new ArrayList<>();
    private final Map<Integer, Button> bibButtons = new HashMap<>();
    private FastTapResultsAdapter resultsAdapter;

    private Handler clockHandler;
    private Runnable clockRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fast_tap);

        // Initialize storage and services
        storage = new SQLiteTimingStorage(this);
        timingService = new TimingService(storage);

        // Initialize views
        tvClock = findViewById(R.id.tvClock);
        bibButtonGrid = findViewById(R.id.bibButtonGrid);
        rvResultsTable = findViewById(R.id.rvResultsTable);
        tvStatus = findViewById(R.id.tvStatus);

        // Setup RecyclerView
        resultsAdapter = new FastTapResultsAdapter();
        rvResultsTable.setLayoutManager(new LinearLayoutManager(this));
        rvResultsTable.setAdapter(resultsAdapter);

        // Setup timestamp capture button
        View timestampButton = findViewById(R.id.timestampCaptureButton);
        timestampButton.setOnClickListener(v -> captureTimestamp());

        // Setup title bar buttons
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            // Navigate to home
        });
        findViewById(R.id.btnResults).setOnClickListener(v -> {
            // Navigate to results
        });

        // Initialize demo race
        initializeDemoRace();

        // Start clock updates
        startClockUpdates();
    }

    private void initializeDemoRace() {
        currentRace = new Race("race-1", "Middle School Boys", new Date());
        currentRace.setStatus(Race.RaceStatus.IN_PROGRESS);
        timingService.startRace(currentRace);

        // Create bib buttons
        List<Crew> crews = createDemoCrews();
        for (Crew crew : crews) {
            addBibButton(crew);
        }
    }

    private List<Crew> createDemoCrews() {
        List<Crew> crews = new ArrayList<>();

        // Bib numbers matching mockup
        int[] bibNumbers = {
            205, 207, 209, 210, 212, 213, 214,
            216, 217, 218, 219, 220, 221, 222, 223,
            251, 252, 253, 254, 255, 256,
            301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311,
            401, 402, 403, 404, 406, 407, 408, 409, 410, 411, 412, 413,
            414, 416, 418, 421, 423, 424, 425, 426, 427, 428, 429, 431, 432,
            502, 503, 504, 505, 506, 507, 508, 509, 510, 512, 513, 514, 515
        };

        // Add some names for demo
        String[] names = {"Jack Donahue", "Andrew Springer", "Brady Stolts", "Dylan Brown",
                         "Cormac Swensen-Hope"};

        for (int i = 0; i < bibNumbers.length; i++) {
            int bib = bibNumbers[i];
            String name = i < names.length ? names[i] : "Crew " + bib;
            Crew crew = new Crew(
                "crew-" + bib,
                bib,
                name,
                "Middle School Boys",
                currentRace.getId()
            );
            crews.add(crew);
        }

        return crews;
    }

    private void addBibButton(Crew crew) {
        Button button = createBibButton();
        button.setText(crew.getBibNumber() + "\nTap to finish");
        button.setOnClickListener(v -> assignBibToNextEntry(crew));

        bibButtons.put(crew.getBibNumber(), button);
        bibButtonGrid.addView(button);
    }

    private Button createBibButton() {
        Button button = new Button(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 100;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(5, 5, 5, 5);
        button.setLayoutParams(params);
        button.setGravity(Gravity.CENTER);
        button.setPadding(6, 10, 6, 10);
        button.setTextSize(12);
        button.setBackground(ContextCompat.getDrawable(this, R.drawable.crew_button_ready));
        button.setTextColor(Color.BLACK);
        return button;
    }

    private void captureTimestamp() {
        timingService.recordTimestamp(new StorageCallback<TimingEntry>() {
            @Override
            public void onSuccess(TimingEntry entry) {
                runOnUiThread(() -> {
                    entry.setRaceId(currentRace.getId());
                    unassignedEntries.add(entry);
                    refreshResultsTable();
                    updateStatus();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> tvStatus.setText("Error: " + error));
            }
        });
    }

    private void assignBibToNextEntry(Crew crew) {
        if (unassignedEntries.isEmpty()) {
            tvStatus.setText("No unassigned timestamps. Tap timer button first.");
            return;
        }

        // Get the oldest unassigned entry
        TimingEntry entry = unassignedEntries.remove(0);

        // Assign bib and crew info
        entry.setBibNumber(crew.getBibNumber());
        entry.setCrewId(crew.getId());
        entry.setCrewName(crew.getName());
        entry.setCategory(crew.getCategory());

        // Save updated entry
        timingService.assignBibNumber(entry.getId(), crew.getBibNumber(),
            new StorageCallback<TimingEntry>() {
                @Override
                public void onSuccess(TimingEntry updatedEntry) {
                    runOnUiThread(() -> {
                        assignedEntries.add(updatedEntry);
                        refreshResultsTable();

                        String statusMsg = crew.getName() + " (" + crew.getCategory() + "): " +
                                         updatedEntry.getFormattedRaceTime() + " " +
                                         getPositionSuffix(assignedEntries.size()) + " +" +
                                         formatSplitTime(updatedEntry);
                        updateStatus(statusMsg);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> tvStatus.setText("Error: " + error));
                }
            });
    }

    private void refreshResultsTable() {
        List<TimingEntry> allEntries = new ArrayList<>();
        allEntries.addAll(assignedEntries);
        allEntries.addAll(unassignedEntries);
        resultsAdapter.setEntries(allEntries, unassignedEntries.size());
    }

    private void updateStatus() {
        int unassigned = unassignedEntries.size();
        int total = assignedEntries.size() + unassigned;
        tvStatus.setText("Tap timer button or select a bib # above                " +
                        (total - unassigned) + " - " + total);
    }

    private void updateStatus(String message) {
        int unassigned = unassignedEntries.size();
        int total = assignedEntries.size() + unassigned;
        tvStatus.setText(message + "                " +
                        (total - unassigned) + " - " + total);
    }

    private String getPositionSuffix(int position) {
        if (position % 100 >= 11 && position % 100 <= 13) {
            return position + "th";
        }
        switch (position % 10) {
            case 1: return position + "st";
            case 2: return position + "nd";
            case 3: return position + "rd";
            default: return position + "th";
        }
    }

    private String formatSplitTime(TimingEntry entry) {
        if (assignedEntries.isEmpty()) {
            return "0:00.0";
        }
        TimingEntry first = assignedEntries.get(0);
        long splitMillis = entry.getRaceClockMillis() - first.getRaceClockMillis();
        long minutes = splitMillis / 60000;
        long seconds = (splitMillis % 60000) / 1000;
        long tenths = (splitMillis % 1000) / 100;
        return String.format(Locale.getDefault(), "%d:%02d.%d", minutes, seconds, tenths);
    }

    private void startClockUpdates() {
        clockHandler = new Handler();
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                updateClock();
                clockHandler.postDelayed(this, 100);
            }
        };
        clockHandler.post(clockRunnable);
    }

    private void updateClock() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.S", Locale.getDefault());
        tvClock.setText(timeFormat.format(new Date()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clockHandler != null && clockRunnable != null) {
            clockHandler.removeCallbacks(clockRunnable);
        }
        if (storage != null) {
            storage.close();
        }
    }
}
