package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.PasswordCallback;

/**
 * A simple user password handler that creates a password
 * from the user id and a simple suffix.
 */
public class DefaultPasswordCallback implements PasswordCallback {

    private final char[] password;

    /**
     * Creates a new user password handler from a given user id
     * and a given suffix.
     *
     * @param userId the user id.
     * @param suffix the user suffix.
     */
    public DefaultPasswordCallback(final String userId, final String suffix) {
        this.password = userId.concat(suffix).toCharArray();
    }

    @Override
    public char[] getPassword() {
        return password;
    }
}
