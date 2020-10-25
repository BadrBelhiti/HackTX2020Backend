package comp.hacktx.backend;

import comp.hacktx.backend.models.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class Utils {

    /**
     * A username is valid if it's at least 4 characters long and only contains alphanumeric characters.
     * @param username Can be _any_ String
     * @return True if the username meets username guidelines, false otherwise.
     */
    public static boolean validateUsername(String username) {
        return username.matches("\\w+") && username.length() >= 4;
    }

    /**
     * A password is valid if it is at 4 characters long and only contains characters in the ASCII range [32, 126].
     * @param password Can be _any_ String
     * @return True if the password meets password guidelines, false otherwise.
     */
    public static boolean validatePassword(String password) {
        // Check length
        if (password.length() < 4) {
            return false;
        }

        // Check each character
        for (int i = 0; i < password.length(); i++){
            char c = password.charAt(i);
            if (c < 32 || c > 126) {
                return false;
            }
        }

        return true;
    }

    /**
     * @param user Username to build token for
     * @param key Secret key to sign
     * @return String representation of generated JWT
     */
    public static String buildToken(User user, String key) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .signWith(Keys.hmacShaKeyFor(key.getBytes())).compact();
    }

    /**
     * @param username Username to verify with
     * @param token JSON web token as String
     * @param key Secret key
     * @return True if the username and token combination are verified, false otherwise.
     */
    public static boolean verifyToken(String username, String token, String key) {
        try {
            Jwts.parserBuilder()
                    .requireSubject(username)
                    .setSigningKey(key.getBytes())
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * @param str A String suspected to represent a non-negative integer.
     * @return True if the String can be parsed into a non-negative integer, false otherwise.
     */
    public static boolean isInteger(String str) {
        return str.matches("\\d+");
    }

}
