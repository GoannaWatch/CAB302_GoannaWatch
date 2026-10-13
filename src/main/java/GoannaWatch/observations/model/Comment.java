package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;

import java.util.InputMismatchException;

/**
 * A model class representing a comment on an observation.
 */
public class Comment {

    private int observationId;
    private Account author;
    private String text;

    /**
     * Constructs a comment with the specified observation, author, and text.
     *
     * @param observationId The ID of the observation.
     * @param author The account writing the comment.
     * @param text The comment text.
     */
    public Comment(int observationId, Account author, String text) {
        if (text == null || text.isBlank()) {
            throw new InputMismatchException("Comment cannot be blank.");
        }

        this.observationId = observationId;
        this.author = author;
        this.text = text;
    }

    /**
     * Returns the ID of the observation.
     *
     * @return The ID of the observation.
     */
    public int getObservationId() {
        return observationId;
    }

    /**
     * Returns the account that wrote the comment.
     *
     * @return The account that wrote the comment.
     */
    public Account getAuthor() {
        return author;
    }

    /**
     * Returns the comment text.
     *
     * @return The comment text.
     */
    public String getText() {
        return text;
    }
}