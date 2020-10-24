package comp.hacktx.backend.controllers;

import comp.hacktx.backend.Utils;
import comp.hacktx.backend.models.Credentials;
import comp.hacktx.backend.models.Report;
import comp.hacktx.backend.models.User;
import comp.hacktx.backend.repositories.SymptomsRepository;
import comp.hacktx.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
