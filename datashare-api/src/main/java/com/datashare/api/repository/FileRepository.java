package com.datashare.api.repository;

import com.datashare.api.entities.File;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
/** JPA repository for {@link com.datashare.api.entities.File} */
public interface FileRepository extends JpaRepository<File, Long> {

  /** Get files by userId */
  List<File> findByUserId(Long userId);

  /** Get files by userId with token */
  @Query("SELECT f FROM FileEntity f LEFT JOIN FETCH f.token WHERE f.userId = :userId")
  List<File> findByUserIdWithToken(Long userId);
}
