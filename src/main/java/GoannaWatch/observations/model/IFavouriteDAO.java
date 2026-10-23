package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;

import java.util.Set;

/**
 * Interface for managing the current user's favourite observations.
 */
public interface IFavouriteDAO {

    /**
     * Checks whether the current user has favourited an observation.
     *
     * @param account account to check favourites of
     * @param observationId the ID of the observation
     * @return true if it is one of the current user's favourites
     * @throws SecurityException if nobody is logged in
     */
    boolean isFavourite(Account account, int observationId);

    /**
     * Adds or removes a favourite for the current user.
     *
     * @param account account that owns the favourite
     * @param observationId the ID of the observation
     * @param favourite true to add the favourite, false to remove it
     * @throws SecurityException if nobody is logged in
     */
    void setFavourite(Account account, int observationId, boolean favourite);

    /**
     * @param account the account whose favourites to load
     * @return the IDs of every observation the account has favourited
     */
    Set<Integer> getFavouriteObservationIds(Account account);
}