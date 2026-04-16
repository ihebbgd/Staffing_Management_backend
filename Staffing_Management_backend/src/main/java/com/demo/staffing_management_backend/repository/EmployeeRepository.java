package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends MongoRepository<Employee, String> {
    boolean existsByEmail(String email);
    List<Employee> findByActiveTrue();
    Optional<Employee> findByUserId(String userId);
    List<Employee> findByUserIdIn(Collection<String> userIds);

    // Case-insensitive substring search across name, email, department and job title.
    @Query("{ $or: [ "
            + "{ 'firstName': { $regex: ?0, $options: 'i' } }, "
            + "{ 'lastName': { $regex: ?0, $options: 'i' } }, "
            + "{ 'email': { $regex: ?0, $options: 'i' } }, "
            + "{ 'department': { $regex: ?0, $options: 'i' } }, "
            + "{ 'jobTitle': { $regex: ?0, $options: 'i' } } ] }")
    Page<Employee> search(String term, Pageable pageable);
}
