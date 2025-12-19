package com.yumyumcoach.domain.community.mapper;

import com.yumyumcoach.domain.community.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {
    Post findById(@Param("postId") Long postId);

    List<Post> findPosts(@Param("offset") int offset,
                         @Param("size") int size,
                         @Param("keyword") String keyword,
                         @Param("sort") String sort);

    long countPosts(@Param("keyword") String keyword);

    void insert(Post post);

    void update(Post post);

    void delete(@Param("postId") Long postId);

    void increaseLikes(@Param("postId") Long postId);

    void decreaseLikes(@Param("postId") Long postId);
}
