package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;

import java.time.LocalDate;
import java.util.InputMismatchException;

public class Observation {

    private int id;
    private Account observer;
    private String location;
    private Double latitude;
    private Double longitude;
    private String animalSeen;
    private String isEndangered;
    private LocalDate observedAt;


    /**
     * Constructs a new Observation.
     *
     * @param observer     The account recording the observation.
     * @param location     The display name/address of the location.
     * @param latitude     The latitude of the location, or null if unresolved.
     * @param longitude    The longitude of the location, or null if unresolved.
     * @param animalSeen   The animal observed.
     * @param isEndangered The endangerment status of the animal observed.
     * @param observedAt   The date of the observation.
     */
    public Observation(Account observer, String location, Double latitude, Double longitude,
                       String animalSeen, String isEndangered, LocalDate observedAt) {
        setObserver(observer);
        setLocationDetails(location, latitude, longitude);
        setAnimalSeen(animalSeen);
        setIsEndangered(isEndangered);
        setObservedAt(observedAt);
    }

    /**
     * Returns the ID of the observation.
     *
     * @return The ID of the observation.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the observation.
     *
     * @param id The ID to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the account associated with the observation.
     *
     * @return The account associated with the observation.
     */
    public Account getObserver() {
        return observer;
    }

    /**
     * Sets the account that recorded the observation.
     *
     * @param observer The account to be set.
     */
    public void setObserver(Account observer) {
        if (observer == null) {
            throw new InputMismatchException("Observation must be linked to an account.");
        }
        this.observer = observer;
    }

    /**
     * Returns the location that the animal was observed.
     *
     * @return The location that the animal was observed.
     */
    public String getLocation() {
        return location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    /**
     * @return true if observation has a resolved place and can be shown on the map
     */
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    public void setPlace(Place place) {
        if (place == null) {
            throw new InputMismatchException("Location cannot be blank.");
        }
        setLocationDetails(place.getName(), place.getLatitude(), place.getLongitude());
    }

    /**
     * Validates location. Location cannot be blank or out of bounds.
     * */
    private void setLocationDetails(String location, Double latitude, Double longitude) {
        if (location == null || location.isBlank()) {
            throw new InputMismatchException("Location cannot be blank.");
        }
        if ((latitude == null) != (longitude == null)) {
            throw new InputMismatchException("Latitude and longitude must both be set or both be empty.");
        }
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new InputMismatchException("Latitude must be between -90 and 90.");
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new InputMismatchException("Longitude must be between -180 and 180.");
        }
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    /**
     * Returns the animal observed.
     *
     * @return The animal observed.
     */
    public String getAnimalSeen() {
        return animalSeen;
    }

    /**
     * Sets the animal observed.
     *
     * @param animalSeen The animal to be set.
     */
    public void setAnimalSeen(String animalSeen) {
        if (animalSeen == null || animalSeen.isBlank()) {
            throw new InputMismatchException("Animal cannot be blank.");
        }
        this.animalSeen = animalSeen;
    }

    /**
     * Returns the animal endangerment status observed.
     *
     * @return The animal endangerment status observed.
     */
    public String getIsEndangered() {
        return isEndangered;
    }

    /**
     * Sets the animal endangerment status observed.
     *
     * @param isEndangered The animal endangerment status to be set.
     */
    public void setIsEndangered(String isEndangered) {
        if (isEndangered == null || isEndangered.isBlank()) {
            throw new InputMismatchException("Animal cannot be blank.");
        }
        this.isEndangered = isEndangered;
    }

    /**
     * Returns the date of the observation.
     *
     * @return The date of the observation.
     */
    public LocalDate getObservedAt() {
        return observedAt;
    }

    /**
     * Sets the date the record was observed.
     *
     * @param observedAt The date to be set.
     */
    public void setObservedAt(LocalDate observedAt) {
        if (observedAt == null) {
            throw new InputMismatchException("Observation date cannot be blank");
        }
        this.observedAt = observedAt;
    }
}
