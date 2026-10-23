package GoannaWatch.observations.model;

import java.util.List;

/**
 * Interface for storing and retrieving comments on observations.
 */
public interface ICommentDAO {
    /**
     * Saves a comment written by the current expert.
     *
     * @param comment The comment to save.
     * @throws SecurityException If the current account is not an expert.
     */
    void addComment(Comment comment);

    /**
     * Returns the comments for an observation.
     *
     * @param observationId The ID of the observation.
     * @return The comments for the observation.
     */
    List<Comment> getCommentsByObservation(int observationId);
}
