package com.racingtimer.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.racingtimer.model.Crew;

/**
 * Dialog for editing racer information.
 */
public class RacerInfoDialog extends DialogBox {

    private final Crew crew;
    private final int totalRacers;
    private final SaveCallback saveCallback;

    // Form fields
    private TextBox nameField;
    private ListBox categoryField;
    private ListBox bibNumberField;
    private TextBox teamNameField;
    private TextBox teamName2Field;
    private ListBox ageField;
    private ListBox genderField;

    public interface SaveCallback {
        void onSave(Crew crew);
    }

    public RacerInfoDialog(Crew crew, int totalRacers, SaveCallback saveCallback) {
        this.crew = crew;
        this.totalRacers = totalRacers;
        this.saveCallback = saveCallback;

        setStyleName("racer-info-dialog");
        setGlassEnabled(true);

        // Main panel
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setStyleName("racer-info-panel");
        mainPanel.setWidth("600px");

        // Title bar
        mainPanel.add(createTitleBar());

        // Racer number indicator
        Label racerNumber = new Label("Racer " + crew.getBibNumber() + " of " + totalRacers);
        racerNumber.setStyleName("racer-number-label");
        mainPanel.add(racerNumber);

        // Orange separator
        HTML separator = new HTML("<div class='orange-separator'></div>");
        mainPanel.add(separator);

        // Form
        mainPanel.add(createForm());

        setWidget(mainPanel);
    }

    private Panel createTitleBar() {
        HorizontalPanel titleBar = new HorizontalPanel();
        titleBar.setStyleName("dialog-title-bar");
        titleBar.setWidth("100%");

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyleName("dialog-button");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        HTML title = new HTML("<div class='dialog-title'><span class='status-indicator'></span>Racer Info</div>");

        Button doneButton = new Button("Done");
        doneButton.setStyleName("dialog-button");
        doneButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                saveAndClose();
            }
        });

        titleBar.add(cancelButton);
        titleBar.add(title);
        titleBar.add(doneButton);

        titleBar.setCellWidth(cancelButton, "100px");
        titleBar.setCellWidth(doneButton, "100px");
        titleBar.setCellHorizontalAlignment(title, HasHorizontalAlignment.ALIGN_CENTER);

        return titleBar;
    }

    private Panel createForm() {
        VerticalPanel form = new VerticalPanel();
        form.setStyleName("racer-form");
        form.setWidth("100%");
        form.setSpacing(15);

        // Name
        form.add(createFormRow("Name:", createNameField()));

        // Category
        form.add(createFormRow("Category:", createCategoryField()));

        // Bib number
        form.add(createFormRow("Bib number:", createBibNumberField()));

        // Team name (optional)
        form.add(createFormRow("Team name:", createTeamNameField(), true));

        // Team name 2 (optional)
        form.add(createFormRow("Team name 2:", createTeamName2Field(), true));

        // Age (optional)
        form.add(createFormRow("Age:", createAgeField(), true));

        // Gender (optional)
        form.add(createFormRow("Gender:", createGenderField(), true));

        return form;
    }

    private Panel createFormRow(String labelText, Widget field) {
        return createFormRow(labelText, field, false);
    }

    private Panel createFormRow(String labelText, Widget field, boolean optional) {
        HorizontalPanel row = new HorizontalPanel();
        row.setStyleName("form-row");
        row.setWidth("100%");
        row.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        Label label = new Label(labelText);
        label.setStyleName("form-label");
        if (optional) {
            label.setHTML(labelText + "<br><span class='optional-label'>(optional)</span>");
        }

        row.add(label);
        row.add(field);

        row.setCellWidth(label, "180px");

        return row;
    }

    private Widget createNameField() {
        nameField = new TextBox();
        nameField.setStyleName("form-field");
        nameField.setWidth("350px");
        nameField.setValue(crew.getName() != null ? crew.getName() : "");
        return nameField;
    }

    private Widget createCategoryField() {
        categoryField = new ListBox();
        categoryField.setStyleName("form-field");
        categoryField.setWidth("350px");

        categoryField.addItem("Select category");
        categoryField.addItem("Varsity Girls");
        categoryField.addItem("JV Girls");
        categoryField.addItem("Intermediate Girls");
        categoryField.addItem("Beginner Girls");
        categoryField.addItem("Varsity Boys");
        categoryField.addItem("JV Boys");
        categoryField.addItem("Intermediate Boys");
        categoryField.addItem("Beginner Boys");

        if (crew.getCategory() != null) {
            for (int i = 0; i < categoryField.getItemCount(); i++) {
                if (categoryField.getItemText(i).equals(crew.getCategory())) {
                    categoryField.setSelectedIndex(i);
                    break;
                }
            }
        }

        return categoryField;
    }

    private Widget createBibNumberField() {
        bibNumberField = new ListBox();
        bibNumberField.setStyleName("form-field");
        bibNumberField.setWidth("350px");

        bibNumberField.addItem("Select bib number");
        for (int i = 1; i <= 200; i++) {
            bibNumberField.addItem(String.valueOf(i));
        }

        bibNumberField.setSelectedIndex(crew.getBibNumber());

        return bibNumberField;
    }

    private Widget createTeamNameField() {
        teamNameField = new TextBox();
        teamNameField.setStyleName("form-field");
        teamNameField.setWidth("350px");
        teamNameField.setValue(crew.getTeamName() != null ? crew.getTeamName() : "");
        return teamNameField;
    }

    private Widget createTeamName2Field() {
        teamName2Field = new TextBox();
        teamName2Field.setStyleName("form-field");
        teamName2Field.setWidth("350px");
        teamName2Field.setValue(crew.getTeamName2() != null ? crew.getTeamName2() : "");
        return teamName2Field;
    }

    private Widget createAgeField() {
        ageField = new ListBox();
        ageField.setStyleName("form-field");
        ageField.setWidth("350px");

        ageField.addItem("Select age");
        for (int i = 10; i <= 20; i++) {
            ageField.addItem(String.valueOf(i));
        }

        if (crew.getAge() != null) {
            for (int i = 0; i < ageField.getItemCount(); i++) {
                if (ageField.getItemText(i).equals(crew.getAge())) {
                    ageField.setSelectedIndex(i);
                    break;
                }
            }
        }

        return ageField;
    }

    private Widget createGenderField() {
        genderField = new ListBox();
        genderField.setStyleName("form-field");
        genderField.setWidth("350px");

        genderField.addItem("Select gender");
        genderField.addItem("Female");
        genderField.addItem("Male");
        genderField.addItem("Non-binary");

        if (crew.getGender() != null) {
            for (int i = 0; i < genderField.getItemCount(); i++) {
                if (genderField.getItemText(i).equals(crew.getGender())) {
                    genderField.setSelectedIndex(i);
                    break;
                }
            }
        }

        return genderField;
    }

    private void saveAndClose() {
        // Update crew object
        crew.setName(nameField.getValue());

        if (categoryField.getSelectedIndex() > 0) {
            crew.setCategory(categoryField.getSelectedValue());
        }

        if (bibNumberField.getSelectedIndex() > 0) {
            crew.setBibNumber(Integer.parseInt(bibNumberField.getSelectedValue()));
        }

        if (!teamNameField.getValue().isEmpty()) {
            crew.setTeamName(teamNameField.getValue());
        }

        if (!teamName2Field.getValue().isEmpty()) {
            crew.setTeamName2(teamName2Field.getValue());
        }

        if (ageField.getSelectedIndex() > 0) {
            crew.setAge(ageField.getSelectedValue());
        }

        if (genderField.getSelectedIndex() > 0) {
            crew.setGender(genderField.getSelectedValue());
        }

        // Call save callback
        saveCallback.onSave(crew);

        // Close dialog
        hide();
    }
}
