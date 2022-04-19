package com.svcline.prodline;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.svcline.models.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProductionLine {
    private static final String SERVICE_ACCOUNT = "radinn-rindus-sandbox-27eca0521d8d.json";
    private static final String PROJECT_ID = "radinn-rindus-sandbox";

    private String startStationId;
    private String endStationId;
    private String serviceStationId;
    private HashMap<String, Station> stationMap;
    private HashMap<String, String> stationTransitionMap;

    private static Firestore firestore = null;
    private Props props;
    private ProductLineConfiguration productLineConfiguration;

    private boolean init = false;

    public ProductionLine() throws IOException {
        this.stationMap = null;
        this.startStationId = null;
        this.serviceStationId = null;
        this.endStationId = null;
        this.stationTransitionMap = null;
        initFirestore();
    }

    private void initCheck() throws InstantiationException {
        if (!this.init)
            throw new InstantiationException("Production line has not been done. Aborting execution.");
    }

    private boolean stationOrderCorrect() {
        for (Map.Entry<String, String> entry : stationTransitionMap.entrySet()) {
            if (!stationMap.containsKey(entry.getKey()) || !stationMap.containsKey(entry.getValue()))
                return false;
        }

        return true;
    }

    public ProductLineConfiguration getProductLineConfiguration() {
        return productLineConfiguration;
    }

    public void setProductLineConfiguration(ProductLineConfiguration productLineConfiguration) {
        this.productLineConfiguration = productLineConfiguration;
    }

    public void init(ProductLineConfiguration plc, Props props) throws InstantiationException, IOException {
        // We must have three stations
        if (plc == null)
            throw new InstantiationException("Initialization failed: Provided configuration cannot be null. Terminating execution.");
        this.productLineConfiguration = plc;

        StationMap stationMapInit = plc.getConfiguredStationMap();
        StationOrderMap stationOrderMapInit = plc.getConfiguredStationOrder();

        if (stationMapInit == null || stationMapInit.getStationMap().isEmpty())
            throw new InstantiationException("Initialization failed: Station map cannot be empty. Terminating execution.");
        this.stationMap = stationMapInit.getStationMap();

        if (stationOrderMapInit == null || stationOrderMapInit.getStationOrder().isEmpty())
            throw new InstantiationException("Initialization failed: Station order map cannot be empty. Terminating execution.");
        this.stationTransitionMap = stationOrderMapInit.getStationOrder();

        // It's now safe to check if the station order map consist of existing stations. If not, we exit.
        if (!stationOrderCorrect())
            throw new InstantiationException("Initialization failed. Station order map contains non-existant stations. Terminating execution.");

        // Check if stations are correctly defined, meaning START, END and SERVICE
        int start = 0;
        int end = 0;
        int service = 0;
        for (Station station : this.stationMap.values()) {
            if (station.getStationType() == StationType.START) {
                this.startStationId = station.getId();
                start++;
            } else if (station.getStationType() == StationType.END) {
                this.endStationId = station.getId();
                end++;
            } else if (station.getStationType() == StationType.SERVICE) {
                this.serviceStationId = station.getId();
                service++;
            }
        }

        // We must have three stations
        if (start == 0 || end == 0 || service == 0)
            throw new InstantiationException(
                    "Initialization failed: production line must have only one START, STOP and SERVICE station. Terminating execution.");

        this.props = props;
        this.init = true;
    }

    public String getStartStationId() {
        return startStationId;
    }

    public void setStartStationId(String startStationId) {
        this.startStationId = startStationId;
    }

    public String getEndStationId() {
        return endStationId;
    }

    public void setEndStationId(String endStationId) {
        this.endStationId = endStationId;
    }

    public String getServiceStationId() {
        return serviceStationId;
    }

    public void setServiceStationId(String servicetStationId) {
        this.serviceStationId = servicetStationId;
    }

    public Station getStation(String stationId) {
        if (stationId == null || stationId.isEmpty() || stationId.isBlank()) {
            return null;
        }

        return this.stationMap.get(stationId);
    }

    private String getNextStationId(String currentStationId) {
        return this.stationTransitionMap.get(currentStationId);
    }

    public LineItem startProduction(String id) throws InstantiationException {
        initCheck();

        return new LineItem(id, startStationId, null, State.START);
    }

    public HashMap<String, Station> getStationMap() {
        return stationMap;
    }

    public void setStationMap(HashMap<String, Station> stationMap) {
        this.stationMap = stationMap;
    }

    public HashMap<String, String> getStationTransitionMap() {
        return stationTransitionMap;
    }

    public void setStationTransitionMap(HashMap<String, String> stationTransitionMap) {
        this.stationTransitionMap = stationTransitionMap;
    }

    public Props getProps() {
        return props;
    }

    public void setProps(Props props) {
        this.props = props;
    }

    private void initFirestore() throws IOException {
        if (firestore == null) {
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(SERVICE_ACCOUNT);

            assert serviceAccount != null;
            FirestoreOptions firestoreOptions =
                    FirestoreOptions.getDefaultInstance().toBuilder()
                            .setProjectId(PROJECT_ID)
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();

            firestore = firestoreOptions.getService();
        }
    }

    public Firestore getFirestore() {
        return firestore;
    }

    public LineItem toNextStation(LineItem actualItem, LineItem currentLineItem) throws IllegalStateException, InstantiationException {
        initCheck();

        State newState = currentLineItem.getState();
        Station currentStation = getStation(currentLineItem.getCurrentStationId());

        if (!isInCorrectLineOrder(actualItem, currentLineItem)) {
            throw new IllegalStateException("This item is not at the correct production line station. Correct station is: " +
                                            getNextStationId(actualItem.currentStationId));
        } else if (actualItem.isScrapped()) {
            throw new IllegalStateException("Scrapped items cannot be processed");
        } else if (actualItem.isDone()) {
            throw new IllegalStateException("Finished items cannot be processed");
        } else if (!currentStation.allowedState(newState)) {
            throw new IllegalStateException("State " + newState + " not allowed for station. Allowed states are: " + currentStation.getAllowedStates());
        } else if (actualItem.isFailed() && !currentStation.isServiceStation()) {
            throw new IllegalStateException("This item should in the service station. This station is a " + currentStation.getStationType() + ".");
        }

        LineItem lineItem = new LineItem(currentLineItem);
        switch (newState) {
            case RETRY:
                lineItem.setPreviousStationId(actualItem.getCurrentStationId());
                lineItem.setCurrentStationId(actualItem.getPreviousStationId());
                lineItem.setState(newState);
                break;

            case PASS:
                if (currentStation.isEndStation()) {
                    lineItem.setState(State.DONE);
                    lineItem.setCurrentStationId(this.endStationId);
                } else if (currentStation.isServiceStation()) {
                    lineItem.setState(newState);
                    lineItem.setCurrentStationId(actualItem.getPreviousStationId());
                } else {
                    lineItem.setState(newState);
                    //lineItem.setCurrentStationId(...); We do not set current station ID since the one passed in is correct
                }

                lineItem.setPreviousStationId(actualItem.getCurrentStationId());

                break;

            case FAIL:
                lineItem.setPreviousStationId(currentLineItem.getCurrentStationId());
                lineItem.setCurrentStationId(this.serviceStationId);    // If we fail we go to the service station
                lineItem.setState(newState);
                break;

            case SCRAP:
                lineItem.setCurrentStationId(getServiceStationId());
                lineItem.setPreviousStationId(getServiceStationId());
                lineItem.setState(newState);
                break;

            case DONE:
                lineItem.setCurrentStationId(this.endStationId);
                lineItem.setPreviousStationId(currentLineItem.getCurrentStationId());
                lineItem.setState(newState);

            default:
                // Other states not handled
        }

        return lineItem;
    }

    private boolean isInCorrectLineOrder(LineItem actualLineItem, LineItem currentItem) {
        if (actualLineItem == null || currentItem == null || actualLineItem.getId() == null || currentItem.getCurrentStationId() == null)
            return false;
        else if (actualLineItem.isFailed() || actualLineItem.isScrapped())
            return true;    // We do not check failed or scrapped items.

        return currentItem.getCurrentStationId().equals(this.stationTransitionMap.get(actualLineItem.getCurrentStationId()));
    }
}
