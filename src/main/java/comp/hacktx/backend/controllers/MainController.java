package comp.hacktx.backend.controllers;

import comp.hacktx.backend.models.Credentials;
import comp.hacktx.backend.repositories.SymptomsRepository;
import comp.hacktx.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MainController {

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
        return new ResponseEntity<>("TODO", HttpStatus.OK);
    }

}
