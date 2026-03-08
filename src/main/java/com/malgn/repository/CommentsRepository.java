package com.malgn.repository;

import com.malgn.domain.Comments;
import com.malgn.domain.Contents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
}
