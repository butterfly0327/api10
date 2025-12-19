package com.yumyumcoach.domain.aiadvisor.mapper;

import com.yumyumcoach.domain.aiadvisor.entity.AiChatMessage;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiChatMessageMapper {
    void insert(@Param("message") AiChatMessage message);

    List<AiChatMessage> selectByEmailAndDate(
            @Param("email") String email,
            @Param("conversationDate") LocalDate conversationDate
    );
}
