package comp.hacktx.backend.repositories;


import comp.hacktx.backend.models.Report;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SymptomsRepository extends CrudRepository<Report, String> {

    List<Report> findAllByTimeBeforeAndTimeAfterAndZipcode(long end, long start, int zipcode);
}
