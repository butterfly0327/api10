package com.yumyumcoach.domain.community.mapper;

import com.yumyumcoach.domain.community.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostCommentMapper {

    List<PostComment> findByPostId(@Param("postId") Long postId);

    long countByPostId(@Param("postId") Long postId);

    PostComment findById(@Param("commentId") Long commentId);

    void insert(PostComment comment);

    void update(PostComment comment);

    void delete(@Param("commentId") Long commentId);

    void deleteByPostId(@Param("postId") Long postId);

    PostComment findByIdAndPostId(Long commentId, Long postId);
}
