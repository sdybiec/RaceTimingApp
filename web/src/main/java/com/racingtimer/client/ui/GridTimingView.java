package com.racingtimer.client.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import com.racingtimer.model.Crew;
import com.racingtimer.model.Race;
import com.racingtimer.model.TimingEntry;
import com.racingtimer.service.TimingService;
import com.racingtimer.storage.StorageCallback;

import java.util.*;

/**
 * Grid Timing View - displays a grid of buttons, one per crew.
 * Each button captures timestamp and bib number with a single tap.
 */
public class GridTimingView extends Composite {

    private final TimingService timingService;

    // UI Components
    private final Label clockLabel;
    private final Label raceNameLabel;
    private final FlowPanel buttonGrid;
    private final Label statusLabel;

    // Button state tracking
    private final Map<Integer, CrewButton> crewButtons = new HashMap<>();
    private final Map<Integer, Timer> draftTimers = new HashMap<>();
    private final Map<Integer, TimingEntry> draftEntries = new HashMap<>();

    private Race currentRace;
    private int finishCount = 0;

    public GridTimingView(TimingService timingService) {
        this.timingService = timingService;

        // Main container
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setStyleName("grid-timing-container");

        // 1. Menu bar
        mainPanel.add(createMenuBar());

        // 2. Title panel
        Panel titlePanel = createTitlePanel();
        raceNameLabel = (Label) ((VerticalPanel) titlePanel).getWidget(0);
        clockLabel = (Label) ((VerticalPanel) titlePanel).getWidget(1);
        mainPanel.add(titlePanel);

        // 3. Button grid
        buttonGrid = new FlowPanel();
        buttonGrid.setStyleName("crew-button-grid");
        ScrollPanel gridScroll = new ScrollPanel(buttonGrid);
        gridScroll.setStyleName("grid-scroll-panel");
        mainPanel.add(gridScroll);

        // 4. Status label
        statusLabel = new Label("Tap bib when racer finishes");
        statusLabel.setStyleName("status-label");
        mainPanel.add(statusLabel);

        // 5. Bottom toolbar
        mainPanel.add(createBottomToolbar());

        // Initialize with demo race
        initializeDemoRace();

        // Start clock timer
        startClockTimer();

        initWidget(mainPanel);
    }

    private Panel createMenuBar() {
        HorizontalPanel menuBar = new HorizontalPanel();
        menuBar.setStyleName("title-bar");
        menuBar.setWidth("100%");

        Button homeButton = new Button("◄ Home");
        homeButton.setStyleName("title-bar-button");

        HTML titleText = new HTML("<div class='title-text'><span class='status-indicator'></span>Race Finish</div>");

        Button resultsButton = new Button("Results ►");
        resultsButton.setStyleName("title-bar-button");

        menuBar.add(homeButton);
        menuBar.add(titleText);
        menuBar.add(resultsButton);

        menuBar.setCellWidth(homeButton, "120px");
        menuBar.setCellWidth(resultsButton, "120px");
        menuBar.setCellHorizontalAlignment(titleText, HasHorizontalAlignment.ALIGN_CENTER);

        return menuBar;
    }

    private Panel createTitlePanel() {
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("grid-title-panel");
        panel.setWidth("100%");

        Label raceName = new Label("Bellevue Cup MTB Race");
        raceName.setStyleName("race-name-label");

        Label clock = new Label("20:14.9");
        clock.setStyleName("race-clock-label");

        panel.add(raceName);
        panel.add(clock);

        return panel;
    }

    private Panel createBottomToolbar() {
        HorizontalPanel toolbar = new HorizontalPanel();
        toolbar.setStyleName("bottom-toolbar");
        toolbar.setWidth("100%");

        String[] buttons = {"❓\nHelp", "⚠\nRace\ncontrol", "📋\nStart list",
                "👤\nShow\nname", "DNS\nDNF\nDSQ", "👁\nFast-lap\nview",
                "+/−\nShow\nsplits"};

        for (String label : buttons) {
            HTML button = new HTML(label.replace("\n", "<br>"));
            button.setStyleName("toolbar-button");
            toolbar.add(button);
        }

        return toolbar;
    }

    private void startClockTimer() {
        Timer clockTimer = new Timer() {
            @Override
            public void run() {
                Date now = new Date();
                DateTimeFormat timeFormat = DateTimeFormat.getFormat("HH:mm:ss.S");
                clockLabel.setText(timeFormat.format(now));
            }
        };
        clockTimer.scheduleRepeating(100);
    }

    private void initializeDemoRace() {
        currentRace = new Race("race-1", "Bellevue Cup MTB Race", new Date());
        currentRace.setStatus(Race.RaceStatus.IN_PROGRESS);
        timingService.startRace(currentRace);

        // Create demo crews (as shown in mockup)
        List<Crew> demoCrews = createDemoCrews();
        for (Crew crew : demoCrews) {
            addCrewButton(crew);
        }

        updateStatusLabel();
    }

    private List<Crew> createDemoCrews() {
        List<Crew> crews = new ArrayList<>();

        // Add crews matching the mockup
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

    private void addCrewButton(Crew crew) {
        CrewButton button = new CrewButton(crew);
        crewButtons.put(crew.getBibNumber(), button);
        buttonGrid.add(button);
    }

    private void handleCrewButtonClick(int bibNumber) {
        CrewButton button = crewButtons.get(bibNumber);
        if (button == null) return;

        switch (button.state) {
            case READY:
                // Capture timestamp in draft state
                captureTimestamp(bibNumber, button);
                break;

            case DRAFT:
                // Cancel draft
                cancelDraft(bibNumber, button);
                break;

            case FINALIZED:
                // Do nothing
                break;
        }
    }

    private void captureTimestamp(final int bibNumber, final CrewButton button) {
        timingService.recordTimestamp(new StorageCallback<TimingEntry>() {
            @Override
            public void onSuccess(TimingEntry entry) {
                // Set bib number
                entry.setBibNumber(bibNumber);
                entry.setRaceId(currentRace.getId());

                // Update to draft state
                button.setState(ButtonState.DRAFT);
                draftEntries.put(bibNumber, entry);

                // Start 5-second timer
                Timer draftTimer = new Timer() {
                    @Override
                    public void run() {
                        finalizeDraft(bibNumber, button, entry);
                    }
                };
                draftTimers.put(bibNumber, draftTimer);
                draftTimer.schedule(5000); // 5 seconds

                statusLabel.setText("Bib " + bibNumber + " - tap again to cancel");
            }

            @Override
            public void onError(String error) {
                statusLabel.setText("Error: " + error);
            }
        });
    }

    private void cancelDraft(int bibNumber, CrewButton button) {
        // Cancel timer
        Timer timer = draftTimers.remove(bibNumber);
        if (timer != null) {
            timer.cancel();
        }

        // Remove draft entry
        TimingEntry entry = draftEntries.remove(bibNumber);
        if (entry != null) {
            // Delete from storage
            timingService.getCurrentRace(); // TODO: delete entry
        }

        // Return to ready state
        button.setState(ButtonState.READY);
        updateStatusLabel();
    }

    private void finalizeDraft(int bibNumber, CrewButton button, TimingEntry entry) {
        // Remove from draft tracking
        draftTimers.remove(bibNumber);
        draftEntries.remove(bibNumber);

        // Finalize entry
        button.setState(ButtonState.FINALIZED);
        button.setVisible(false);

        finishCount++;
        updateStatusLabel();
    }

    private void updateStatusLabel() {
        int totalCrews = crewButtons.size();
        statusLabel.setText("Tap bib when racer finishes                " +
                          finishCount + " - " + totalCrews);
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
     * Custom button widget for crew timing
     */
    private class CrewButton extends Composite {
        private final Crew crew;
        private ButtonState state = ButtonState.READY;
        private final Button button;

        public CrewButton(Crew crew) {
            this.crew = crew;

            button = new Button();
            updateButtonDisplay();

            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    handleCrewButtonClick(crew.getBibNumber());
                }
            });

            initWidget(button);
        }

        public void setState(ButtonState newState) {
            this.state = newState;
            updateButtonDisplay();
        }

        private void updateButtonDisplay() {
            String bibText = String.valueOf(crew.getBibNumber());
            button.setHTML("<div class='bib-number'>" + bibText + "</div>" +
                          "<div class='button-text'>Tap to finish</div>");

            switch (state) {
                case READY:
                    button.setStyleName("crew-button crew-button-ready");
                    break;
                case DRAFT:
                    button.setStyleName("crew-button crew-button-draft");
                    break;
                case FINALIZED:
                    button.setStyleName("crew-button crew-button-finalized");
                    break;
            }
        }
    }
}
