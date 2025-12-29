package com.racingtimer.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.racingtimer.client.storage.LocalTimingStorage;
import com.racingtimer.client.ui.RaceFinishView;
import com.racingtimer.service.TimingService;

/**
 * Entry point for the GWT Race Timer Web application.
 */
public class RaceTimerWeb implements EntryPoint {

    @Override
    public void onModuleLoad() {
        // Initialize storage and services
        LocalTimingStorage storage = new LocalTimingStorage();
        TimingService timingService = new TimingService(storage);

        // Create and attach the main view
        RaceFinishView raceFinishView = new RaceFinishView(timingService);

        // Remove loading message
        RootPanel loadingPanel = RootPanel.get("loading");
        if (loadingPanel != null) {
            loadingPanel.setVisible(false);
        }

        // Add view to the page
        RootPanel.get().add(raceFinishView);
    }
}
