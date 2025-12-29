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
 * Grid Timing Activity - displays a grid of buttons, one per crew.
 */
public class GridTimingActivity extends AppCompatActivity {

    private TextView tvRaceName;
    private TextView tvRaceClock;
    private GridLayout crewButtonGrid;
    private TextView tvStatus;

    private TimingService timingService;
    private SQLiteTimingStorage storage;

    private Race currentRace;
    private final Map<Integer, CrewButton> crewButtons = new HashMap<>();
    private final Map<Integer, Runnable> draftTimers = new HashMap<>();
    private final Map<Integer, TimingEntry> draftEntries = new HashMap<>();

    private int finishCount = 0;
    private int totalCrews = 0;

    private Handler clockHandler;
    private Runnable clockRunnable;
    private Handler draftHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_timing);

        // Initialize storage and services
        storage = new SQLiteTimingStorage(this);
        timingService = new TimingService(storage);

        // Initialize views
        tvRaceName = findViewById(R.id.tvRaceName);
        tvRaceClock = findViewById(R.id.tvRaceClock);
        crewButtonGrid = findViewById(R.id.crewButtonGrid);
        tvStatus = findViewById(R.id.tvStatus);

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
        currentRace = new Race("race-1", "Bellevue Cup MTB Race", new Date());
        currentRace.setStatus(Race.RaceStatus.IN_PROGRESS);
        timingService.startRace(currentRace);

        tvRaceName.setText(currentRace.getName());

        // Create demo crews (matching mockup)
        List<Crew> demoCrews = createDemoCrews();
        totalCrews = demoCrews.size();

        // Add "No Bib" button first
        addNoBibButton();

        // Add crew buttons
        for (Crew crew : demoCrews) {
            addCrewButton(crew);
        }

        updateStatusLabel();
    }

    private List<Crew> createDemoCrews() {
        List<Crew> crews = new ArrayList<>();

        // Bib numbers from the mockup
        int[] bibNumbers = {
            201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 212, 213,
            214, 216, 217, 218, 219, 220, 221, 222, 223, 251, 252, 253, 254,
            255, 256, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311,
            401, 402, 403, 404, 406, 407, 408, 409, 410, 411, 412, 413, 414,
            416, 418, 421, 423, 424, 425, 426, 427, 428, 429, 431, 432,
            502, 503, 504, 505, 506, 507, 508, 509, 510, 512, 513, 514, 515, 516,
            517, 518, 519, 520, 521, 522, 523, 524, 526, 527, 529, 530, 531,
            532, 535, 536, 537, 539, 540,
            932, 933, 934, 936
        };

        for (int bib : bibNumbers) {
            Crew crew = new Crew(
                "crew-" + bib,
                bib,
                "Crew " + bib,
                "Category",
                currentRace.getId()
            );
            crews.add(crew);
        }

        return crews;
    }

    private void addNoBibButton() {
        Button button = createCrewButton();
        button.setText("No\nBib");
        button.setBackground(ContextCompat.getDrawable(this, R.drawable.crew_button_no_bib));
        button.setTextColor(Color.WHITE);
        button.setOnClickListener(v -> {
            // Handle no bib
        });
        crewButtonGrid.addView(button);
    }

    private void addCrewButton(Crew crew) {
        CrewButton crewButton = new CrewButton(crew);
        crewButtons.put(crew.getBibNumber(), crewButton);
        crewButtonGrid.addView(crewButton.button);
    }

    private Button createCrewButton() {
        Button button = new Button(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(5, 5, 5, 5);
        button.setLayoutParams(params);
        button.setMinHeight(120);
        button.setGravity(Gravity.CENTER);
        button.setPadding(8, 12, 8, 12);
        button.setTextSize(14);
        return button;
    }

    private void handleCrewButtonClick(int bibNumber) {
        CrewButton crewButton = crewButtons.get(bibNumber);
        if (crewButton == null) return;

        switch (crewButton.state) {
            case READY:
                captureTimestamp(bibNumber, crewButton);
                break;

            case DRAFT:
                cancelDraft(bibNumber, crewButton);
                break;

            case FINALIZED:
                // Do nothing
                break;
        }
    }

    private void captureTimestamp(final int bibNumber, final CrewButton crewButton) {
        timingService.recordTimestamp(new StorageCallback<TimingEntry>() {
            @Override
            public void onSuccess(TimingEntry entry) {
                runOnUiThread(() -> {
                    // Set bib number
                    entry.setBibNumber(bibNumber);
                    entry.setRaceId(currentRace.getId());

                    // Update to draft state
                    crewButton.setState(ButtonState.DRAFT);
                    draftEntries.put(bibNumber, entry);

                    // Start 5-second timer
                    Runnable draftTimer = () -> finalizeDraft(bibNumber, crewButton, entry);
                    draftTimers.put(bibNumber, draftTimer);
                    draftHandler.postDelayed(draftTimer, 5000); // 5 seconds

                    tvStatus.setText("Bib " + bibNumber + " - tap again to cancel");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> tvStatus.setText("Error: " + error));
            }
        });
    }

    private void cancelDraft(int bibNumber, CrewButton crewButton) {
        // Cancel timer
        Runnable timer = draftTimers.remove(bibNumber);
        if (timer != null) {
            draftHandler.removeCallbacks(timer);
        }

        // Remove draft entry
        draftEntries.remove(bibNumber);

        // Return to ready state
        crewButton.setState(ButtonState.READY);
        updateStatusLabel();
    }

    private void finalizeDraft(int bibNumber, CrewButton crewButton, TimingEntry entry) {
        // Remove from draft tracking
        draftTimers.remove(bibNumber);
        draftEntries.remove(bibNumber);

        // Finalize entry - save to storage
        storage.saveTimingEntry(entry, new StorageCallback<TimingEntry>() {
            @Override
            public void onSuccess(TimingEntry result) {
                runOnUiThread(() -> {
                    crewButton.setState(ButtonState.FINALIZED);
                    finishCount++;
                    updateStatusLabel();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> tvStatus.setText("Error saving: " + error));
            }
        });
    }

    private void updateStatusLabel() {
        tvStatus.setText("Tap bib when racer finishes                " +
                        finishCount + " - " + totalCrews);
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
        tvRaceClock.setText(timeFormat.format(new Date()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clockHandler != null && clockRunnable != null) {
            clockHandler.removeCallbacks(clockRunnable);
        }
        if (draftHandler != null) {
            draftHandler.removeCallbacksAndMessages(null);
        }
        if (storage != null) {
            storage.close();
        }
    }

    /**
     * Button state enum
     */
    private enum ButtonState {
        READY,      // Green - ready to capture
        DRAFT,      // Yellow - captured but can be cancelled
        FINALIZED   // Hidden - finalized
    }

    /**
     * Wrapper class for crew button with state management
     */
    private class CrewButton {
        final Crew crew;
        final Button button;
        ButtonState state = ButtonState.READY;

        CrewButton(Crew crew) {
            this.crew = crew;
            this.button = createCrewButton();
            updateButtonDisplay();

            button.setOnClickListener(v -> handleCrewButtonClick(crew.getBibNumber()));
        }

        void setState(ButtonState newState) {
            this.state = newState;
            updateButtonDisplay();
        }

        void updateButtonDisplay() {
            String text = String.valueOf(crew.getBibNumber()) + "\nTap to finish";
            button.setText(text);

            switch (state) {
                case READY:
                    button.setBackground(ContextCompat.getDrawable(
                        GridTimingActivity.this, R.drawable.crew_button_ready));
                    button.setTextColor(Color.BLACK);
                    button.setVisibility(View.VISIBLE);
                    break;

                case DRAFT:
                    button.setBackground(ContextCompat.getDrawable(
                        GridTimingActivity.this, R.drawable.crew_button_draft));
                    button.setTextColor(Color.BLACK);
                    button.setVisibility(View.VISIBLE);
                    break;

                case FINALIZED:
                    button.setVisibility(View.GONE);
                    break;
            }
        }
    }
}
