package comp.hacktx.backend.repositories;

import comp.hacktx.backend.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

    User findByUsername(String username);

    boolean existsByUsername(String username);
}
