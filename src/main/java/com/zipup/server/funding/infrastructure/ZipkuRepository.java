package com.zipup.server.funding.infrastructure;

import com.zipup.server.funding.domain.Zipku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ZipkuRepository extends JpaRepository<Zipku, UUID> {
}
