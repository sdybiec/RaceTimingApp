package com.racingtimer.android;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.racingtimer.android.adapter.TimingEntryAdapter;
import com.racingtimer.android.storage.SQLiteTimingStorage;
import com.racingtimer.model.Race;
import com.racingtimer.model.TimingEntry;
import com.racingtimer.service.TimingService;
import com.racingtimer.storage.StorageCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main Activity for Race Finish timing screen.
 */
public class RaceFinishActivity extends AppCompatActivity {

    private TextView tvClock;
    private TextView tvInputDisplay;
    private RecyclerView rvTimingTable;
    private TimingEntryAdapter adapter;

    private TimingService timingService;
    private SQLiteTimingStorage storage;

    private final List<TimingEntry> entries = new ArrayList<>();
    private int selectedRow = -1;
    private StringBuilder bibInput = new StringBuilder();

    private Handler clockHandler;
    private Runnable clockRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_finish);

        // Initialize storage and services
        storage = new SQLiteTimingStorage(this);
        timingService = new TimingService(storage);

        // Initialize demo race
        initializeDemoRace();

        // Initialize views
        tvClock = findViewById(R.id.tvClock);
        tvInputDisplay = findViewById(R.id.tvInputDisplay);
        rvTimingTable = findViewById(R.id.rvTimingTable);

        // Setup RecyclerView
        adapter = new TimingEntryAdapter();
        adapter.setOnEntryClickListener(position -> {
            selectedRow = position;
            adapter.setSelectedPosition(position);
        });
        rvTimingTable.setLayoutManager(new LinearLayoutManager(this));
        rvTimingTable.setAdapter(adapter);

        // Setup timestamp capture button
        View timestampButton = findViewById(R.id.timestampCaptureButton);
        timestampButton.setOnClickListener(v -> recordTimestamp());

        // Setup keypad buttons
        setupKeypadButtons();

        // Setup title bar buttons
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            // Navigate to home
        });
        findViewById(R.id.btnResults).setOnClickListener(v -> {
            // Navigate to results
        });

        // Start clock updates
        startClockUpdates();
    }

    private void setupKeypadButtons() {
        int[] numberButtons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int i = 0; i < numberButtons.length; i++) {
            final int number = i;
            findViewById(numberButtons[i]).setOnClickListener(v -> handleNumberInput(number));
        }

        findViewById(R.id.btnBackspace).setOnClickListener(v -> handleBackspace());
        findViewById(R.id.btnNoBib).setOnClickListener(v -> handleNoBib());
        findViewById(R.id.btnEnter).setOnClickListener(v -> handleEnter());
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
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        tvClock.setText(timeFormat.format(new Date()));
    }

    private void recordTimestamp() {
        timingService.recordTimestamp(new StorageCallback<TimingEntry>() {
            @Override
            public void onSuccess(TimingEntry entry) {
                runOnUiThread(() -> {
                    entries.add(entry);
                    adapter.addEntry(entry);
                    selectedRow = entries.size() - 1;
                    adapter.setSelectedPosition(selectedRow);

                    // Auto-scroll to new entry
                    rvTimingTable.smoothScrollToPosition(adapter.getItemCount() - 1);

                    // If there's a bib number pre-entered, assign it
                    if (bibInput.length() > 0) {
                        int bibNumber = Integer.parseInt(bibInput.toString());
                        assignBibToSelectedEntry(bibNumber);
                        bibInput.setLength(0);
                        updateInputDisplay();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> tvInputDisplay.setText("Error: " + error));
            }
        });
    }

    private void handleNumberInput(int number) {
        bibInput.append(number);
        updateInputDisplay();
    }

    private void handleBackspace() {
        if (bibInput.length() > 0) {
            bibInput.setLength(bibInput.length() - 1);
            updateInputDisplay();
        }
    }

    private void handleNoBib() {
        bibInput.setLength(0);
        updateInputDisplay();

        if (selectedRow >= 0 && selectedRow < entries.size()) {
            TimingEntry entry = entries.get(selectedRow);
            entry.setBibNumber(null);
            adapter.notifyItemChanged(selectedRow);
        }
    }

    private void handleEnter() {
        if (bibInput.length() > 0 && selectedRow >= 0) {
            int bibNumber = Integer.parseInt(bibInput.toString());
            assignBibToSelectedEntry(bibNumber);
            bibInput.setLength(0);
            updateInputDisplay();
        }
    }

    private void assignBibToSelectedEntry(int bibNumber) {
        if (selectedRow >= 0 && selectedRow < entries.size()) {
            TimingEntry entry = entries.get(selectedRow);
            timingService.assignBibNumber(entry.getId(), bibNumber, new StorageCallback<TimingEntry>() {
                @Override
                public void onSuccess(TimingEntry updatedEntry) {
                    runOnUiThread(() -> {
                        entries.set(selectedRow, updatedEntry);
                        adapter.notifyItemChanged(selectedRow);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> tvInputDisplay.setText("Error: " + error));
                }
            });
        }
    }

    private void updateInputDisplay() {
        if (bibInput.length() == 0) {
            tvInputDisplay.setText(R.string.input_prompt);
        } else {
            tvInputDisplay.setText("Bib #: " + bibInput);
        }
    }

    private void initializeDemoRace() {
        Race demoRace = new Race("race-1", "Middle School Boys", new Date());
        demoRace.setStatus(Race.RaceStatus.IN_PROGRESS);
        timingService.startRace(demoRace);
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
