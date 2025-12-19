package com.yumyumcoach.domain.ai.mapper;

import com.yumyumcoach.domain.ai.entity.AiChatMessage;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiChatMessageMapper {
    void insert(AiChatMessage message);

    List<AiChatMessage> findByEmailAndDate(
            @Param("email") String email,
            @Param("conversationDate") LocalDate conversationDate
    );
}
