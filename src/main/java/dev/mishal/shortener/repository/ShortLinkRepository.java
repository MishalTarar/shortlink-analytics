package dev.mishal.shortener.repository;

import dev.mishal.shortener.domain.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    Optional<ShortLink> findByCode(String code);

    boolean existsByCode(String code);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ShortLink link " +
            "set link.clickCount = link.clickCount + 1, link.lastClickedAt = :clickedAt " +
            "where link.code = :code")
    int recordClick(@Param("code") String code, @Param("clickedAt") Instant clickedAt);
}
