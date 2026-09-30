package GoannaWatch.account.model;

import org.apache.commons.validator.routines.EmailValidator;
import java.util.InputMismatchException;
import java.util.regex.Pattern;


/**
 * A model class representing an account with a first name, last name, email, and password.
 */
public class Account {

    // At least 8 chars, 1 digit, special, lower, and uppercase char, with no whitespace
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");


    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role = "user";

    /**
     * Constructs a new Account with the specified first name, last name, email, and password
     * @param firstName The first name of the contact.
     * @param lastName The last name of the contact.
     * @param email The email of the contact.
     * @param password The password of the contact.
     */
    public Account(String firstName, String lastName, String email, String password) {
        setEmail(email);
        setFirstName(firstName);
        setLastName(lastName);
        setPassword(password);
    }

    /**
     * Constructs an account without loading a password. Used when reading account details from the database.
     */
    public Account(String firstName, String lastName, String email) {
        setEmail(email);
        setFirstName(firstName);
        setLastName(lastName);
    }

    /**
     * Returns the ID of the account.
     * @return The ID of the account.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the account.
     * @param id The ID to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the first name of the account.
     * @return The first name of the account.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the account.
     * @param firstName The first name to set.
     */
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()){
            throw new InputMismatchException("First name cannot be blank.");
        }
        this.firstName = capitaliseName(firstName);
    }

    /**
     * Returns a capitalised version of the provided name.
     * @param entry The name to be capitalised.
     * @return The capitalised version of the name.
     */
    private String capitaliseName(String entry) {
        return entry.substring(0,1).toUpperCase() + entry.substring(1);
    }

    /**
     * Returns the last name of the account.
     * @return The last name of the account.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the account.
     * @param lastName The last name to set.
     */
    public void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank()){
            throw new InputMismatchException("Last name cannot be blank.");
        }
        this.lastName = capitaliseName(lastName);
    }

    /**
     * Returns the email of the account.
     * @return The email of the account.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the account.
     * @param email The email to be set.
     */
    public void setEmail(String email) {
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (email==null || !emailValidator.isValid(email)){
            throw new InputMismatchException("Email must be a valid email address.");
        }
        this.email = email;
    }

    /**
     * Returns the password of the account.
     * @return The password of the account.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Checks whether a password meets the required security requirements.
     *
     * @param password The password to validate.
     * @return true if the password meets all requirements, otherwise false.
     */
    public static boolean isPasswordValid(String password) {
        return password != null && !password.isBlank() && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Sets the password of the account.
     * @param password The password of the account. Must include at least 8 characters, an uppercase and lowercase character, a number, and a special character.
     */
    public void setPassword(String password) {
        if (!isPasswordValid(password)) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters and include an uppercase letter, a number, and a special character."
            );
        }

        this.password = password;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the account's role.
     *
     * @return user or expert
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role held by this account object.
     *
     * @param role user or expert
     * @throws IllegalArgumentException if the role is not supported
     */
    public void setRole(String role) {
        if (!"user".equals(role) && !"expert".equals(role)) {
            throw new IllegalArgumentException("Invalid account role.");
        }

        this.role = role;
    }

    /**
     * Checks whether this account is an expert.
     *
     * @return true if the role is expert
     */
    public boolean isExpert() {
        return "expert".equals(role);
    }
}

