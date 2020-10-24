package comp.hacktx.backend;

import comp.hacktx.backend.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.HashSet;
import java.util.Set;

public class Utils {

    /**
     * @param username Can be _any_ String
     * @return True if the username meets username guidelines, false otherwise.
     */
    public static boolean validateUsername(String username) {
        // TODO: Validate
        return true;
    }

    /**
     * @param password Can be _any_ String
     * @return True if the password meets password guidelines, false otherwise.
     */
    public static boolean validatePassword(String password) {
        // TODO: Validate
        return true;
    }

    public static String buildToken(User user, String key) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .signWith(Keys.hmacShaKeyFor(key.getBytes())).compact();
    }

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

    public static boolean isInteger(String str) {
        return str.matches("\\d+");
    }

    private static final String[] SYMPTOMS = new String[]
            {"Fever", "Coughing", "Sneezing", "Fatigue", "Shortness of breath",
                    "Chest pain", "Muscle aches", "sore throat"};

    public static Set<String> getSymptomsList(int symptoms) {
        Set<String> result = new HashSet<>();

        int pos = 0;
        while (symptoms > 0) {
            if (symptoms % 2 == 1) {
                result.add(SYMPTOMS[pos]);
            }
            symptoms /= 2;
        }

        return result;
    }

    public static int getSymptomsMask(Set<String> symptoms) {
        int result = 0;

        for (String symptom : SYMPTOMS) {
            if (symptoms.contains(symptom)) {
                result++;
            }
            result *= 2;
        }

        return result;
    }

}
