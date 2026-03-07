package com.malgn.repository;

import com.malgn.domain.Contents;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ContentsRepository extends JpaRepository<Contents, Long>, ContentRepositoryCustom {

}
