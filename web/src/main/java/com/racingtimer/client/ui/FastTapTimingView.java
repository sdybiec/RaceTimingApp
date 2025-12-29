package com.racingtimer.client.ui;

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
 * Fast Tap Timing View - separates timestamp capture from bib assignment.
 * Allows multiple timestamps to be captured in quick succession,
 * then bib numbers assigned in order.
 */
public class FastTapTimingView extends Composite {

    private final TimingService timingService;

    // UI Components
    private final Label clockLabel;
    private final FlowPanel bibButtonGrid;
    private final FlexTable resultsTable;
    private final Label statusLabel;

    // Data tracking
    private Race currentRace;
    private final List<TimingEntry> unassignedEntries = new ArrayList<>();
    private final List<TimingEntry> assignedEntries = new ArrayList<>();
    private final Map<Integer, Button> bibButtons = new HashMap<>();

    public FastTapTimingView(TimingService timingService) {
        this.timingService = timingService;

        // Main container
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setStyleName("fast-tap-container");

        // 1. Title bar
        mainPanel.add(createTitleBar());

        // 2. Timestamp capture button
        Panel timestampPanel = createTimestampCapturePanel();
        clockLabel = (Label) ((VerticalPanel) timestampPanel.getWidget(0)).getWidget(0);
        mainPanel.add(timestampPanel);

        // 3. Bib number button grid
        bibButtonGrid = new FlowPanel();
        bibButtonGrid.setStyleName("bib-button-grid");
        ScrollPanel bibGridScroll = new ScrollPanel(bibButtonGrid);
        bibGridScroll.setStyleName("bib-grid-scroll-panel");
        mainPanel.add(bibGridScroll);

        // 4. Results table
        resultsTable = createResultsTable();
        ScrollPanel tableScroll = new ScrollPanel(resultsTable);
        tableScroll.setStyleName("results-table-scroll");
        mainPanel.add(tableScroll);

        // 5. Status label
        statusLabel = new Label("Tap timer button or select a bib # above");
        statusLabel.setStyleName("status-label");
        mainPanel.add(statusLabel);

        // 6. Bottom toolbar
        mainPanel.add(createBottomToolbar());

        // Initialize demo race
        initializeDemoRace();

        // Start clock timer
        startClockTimer();

        initWidget(mainPanel);
    }

    private Panel createTitleBar() {
        HorizontalPanel titleBar = new HorizontalPanel();
        titleBar.setStyleName("title-bar");
        titleBar.setWidth("100%");

        Button homeButton = new Button("◄ Home");
        homeButton.setStyleName("title-bar-button");

        HTML titleText = new HTML("<div class='title-text'><span class='status-indicator'></span>Race Finish</div>");

        Button resultsButton = new Button("Results ►");
        resultsButton.setStyleName("title-bar-button");

        titleBar.add(homeButton);
        titleBar.add(titleText);
        titleBar.add(resultsButton);

        titleBar.setCellWidth(homeButton, "120px");
        titleBar.setCellWidth(resultsButton, "120px");
        titleBar.setCellHorizontalAlignment(titleText, HasHorizontalAlignment.ALIGN_CENTER);

        return titleBar;
    }

    private Panel createTimestampCapturePanel() {
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("timestamp-capture-fasttap");
        panel.setWidth("100%");

        Label timeLabel = new Label("20:58.9");
        timeLabel.setStyleName("timestamp-display");

        Label instructionLabel = new Label("Tap to record a time");
        instructionLabel.setStyleName("timestamp-instruction");

        panel.add(timeLabel);
        panel.add(instructionLabel);

        panel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                captureTimestamp();
            }
        }, ClickEvent.getType());

        return panel;
    }

    private FlexTable createResultsTable() {
        FlexTable table = new FlexTable();
        table.setStyleName("timing-table");
        table.setWidth("100%");

        // Header row
        table.setText(0, 0, "Seq #");
        table.setText(0, 1, "Bib");
        table.setText(0, 2, "Name");
        table.setText(0, 3, "Category");
        table.setText(0, 4, "Race clock");

        table.getRowFormatter().setStyleName(0, "table-header");

        return table;
    }

    private Panel createBottomToolbar() {
        HorizontalPanel toolbar = new HorizontalPanel();
        toolbar.setStyleName("bottom-toolbar");
        toolbar.setWidth("100%");

        String[] buttons = {"❓\nHelp", "⚠\nRace\ncontrol", "📋\nStart list",
                "👤\nShow\nname", "DNS\nDNF\nDSQ", "👁\nNormal\nview",
                "⌨\nKeypad\nview", "+/−\nShow\nsplits"};

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
        currentRace = new Race("race-1", "Middle School Boys", new Date());
        currentRace.setStatus(Race.RaceStatus.IN_PROGRESS);
        timingService.startRace(currentRace);

        // Create bib buttons for crews
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

        // Add some names for demo data
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
        Button button = new Button();
        button.setHTML("<div class='bib-text'>" + crew.getBibNumber() + "</div>" +
                      "<div class='button-subtext'>Tap to finish</div>");
        button.setStyleName("bib-button");

        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                assignBibToNextEntry(crew);
            }
        });

        bibButtons.put(crew.getBibNumber(), button);
        bibButtonGrid.add(button);
    }

    private void captureTimestamp() {
        timingService.recordTimestamp(new StorageCallback<TimingEntry>() {
            @Override
            public void onSuccess(TimingEntry entry) {
                entry.setRaceId(currentRace.getId());
                unassignedEntries.add(entry);
                addUnassignedRowToTable(entry);
                updateStatus();
            }

            @Override
            public void onError(String error) {
                statusLabel.setText("Error: " + error);
            }
        });
    }

    private void assignBibToNextEntry(Crew crew) {
        if (unassignedEntries.isEmpty()) {
            statusLabel.setText("No unassigned timestamps. Tap timer button first.");
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
                    assignedEntries.add(updatedEntry);
                    refreshResultsTable();
                    updateStatus(crew.getName() + " (" + crew.getCategory() + "): " +
                               updatedEntry.getFormattedRaceTime() + " " +
                               getPositionSuffix(assignedEntries.size()) + " +" +
                               formatSplitTime(updatedEntry));
                }

                @Override
                public void onError(String error) {
                    statusLabel.setText("Error: " + error);
                }
            });
    }

    private void addUnassignedRowToTable(TimingEntry entry) {
        int row = resultsTable.getRowCount();
        resultsTable.setText(row, 0, String.valueOf(entry.getSequenceNumber()));
        resultsTable.setText(row, 1, "-");
        resultsTable.setText(row, 2, "Select a bib # above");
        resultsTable.setText(row, 3, "");
        resultsTable.setHTML(row, 4, "<span class='race-clock'>" +
                            entry.getFormattedRaceTime() + "</span>");
        resultsTable.getRowFormatter().addStyleName(row, "unassigned-row");
    }

    private void refreshResultsTable() {
        // Clear table except header
        while (resultsTable.getRowCount() > 1) {
            resultsTable.removeRow(1);
        }

        // Add assigned entries
        for (TimingEntry entry : assignedEntries) {
            addAssignedRowToTable(entry);
        }

        // Add unassigned entries
        for (TimingEntry entry : unassignedEntries) {
            addUnassignedRowToTable(entry);
        }
    }

    private void addAssignedRowToTable(TimingEntry entry) {
        int row = resultsTable.getRowCount();
        resultsTable.setText(row, 0, String.valueOf(entry.getSequenceNumber()));
        resultsTable.setHTML(row, 1, "<span class='bib-number'>" +
                            entry.getBibNumber() + "</span>");
        resultsTable.setText(row, 2, entry.getCrewName() != null ? entry.getCrewName() : "");
        resultsTable.setText(row, 3, entry.getCategory() != null ? entry.getCategory() : "");
        resultsTable.setHTML(row, 4, "<span class='race-clock'>" +
                            entry.getFormattedRaceTime() + "</span>");
    }

    private void updateStatus() {
        int unassigned = unassignedEntries.size();
        int total = assignedEntries.size() + unassigned;
        statusLabel.setText("Tap timer button or select a bib # above                " +
                          (total - unassigned) + " - " + total);
    }

    private void updateStatus(String message) {
        int unassigned = unassignedEntries.size();
        int total = assignedEntries.size() + unassigned;
        statusLabel.setText(message + "                " +
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
        return String.format("%d:%02d.%d", minutes, seconds, tenths);
    }
}
