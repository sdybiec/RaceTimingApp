package com.racingtimer.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.racingtimer.model.Crew;
import com.racingtimer.model.Race;
import com.racingtimer.service.TimingService;
import com.racingtimer.storage.StorageCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Start List View - displays and manages the list of crews in a race.
 */
public class StartListView extends Composite {

    private final TimingService timingService;

    // UI Components
    private final FlexTable crewTable;
    private final Label statusLabel;

    private Race currentRace;
    private final List<Crew> crews = new ArrayList<>();

    public StartListView(TimingService timingService) {
        this.timingService = timingService;

        // Main container
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setStyleName("start-list-container");

        // 1. Title bar
        mainPanel.add(createTitleBar());

        // 2. Crew table
        crewTable = createCrewTable();
        ScrollPanel tableScroll = new ScrollPanel(crewTable);
        tableScroll.setStyleName("crew-table-scroll");
        mainPanel.add(tableScroll);

        // 3. Status label
        statusLabel = new Label("Number of racers: 0");
        statusLabel.setStyleName("status-label");
        mainPanel.add(statusLabel);

        // 4. Bottom toolbar
        mainPanel.add(createBottomToolbar());

        // Initialize with demo race
        initializeDemoRace();

        initWidget(mainPanel);
    }

    private Panel createTitleBar() {
        HorizontalPanel titleBar = new HorizontalPanel();
        titleBar.setStyleName("title-bar");
        titleBar.setWidth("100%");

        Button raceFinishButton = new Button("◄ Race Finish");
        raceFinishButton.setStyleName("title-bar-button");

        HTML titleText = new HTML("<div class='title-text'><span class='status-indicator'></span>Start List</div>");

        titleBar.add(raceFinishButton);
        titleBar.add(titleText);

        titleBar.setCellWidth(raceFinishButton, "150px");
        titleBar.setCellHorizontalAlignment(titleText, HasHorizontalAlignment.ALIGN_CENTER);

        return titleBar;
    }

    private FlexTable createCrewTable() {
        FlexTable table = new FlexTable();
        table.setStyleName("crew-table");
        table.setWidth("100%");

        // Header row
        table.setHTML(0, 0, "<input type='checkbox' class='select-all'/>");
        table.setText(0, 1, "Bib");
        table.setText(0, 2, "Name");
        table.setText(0, 3, "Category");
        table.setText(0, 4, "");

        table.getRowFormatter().setStyleName(0, "table-header");
        table.getCellFormatter().setWidth(0, 0, "40px");
        table.getCellFormatter().setWidth(0, 1, "60px");
        table.getCellFormatter().setWidth(0, 4, "40px");

        return table;
    }

    private Panel createBottomToolbar() {
        HorizontalPanel toolbar = new HorizontalPanel();
        toolbar.setStyleName("bottom-toolbar");
        toolbar.setWidth("100%");

        String[] buttons = {"❓\nHelp", "↕\nSort list", "📡\nPost\nstart list",
                "🔍\nSearch\nby bib", "DNS\nDid not\nstart", "➕\nAdd\nracer"};

        for (String label : buttons) {
            HTML button = new HTML(label.replace("\n", "<br>"));
            button.setStyleName("toolbar-button");
            toolbar.add(button);
        }

        return toolbar;
    }

    private void initializeDemoRace() {
        currentRace = new Race("race-1", "Girls Regatta", new Date());
        currentRace.setStatus(Race.RaceStatus.SCHEDULED);

        // Create demo crews matching the mockup
        crews.addAll(createDemoCrews());

        // Populate table
        refreshCrewTable();
    }

    private List<Crew> createDemoCrews() {
        List<Crew> demoCrews = new ArrayList<>();

        // Varsity Girls
        demoCrews.add(createCrew("1", 1, "Sharon Hart", "Varsity Girls"));
        demoCrews.add(createCrew("2", 2, "Lydia Weyand", "Varsity Girls"));
        demoCrews.add(createCrew("3", 3, "Susannah Hart", "Varsity Girls"));
        demoCrews.add(createCrew("4", 4, "Cassie Ross", "Varsity Girls"));

        // JV Girls
        demoCrews.add(createCrew("51", 51, "Lauren Paulsen", "JV Girls"));
        demoCrews.add(createCrew("52", 52, "Korina Waring-Enriquez", "JV Girls"));
        demoCrews.add(createCrew("53", 53, "Robin Thomas", "JV Girls"));
        demoCrews.add(createCrew("54", 54, "Anne Bania", "JV Girls"));
        demoCrews.add(createCrew("55", 55, "Isabella Livingston", "JV Girls"));
        demoCrews.add(createCrew("56", 56, "Brooke Brown", "JV Girls"));
        demoCrews.add(createCrew("57", 57, "Madison Braden", "JV Girls"));
        demoCrews.add(createCrew("58", 58, "Elle Lee", "JV Girls"));

        // Intermediate Girls
        demoCrews.add(createCrew("101", 101, "Morgan Mackaay", "Intermediate Girls"));
        Crew dnsCrew = createCrew("103", 103, "Anika Vroom", "Intermediate Girls");
        dnsCrew.setStatus(Crew.CrewStatus.DNS);
        demoCrews.add(dnsCrew);
        demoCrews.add(createCrew("104", 104, "Alena Tuttle", "Intermediate Girls"));
        demoCrews.add(createCrew("105", 105, "Tian Qing Yen", "Intermediate Girls"));
        demoCrews.add(createCrew("108", 108, "Camille Ottaway", "Intermediate Girls"));
        demoCrews.add(createCrew("110", 110, "Chiara D'angelo", "Intermediate Girls"));
        demoCrews.add(createCrew("111", 111, "Annalise Rubida", "Intermediate Girls"));

        // Beginner Girls
        demoCrews.add(createCrew("151", 151, "Kayla Lampert", "Beginner Girls"));
        demoCrews.add(createCrew("152", 152, "Meghan L. Duft", "Beginner Girls"));
        demoCrews.add(createCrew("153", 153, "Samantha Alvarez", "Beginner Girls"));
        demoCrews.add(createCrew("154", 154, "Chiara Cella", "Beginner Girls"));
        demoCrews.add(createCrew("155", 155, "Kari Slotten", "Beginner Girls"));

        return demoCrews;
    }

    private Crew createCrew(String id, int bibNumber, String name, String category) {
        return new Crew("crew-" + id, bibNumber, name, category, currentRace.getId());
    }

    private void refreshCrewTable() {
        // Clear table except header
        while (crewTable.getRowCount() > 1) {
            crewTable.removeRow(1);
        }

        // Add crew rows
        for (Crew crew : crews) {
            addCrewRow(crew);
        }

        // Update status
        statusLabel.setText("Number of racers: " + crews.size());
    }

    private void addCrewRow(final Crew crew) {
        int row = crewTable.getRowCount();

        // Checkmark or empty
        if (crew.getStatus() == Crew.CrewStatus.ACTIVE) {
            crewTable.setHTML(row, 0, "<span class='checkmark'>✓</span>");
        } else {
            crewTable.setText(row, 0, "");
        }

        // Bib number
        crewTable.setText(row, 1, String.valueOf(crew.getBibNumber()));

        // Name
        crewTable.setText(row, 2, crew.getName());

        // Category
        HTML categoryCell = new HTML(crew.getCategory());
        if (crew.getStatus() == Crew.CrewStatus.DNS) {
            categoryCell.setHTML(crew.getCategory() + " <span class='dns-badge'>DNS</span>");
        }
        crewTable.setWidget(row, 3, categoryCell);

        // Edit button
        Button editButton = new Button("►");
        editButton.setStyleName("edit-button");
        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showRacerInfoDialog(crew);
            }
        });
        crewTable.setWidget(row, 4, editButton);

        // Style row based on status
        if (crew.getStatus() != Crew.CrewStatus.ACTIVE) {
            crewTable.getRowFormatter().addStyleName(row, "inactive-crew");
        }
    }

    private void showRacerInfoDialog(Crew crew) {
        // Show racer info dialog
        RacerInfoDialog dialog = new RacerInfoDialog(crew, crews.size(), new RacerInfoDialog.SaveCallback() {
            @Override
            public void onSave(Crew updatedCrew) {
                // Update crew in list
                for (int i = 0; i < crews.size(); i++) {
                    if (crews.get(i).getId().equals(updatedCrew.getId())) {
                        crews.set(i, updatedCrew);
                        break;
                    }
                }
                refreshCrewTable();
            }
        });
        dialog.center();
        dialog.show();
    }
}
