package com.dev.ultron.repository.web;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dev.ultron.domain.web.SuscripcionNewsletter;

@Repository
public interface SuscripcionNewsletterRepository extends JpaRepository<SuscripcionNewsletter, Long> {

    @Query("""
            SELECT s FROM SuscripcionNewsletter s
            WHERE LOWER(s.email) = LOWER(:email)
            """)
    Optional<SuscripcionNewsletter> findByEmailIgnoreCase(@Param("email") String email);
}
