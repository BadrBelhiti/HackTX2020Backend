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

    private UserRepository userRepository;
    private SymptomsRepository symptomsRepository;

    @Autowired
    public MainController(UserRepository userRepository, SymptomsRepository symptomsRepository) {
        this.userRepository = userRepository;
        this.symptomsRepository = symptomsRepository;
    }

    @PostMapping("/test")
    public ResponseEntity<?> postTest(@RequestBody Map<String, Object> data) {
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/test/{data}")
    public ResponseEntity<?> getTest(@PathVariable String data) {
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Credentials credentials) {
        // Validate username
        if (!Utils.validateUsername(credentials.getUsername())) {
            return new ResponseEntity<>(Collections.singletonMap("error", "invalid username"), HttpStatus.BAD_REQUEST);
        }

        // Validate password
        if (!Utils.validatePassword(credentials.getPassword())) {
            return new ResponseEntity<>(Collections.singletonMap("error", "invalid password"), HttpStatus.BAD_REQUEST);
        }

        // Check if user exists
        if (userRepository.existsByUsername(credentials.getUsername())) {
            return new ResponseEntity<>(Collections.singletonMap("error", "User already exists"), HttpStatus.BAD_REQUEST);
        }

        // Create and add user
        String id = Long.toString(System.nanoTime());
        String username = credentials.getUsername();
        String hashedPassword = BCrypt.hashpw(credentials.getPassword(), BCrypt.gensalt(10));
        User user = new User(id, username, hashedPassword, 0, 0, 0);
        userRepository.save(user);

        // Generate auth token
        String token = Utils.buildToken(user, key);

        return new ResponseEntity<>(Collections.singletonMap("token", token), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credentials credentials) {
        if (!userRepository.existsByUsername(credentials.getUsername())) {
            return new ResponseEntity<>(Collections.singletonMap("error", "invalid credentials"), HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUsername(credentials.getUsername());

        if (!BCrypt.checkpw(credentials.getPassword(), user.getPassword())) {
            return new ResponseEntity<>(Collections.singletonMap("error", "invalid credentials"), HttpStatus.BAD_REQUEST);
        }

        // Generate auth token
        String token = Utils.buildToken(user, key);

        return new ResponseEntity<>(Collections.singletonMap("token", token), HttpStatus.OK);
    }

    @PostMapping("/report")
    public ResponseEntity<?> report(@RequestHeader Map<String, String> headers, @RequestBody Map<String, String> data) {
        if (!headers.containsKey("authorization")) {
            return new ResponseEntity<>(Collections.singletonMap("error", "This call requires auth"), HttpStatus.UNAUTHORIZED);
        }

        if (!data.containsKey("username") || !data.containsKey("time") || !data.containsKey("zipcode") || !data.containsKey("symptoms")) {
            return new ResponseEntity<>(Collections.singletonMap("error", "malformed data"), HttpStatus.BAD_REQUEST);
        }

        String authHeader = headers.get("authorization");

        // Header in form {"authorization": "Bearer a.b.c"}
        String token = authHeader.substring(7);

        String username = data.get("username");
        String time = data.get("time");
        String zipcode = data.get("zipcode");
        String symptoms = data.get("symptoms");

        if (!Utils.verifyToken(username, token)) {
            return new ResponseEntity<>(Collections.singletonMap("error", "Bad token"), HttpStatus.BAD_REQUEST);
        }

        if (!Utils.isInteger(time) || !Utils.isInteger(zipcode) || !Utils.isInteger(symptoms)) {
            return new ResponseEntity<>(Collections.singletonMap("error", "malformed data"), HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUsername(username);

        // Get time since user made last report
        long deltaTime = System.currentTimeMillis() - user.getLastReport();

        if (deltaTime < 24 * 60 * 60 * 1000) {
            String errorMessage = "Must wait 24 hours between reports. Last report: " + user.getLastReport();
            return new ResponseEntity<>(Collections.singletonMap("error", errorMessage), HttpStatus.BAD_REQUEST);
        }

        Report report = new Report(Long.toString(System.nanoTime()), Long.parseLong(time), Integer.parseInt(zipcode), Integer.parseInt(symptoms));
        symptomsRepository.save(report);

        final int pointsReward = 100;

        // Reward user
        user.givePoints(pointsReward);

        if (System.currentTimeMillis() - user.getLastReport() <= 2 * 24 * 60 * 60 * 1000) {
            user.incrementStreak();
        } else {
            user.resetStreak();
        }

        userRepository.save(user);

        return new ResponseEntity<>(Collections.singletonMap("success", "report counted"), HttpStatus.OK);
    }

    @GetMapping("/records/{zipcode}/{start}/{end}")
    public ResponseEntity<?> getReports(@PathVariable String zipcode, @PathVariable String start, @PathVariable String end) {
        // Validate arguments
        if (!Utils.isInteger(zipcode) || !Utils.isInteger(start) || !Utils.isInteger(end)) {
            return new ResponseEntity<>(Collections.singletonMap("error", "malformed arg"), HttpStatus.BAD_REQUEST);
        }

        List<Report> reports =
                symptomsRepository.findAllByTimeBeforeAndTimeAfterAndZipcode(Long.parseLong(end), Long.parseLong(start), Integer.parseInt(zipcode));

        return new ResponseEntity<>(Collections.singletonMap("reports", reports), HttpStatus.OK);
    }

}
