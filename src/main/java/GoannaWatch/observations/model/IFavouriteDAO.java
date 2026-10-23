package GoannaWatch.observations.model;

/**
 * Interface for managing the current user's favourite observations.
 */
public interface IFavouriteDAO {

    /**
     * Checks whether the current user has favourited an observation.
     *
     * @param observationId the ID of the observation
     * @return true if it is one of the current user's favourites
     * @throws SecurityException if nobody is logged in
     */
    boolean isFavourite(int observationId);

    /**
     * Adds or removes a favourite for the current user.
     *
     * @param observationId the ID of the observation
     * @param favourite true to add the favourite, false to remove it
     * @throws SecurityException if nobody is logged in
     */
    void setFavourite(int observationId, boolean favourite);
}