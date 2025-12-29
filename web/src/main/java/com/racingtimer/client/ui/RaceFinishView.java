package com.racingtimer.client.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import com.racingtimer.model.Race;
import com.racingtimer.model.TimingEntry;
import com.racingtimer.service.TimingService;
import com.racingtimer.storage.StorageCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Main Race Finish view with timestamp capture and timing table.
 */
public class RaceFinishView extends Composite {

    private final TimingService timingService;

    // UI Components
    private final Label clockLabel;
    private final FlexTable timingTable;
    private final Label inputDisplay;
    private final KeypadPanel keypad;

    private final List<TimingEntry> entries = new ArrayList<>();
    private int selectedRow = -1;
    private String bibInput = "";

    public RaceFinishView(TimingService timingService) {
        this.timingService = timingService;

        // Initialize demo race
        initializeDemoRace();

        // Main container
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setStyleName("race-finish-container");

        // 1. Title bar
        mainPanel.add(createTitleBar());

        // 2. Timestamp capture button
        Panel timestampPanel = createTimestampCapturePanel();
        clockLabel = (Label) ((VerticalPanel) timestampPanel.getWidget(0)).getWidget(0);
        mainPanel.add(timestampPanel);

        // 3. Timing table
        timingTable = createTimingTable();
        ScrollPanel tableScroll = new ScrollPanel(timingTable);
        tableScroll.setStyleName("timing-table-container");
        mainPanel.add(tableScroll);

        // 4. Input display
        inputDisplay = new Label("Tap timer button or pre-enter bib #");
        inputDisplay.setStyleName("input-display");
        mainPanel.add(inputDisplay);

        // 5. Keypad
        keypad = new KeypadPanel(new KeypadHandler() {
            @Override
            public void onNumberPressed(int number) {
                handleNumberInput(number);
            }

            @Override
            public void onBackspace() {
                handleBackspace();
            }

            @Override
            public void onNoBib() {
                handleNoBib();
            }

            @Override
            public void onEnter() {
                handleEnter();
            }
        });
        mainPanel.add(keypad);

        // 6. Bottom toolbar
        mainPanel.add(createBottomToolbar());

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
        panel.setStyleName("timestamp-capture");
        panel.setWidth("100%");

        Label timeLabel = new Label("21:24.8");
        timeLabel.setStyleName("timestamp-display");

        Label instructionLabel = new Label("Tap to record a time");
        instructionLabel.setStyleName("timestamp-instruction");

        panel.add(timeLabel);
        panel.add(instructionLabel);

        panel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                recordTimestamp();
            }
        }, ClickEvent.getType());

        return panel;
    }

    private FlexTable createTimingTable() {
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
                "⬇\nHide\nkeypad", "+/−\nShow\nsplits"};

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
                DateTimeFormat timeFormat = DateTimeFormat.getFormat("HH:mm:ss");
                clockLabel.setText(timeFormat.format(now));
            }
        };
        clockTimer.scheduleRepeating(100); // Update every 100ms
    }

    private void recordTimestamp() {
        timingService.recordTimestamp(new StorageCallback<TimingEntry>() {
            @Override
            public void onSuccess(TimingEntry entry) {
                entries.add(entry);
                addEntryToTable(entry);
                selectedRow = entries.size() - 1;
                highlightSelectedRow();

                // If there's a bib number pre-entered, assign it
                if (!bibInput.isEmpty()) {
                    int bibNumber = Integer.parseInt(bibInput);
                    assignBibToSelectedEntry(bibNumber);
                    bibInput = "";
                    updateInputDisplay();
                }
            }

            @Override
            public void onError(String error) {
                inputDisplay.setText("Error: " + error);
            }
        });
    }

    private void addEntryToTable(TimingEntry entry) {
        int row = timingTable.getRowCount();

        timingTable.setText(row, 0, String.valueOf(entry.getSequenceNumber()));

        String bibText = entry.getBibNumber() != null ?
                String.valueOf(entry.getBibNumber()) : "Enter a bib number";
        HTML bibCell = new HTML("<span class='bib-number" +
                (entry.getBibNumber() == null ? " empty" : "") + "'>" +
                bibText + "</span>");
        timingTable.setWidget(row, 1, bibCell);

        timingTable.setText(row, 2, entry.getCrewName() != null ? entry.getCrewName() : "");
        timingTable.setText(row, 3, entry.getCategory() != null ? entry.getCategory() : "");

        HTML raceClockCell = new HTML("<span class='race-clock'>" +
                entry.getFormattedRaceTime() + "</span>");
        timingTable.setWidget(row, 4, raceClockCell);

        // Make row clickable
        final int finalRow = row;
        timingTable.getRowFormatter().addStyleName(row, "table-row");
        for (int col = 0; col < 5; col++) {
            timingTable.getCellFormatter().getElement(row, col).setAttribute("data-row", String.valueOf(finalRow - 1));
        }
    }

    private void handleNumberInput(int number) {
        bibInput += number;
        updateInputDisplay();
    }

    private void handleBackspace() {
        if (!bibInput.isEmpty()) {
            bibInput = bibInput.substring(0, bibInput.length() - 1);
            updateInputDisplay();
        }
    }

    private void handleNoBib() {
        bibInput = "";
        updateInputDisplay();
        // Assign null bib to selected entry
        if (selectedRow >= 0 && selectedRow < entries.size()) {
            TimingEntry entry = entries.get(selectedRow);
            entry.setBibNumber(null);
            refreshTable();
        }
    }

    private void handleEnter() {
        if (!bibInput.isEmpty() && selectedRow >= 0) {
            int bibNumber = Integer.parseInt(bibInput);
            assignBibToSelectedEntry(bibNumber);
            bibInput = "";
            updateInputDisplay();
        }
    }

    private void assignBibToSelectedEntry(int bibNumber) {
        if (selectedRow >= 0 && selectedRow < entries.size()) {
            TimingEntry entry = entries.get(selectedRow);
            timingService.assignBibNumber(entry.getId(), bibNumber, new StorageCallback<TimingEntry>() {
                @Override
                public void onSuccess(TimingEntry updatedEntry) {
                    entries.set(selectedRow, updatedEntry);
                    refreshTable();
                }

                @Override
                public void onError(String error) {
                    inputDisplay.setText("Error: " + error);
                }
            });
        }
    }

    private void updateInputDisplay() {
        if (bibInput.isEmpty()) {
            inputDisplay.setText("Tap timer button or pre-enter bib #");
        } else {
            inputDisplay.setText("Bib #: " + bibInput);
        }
    }

    private void highlightSelectedRow() {
        for (int i = 1; i < timingTable.getRowCount(); i++) {
            timingTable.getRowFormatter().removeStyleName(i, "selected");
        }
        if (selectedRow >= 0) {
            timingTable.getRowFormatter().addStyleName(selectedRow + 1, "selected");
        }
    }

    private void refreshTable() {
        while (timingTable.getRowCount() > 1) {
            timingTable.removeRow(1);
        }
        for (TimingEntry entry : entries) {
            addEntryToTable(entry);
        }
        highlightSelectedRow();
    }

    private void initializeDemoRace() {
        Race demoRace = new Race("race-1", "Middle School Boys", new Date());
        demoRace.setStatus(Race.RaceStatus.IN_PROGRESS);
        timingService.startRace(demoRace);
    }
}
