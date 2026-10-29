package com.lexiscan.backend.repository;
import com.lexiscan.backend.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface DocumentRepository extends JpaRepository<Document,Long>{
 List<Document> findByUserOrderByCreatedAtDesc(User user);
 Optional<Document> findByIdAndUser(Long id,User user);
}
