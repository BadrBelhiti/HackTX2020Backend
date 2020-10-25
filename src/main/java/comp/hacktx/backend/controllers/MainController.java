package comp.hacktx.backend.controllers;

import comp.hacktx.backend.Utils;
import comp.hacktx.backend.models.Credentials;
import comp.hacktx.backend.models.Report;
import comp.hacktx.backend.models.User;
import comp.hacktx.backend.repositories.SymptomsRepository;
import comp.hacktx.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MainController {

    @Value("${security.key}")
    private String key;

    private final UserRepository userRepository;
    private final SymptomsRepository symptomsRepository;

    @Autowired
    public MainController(UserRepository userRepository, SymptomsRepository symptomsRepository) {
        this.userRepository = userRepository;
        this.symptomsRepository = symptomsRepository;
    }

    /**
     * @param credentials Object encompassing username and password
     * @return ResponseEntity with auth token on success, error message on failure.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Credentials credentials) {
        // Ensure credentials are complete
        if (!credentials.assertNonNull()) {
            return error("invalid credentials", HttpStatus.BAD_REQUEST);
        }

        // Validate username
        if (!Utils.validateUsername(credentials.getUsername())) {
            return error("invalid username", HttpStatus.BAD_REQUEST);
        }

        // Validate password
        if (!Utils.validatePassword(credentials.getPassword())) {
            return error("invalid password", HttpStatus.BAD_REQUEST);
        }

        // Check if user exists
        if (userRepository.existsByUsername(credentials.getUsername())) {
            return error("user already exists", HttpStatus.BAD_REQUEST);
        }

        // Create and add user
        String id = Long.toString(System.nanoTime());
        String username = credentials.getUsername();
        String hashedPassword = BCrypt.hashpw(credentials.getPassword(), BCrypt.gensalt(10));
        User user = new User(id, username, hashedPassword, 0, 0, 0);
        userRepository.save(user);

        return simpleResponse("token", Utils.buildToken(user, key), HttpStatus.OK);
    }

    /**
     * @param credentials Object encompassing username and password
     * @return ResponseEntity with auth token on success, error message on failure.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credentials credentials) {
        // Ensure credentials are complete
        if (!credentials.assertNonNull()) {
            return error("invalid credentials", HttpStatus.BAD_REQUEST);
        }

        // Verify user exists
        if (!userRepository.existsByUsername(credentials.getUsername())) {
            return error("invalid credentials", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUsername(credentials.getUsername());

        // Verify password
        if (!BCrypt.checkpw(credentials.getPassword(), user.getPassword())) {
            return error("invalid credentials", HttpStatus.BAD_REQUEST);
        }

        // Generate and return auth token upon success
        return simpleResponse("token", Utils.buildToken(user, key), HttpStatus.OK);
    }

    /**
     * @param headers All headers present in request
     * @param data    Entire body of request
     * @return ResponseEntity indicating failure or success of this request
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestHeader Map<String, String> headers, @RequestBody Map<String,
            String> data) {
        ResponseEntity<?> verificationResult = verifyToken(headers, data);
        if (verificationResult != null) {
            return verificationResult;
        }

        if (!data.containsKey("newpassword")) {
            return error("malformed data", HttpStatus.BAD_REQUEST);
        }

        String username = data.get("username");
        String newPassword = data.get("newpassword");

        if (!Utils.validatePassword(newPassword)) {
            return error("invalid password", HttpStatus.BAD_REQUEST);
        }

        // Update password
        User user = userRepository.findByUsername(username);
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
        user.updatePassword(hashedPassword);
        userRepository.save(user);

        return simpleResponse("success", "password updated", HttpStatus.OK);
    }

    /**
     * @param headers All headers present in request
     * @param data    Entire body of request
     * @return ResponseEntity indicating failure or success of this request
     */
    @PostMapping("/report")
    public ResponseEntity<?> report(@RequestHeader Map<String, String> headers, @RequestBody Map<String, String> data) {
        ResponseEntity<?> verificationResult = verifyToken(headers, data);
        if (verificationResult != null) {
            return verificationResult;
        }

        if (!data.containsKey("zipcode") || !data.containsKey("symptoms")) {
            return error("malformed data", HttpStatus.BAD_REQUEST);
        }

        String username = data.get("username");
        String zipcode = data.get("zipcode");
        String symptoms = data.get("symptoms");

        if (!Utils.isInteger(zipcode) || !Utils.isInteger(symptoms)) {
            return error("malformed data", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUsername(username);

        // Get time since user made last report
        long deltaTime = System.currentTimeMillis() - user.getLastReport();

        // If user already made a report within the last 24 hours, deny and return an error.
        if (deltaTime < 24 * 60 * 60 * 1000) {
            String errorMessage = "Must wait 24 hours between reports. Last report: " + user.getLastReport();
            return error(errorMessage, HttpStatus.BAD_REQUEST);
        }

        // Build and submit report
        Report report = new Report(
                Long.toString(System.nanoTime()),
                System.currentTimeMillis(),
                Integer.parseInt(zipcode),
                Integer.parseInt(symptoms));

        submitReport(user, report);

        return simpleResponse("success", "report counted", HttpStatus.OK);
    }

    /**
     * @param zipcode Zipcode to query for records
     * @param start   The beginning of the time window to query data from. Measured in milliseconds from epoch.
     * @param end     The ending of the time window to query data from. Measured in milliseconds from epoch.
     * @return A ResponseEntity containing all relevant records, or containing an error if one exists.
     */
    @GetMapping("/records/{zipcode}/{start}/{end}")
    public ResponseEntity<?> getReports(@PathVariable String zipcode, @PathVariable String start,
                                        @PathVariable String end) {
        // Validate arguments
        if (!Utils.isInteger(zipcode) || !Utils.isInteger(start) || !Utils.isInteger(end)) {
            return error("malformed arg", HttpStatus.BAD_REQUEST);
        }

        List<Report> reports =
                symptomsRepository.findAllByTimeBeforeAndTimeAfterAndZipcode(Long.parseLong(end),
                        Long.parseLong(start), Integer.parseInt(zipcode));

        return simpleResponse("reports", reports, HttpStatus.OK);
    }

    /**
     * @param username User's username to retrieve points for
     * @return ResponseEntity indicating number of points user has or an error on failure.
     */
    @GetMapping("/points/{username}")
    public ResponseEntity<?> getPoints(@PathVariable String username) {
        if (!userRepository.existsByUsername(username)) {
            return error("user doesn't exist", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUsername(username);

        return simpleResponse("points", user.getPoints(), HttpStatus.OK);
    }

    /**
     * @param username User's username to retrieve streak for
     * @return ResponseEntity indicating user's streak or an error on failure.
     */
    @GetMapping("/streak/{username}")
    public ResponseEntity<?> getStreak(@PathVariable String username) {
        if (!userRepository.existsByUsername(username)) {
            return error("user doesn't exist", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUsername(username);

        return simpleResponse("streak", user.getStreak(), HttpStatus.OK);
    }

    /**
     * @param headers All headers present in request
     * @param body    Full request body
     * @return ResponseEntity object if the verification fails and an error is generated.
     * If no error is found, then return null.
     */
    private ResponseEntity<?> verifyToken(Map<String, String> headers, Map<String, String> body) {
        // Check for presence of token
        if (!headers.containsKey("authorization")) {
            return error("This call requires auth", HttpStatus.UNAUTHORIZED);
        }

        // Check for presence of user identity
        if (!body.containsKey("username")) {
            return error("malformed data", HttpStatus.BAD_REQUEST);
        }

        // Extract token from header
        String token = headers.get("authorization").substring(7);

        // Verify JWT token
        if (!Utils.verifyToken(body.get("username"), token, key)) {
            return error("bad token", HttpStatus.BAD_REQUEST);
        }

        // No error to return
        return null;
    }

    /**
     * @param title  Key of singleton map
     * @param data   Value of singleton map
     * @param status HTTP status code to return
     * @return ResponseEntity representing a singleton response with the specified status code
     */
    private ResponseEntity<?> simpleResponse(String title, Object data, HttpStatus status) {
        return new ResponseEntity<>(Collections.singletonMap(title, data), status);
    }

    /**
     * @param error  Error message
     * @param status HTTP status to return
     * @return ResponseEntity representing error along with status code
     */
    private ResponseEntity<?> error(String error, HttpStatus status) {
        return simpleResponse("error", error, status);
    }

    /**
     * @param user   The user who is submitting the report
     * @param report The incoming symptoms report from the user
     */
    private void submitReport(User user, Report report) {
        // Save report to database
        symptomsRepository.save(report);

        final int pointsReward = 100;

        // Reward user
        user.givePoints(pointsReward);

        // If report was made less than 48 hours from previous report or is first report, then the user has submitted a
        // report on two consecutive days and therefore their streak is incremented. Otherwise, reset streak.
        if (user.getLastReport() == 0 || System.currentTimeMillis() - user.getLastReport() <= 2 * 24 * 60 * 60 * 1000) {
            user.incrementStreak();
        } else {
            user.resetStreak();
        }

        // Update user's last report submission time
        user.registerReport();
        userRepository.save(user);
    }

}
