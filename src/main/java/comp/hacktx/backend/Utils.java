package comp.hacktx.backend;

import comp.hacktx.backend.models.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

public class Utils {

    public static boolean validateUsername(String username) {
        // TODO: Validate
        return true;
    }

    public static boolean validatePassword(String password) {
        // TODO: Validate
        return true;
    }

    public static String buildToken(User user, String key) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .signWith(Keys.hmacShaKeyFor(key.getBytes())).compact();
    }

    public static boolean verifyToken(User user, String token) {
        // TODO: Validate token
        // https://github.com/jwtk/jjwt#jws-read
        return false;
    }

    public static boolean isInteger(String str) {
        return str.matches("\\d+");
    }

}
