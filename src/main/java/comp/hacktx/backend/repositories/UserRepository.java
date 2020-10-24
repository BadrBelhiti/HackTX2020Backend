package comp.hacktx.backend.repositories;

import comp.hacktx.backend.models.User;
import org.springframework.data.repository.Repository;


public interface UserRepository extends Repository<User, String> {

    User findByUsername(String username);

}
