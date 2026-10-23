package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Encapsulation of business logic and rules about editing, commenting etc.
 * Controllers call service layer rather than DAOs
 */
public class ObservationService {

    private final IObservationDAO observationDAO;
    private final ICommentDAO commentDAO;
    private final IFavouriteDAO favouriteDAO;

    /**
     * @param observationDAO observation database
     * @param commentDAO comments database
     * @param favouriteDAO favourites database
     */
    public ObservationService(IObservationDAO observationDAO, ICommentDAO commentDAO,
                              IFavouriteDAO favouriteDAO) {
        this.observationDAO = observationDAO;
        this.commentDAO = commentDAO;
        this.favouriteDAO = favouriteDAO;
    }

    /**
     * Returns all observations
     * @param viewer the account viewing the observations
     * @return all observations, with favourites marked
     */
    public List<Observation> getAll (Account viewer){
        return markFavourites(observationDAO.getAllObservations(), viewer);
    }

    /**
     * Returns all observations made by the current account.
     * @param owner the account who created the observations
     * @return the owner's observations with favourites marked
     */
    public List<Observation> getMine (Account owner){
        return markFavourites(observationDAO.getObservationsByAccount(owner), owner);
    }

    /**
     * Saves a new observation
     * @param observation the observation to save
     * @param actor the account recording the observation
     */
    public void add (Observation observation, Account actor){
        if (actor == null || actor.getId() != observation.getObserver().getId()) {
            throw new SecurityException("You must be logged in to record an observation.");
        }
        observationDAO.addObservation(observation);
    }

    /**
     * Updates an existing observation.
     * Can only be performed by observation owner or expert.
     * @param observation observation to be updated
     * @param actor the account attempting to update the observation
     */
    public void update (Observation observation, Account actor){
        if (!observation.canBeModifiedBy(actor)) {
            throw new SecurityException("You can only edit your own observations.");
        }
        observationDAO.updateObservation(observation);
    }

    /**
     * Deletes an existing observation.
     * Can only be performed by observation owner or expert.
     * @param observation the observation to be deleted
     * @param actor the account attempting to delete the observation
     */
    public void delete (Observation observation, Account actor){
        if (!observation.canBeModifiedBy(actor)) {
            throw new SecurityException("You can only delete your own observations.");
        }
        observationDAO.deleteObservation(observation);
    }

    /**
     * Returns a list of distinct species recorded, sorted alphabetically.
     * @return List of distinct species recorded thus far.
     */
    public List<String> getKnownSpecies () {
        List<String> species = observationDAO.getAllObservations().stream()
                .map(Observation::getAnimalSeen)
                .filter(s -> !"Unknown".equalsIgnoreCase(s))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toCollection(ArrayList::new));
        species.add(0, "Unknown");
        return species;
    }

    /**
     * Adds or removes observation from the actor's favourites.
     * @param observation observation to be favourited
     * @param actor the current account attempting to favourite
     * @param favourite if true, the observation will be added to the actor's favourite
     */
    public void setFavourite (Observation observation, Account actor,boolean favourite){
        if (actor == null) {
            throw new SecurityException("Please log in first.");
        }
        favouriteDAO.setFavourite(actor, observation.getId(), favourite);
        observation.setIsFavourite(favourite);
    }

    /**
     * Returns the comments for a given observation.
     * @param observationId the ID of the observation
     * @return the comments of the observations. Oldest first.
     */
    public List<Comment> getComments ( int observationId){
        return commentDAO.getCommentsByObservation(observationId);
    }

    /**
     * Adds a comment to an observation.
     * Action limited to experts.
     * @param observationId observation for which a comment is left
     * @param author the account leaving a comment
     * @param text string containing the content of the comment
     */
    public void addComment ( int observationId, Account author, String text){
        if (author == null || !author.isExpert()) {
            throw new SecurityException("Only experts can add comments.");
        }
        commentDAO.addComment(new Comment(observationId, author, text));
    }

    /**
     * Sets the observation's favourite flag from the viewer's saved favourites.
     * @param observations the list of the viewer's observations
     * @param viewer the account viewing the observations
     * @return returns the list of observations
     */
    private List<Observation> markFavourites (List <Observation> observations, Account viewer){
        if (viewer == null) {
            return observations;
        }
        Set<Integer> favouriteIds = favouriteDAO.getFavouriteObservationIds(viewer);
        for (Observation o : observations) {
            o.setIsFavourite(favouriteIds.contains(o.getId()));
        }
        return observations;
    }

    /**
     * Returns the observations the viewer has favourited.
     * @param viewer the account whose favourites to return
     * @return the viewer's favourite observations
     */
    public List<Observation> getFavourites(Account viewer) {
        if (viewer == null) {
            return List.of();
        }
        return getAll(viewer).stream()
                .filter(Observation::getIsFavourite)
                .toList();
    }

    /**
     * Returns observations of endangered animals.
     * @param viewer the account viewing the list
     * @return the endangered observations
     */
    public List<Observation> getEndangered(Account viewer) {
        return getAll(viewer).stream()
                .filter(o -> "Yes".equalsIgnoreCase(o.getIsEndangered()))
                .toList();
    }

}
