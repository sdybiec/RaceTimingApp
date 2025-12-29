package com.racingtimer.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;

/**
 * Telephone-style keypad for entering bib numbers.
 */
public class KeypadPanel extends Composite {

    private final KeypadHandler handler;

    public KeypadPanel(KeypadHandler handler) {
        this.handler = handler;

        Grid keypad = new Grid(4, 3);
        keypad.setStyleName("keypad");

        // Row 1: 1, 2, 3
        keypad.setWidget(0, 0, createButton("1", 1));
        keypad.setWidget(0, 1, createButton("2", 2));
        keypad.setWidget(0, 2, createButton("3", 3));

        // Row 2: 4, 5, 6
        keypad.setWidget(1, 0, createButton("4", 4));
        keypad.setWidget(1, 1, createButton("5", 5));
        keypad.setWidget(1, 2, createButton("6", 6));

        // Row 3: 7, 8, 9
        keypad.setWidget(2, 0, createButton("7", 7));
        keypad.setWidget(2, 1, createButton("8", 8));
        keypad.setWidget(2, 2, createButton("9", 9));

        // Row 4: Backspace, No Bib, 0, Enter
        keypad.setWidget(3, 0, createBackspaceButton());
        keypad.setWidget(3, 1, createNoBibButton());
        keypad.setWidget(3, 2, createButton("0", 0));

        // We need to use a different layout for the last row
        // Let's create a separate grid for better control
        Grid lastRow = new Grid(1, 4);
        lastRow.setStyleName("keypad");
        lastRow.setWidget(0, 0, createBackspaceButton());
        lastRow.setWidget(0, 1, createNoBibButton());
        lastRow.setWidget(0, 2, createButton("0", 0));
        lastRow.setWidget(0, 3, createEnterButton());

        initWidget(keypad);
    }

    private Button createButton(String label, final int number) {
        Button button = new Button(label);
        button.setStyleName("keypad-button");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handler.onNumberPressed(number);
            }
        });
        return button;
    }

    private Button createBackspaceButton() {
        Button button = new Button("⌫");
        button.setStyleName("keypad-button special");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handler.onBackspace();
            }
        });
        return button;
    }

    private Button createNoBibButton() {
        Button button = new Button("No\nBib");
        button.setStyleName("keypad-button special");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handler.onNoBib();
            }
        });
        return button;
    }

    private Button createEnterButton() {
        Button button = new Button("Enter");
        button.setStyleName("keypad-button special");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handler.onEnter();
            }
        });
        return button;
    }
}

/**
 * Handler interface for keypad events.
 */
interface KeypadHandler {
    void onNumberPressed(int number);
    void onBackspace();
    void onNoBib();
    void onEnter();
}
