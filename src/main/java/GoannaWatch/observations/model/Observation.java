package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;

import java.time.LocalDate;
import java.util.InputMismatchException;

public class Observation {

    private int id;
    private Account observer;
    private String location;
    private String animalSeen;
    private String isEndangered;
    private LocalDate observedAt;
    public Boolean isFavourite;


    /**
     * Constructs a new Observation with the specified account, location, animal, and date.
     *
     * @param observer   The account recording the observation.
     * @param location   The location of the observation.
     * @param animalSeen The animal observed.
     * @param isEndangered The endangerment status of the animal observed.
     * @param observedAt The date of the observation.
     * @param isFavourite The favourite status of an observation
     */
    public Observation(Account observer, String location, String animalSeen, String isEndangered, LocalDate observedAt, Boolean isFavourite) {
        setObserver(observer);
        setLocation(location);
        setAnimalSeen(animalSeen);
        setIsEndangered(isEndangered);
        setObservedAt(observedAt);
        setIsFavourite(isFavourite);
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

    /**
     * Sets the location that the animal was observed.
     *
     * @param location The location to be set.
     */
    public void setLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new InputMismatchException("Location cannot be blank.");
        }
        this.location = location;
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

    /**
     * Returns the favourite status observed.
     *
     * @return The favourite status observed.
     */
    public Boolean getIsFavourite() {
        return isFavourite;
    }

    /**
     * Sets the favourite status observed.
     *
     * @param isFavourite The favourite status to be set.
     */
    public void setIsFavourite(Boolean isFavourite) {
        if (isFavourite == null) {
            throw new InputMismatchException("Favourite status cannot be blank.");
        }
        this.isFavourite = isFavourite;
    }
}

