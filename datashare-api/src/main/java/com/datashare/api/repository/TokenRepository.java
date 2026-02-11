package com.datashare.api.repository;

import com.datashare.api.entities.Token;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/** JPA repository for {@link com.datashare.api.entities.Token} */
public interface TokenRepository extends JpaRepository<Token, Long> {

  Optional<Token> findByTokenString(String tokenString);
}
