package GoannaWatch.observations.model;

/**
 * A real-world location. A name and its coords.
 */
public class Place {
    private final String name;
    private final double latitude;
    private final double longitude;

    /**
     * Constructs a new place.
     * @param name The name or address of the place
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     */
    public Place(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    /**
     * Returns the name of the place.
     * Allows for sensible display in lists/dropdowns without extra formatting.
     * @return the place's name
     */
    @Override
    public String toString() { return name; }
}