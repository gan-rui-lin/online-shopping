package com.helloworld.onlineshopping.modules.rag.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.rag.dto.RagAskDTO;
import com.helloworld.onlineshopping.modules.rag.entity.*;
import com.helloworld.onlineshopping.modules.rag.mapper.*;
import com.helloworld.onlineshopping.modules.rag.vo.ChatMessageVO;
import com.helloworld.onlineshopping.modules.rag.vo.RagAnswerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class RagService {
    private final KnowledgeService knowledgeService;
    private final AiClient aiClient;
    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;

    @Transactional
    public RagAnswerVO ask(RagAskDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<ProductKnowledgeDocEntity> docs = knowledgeService.searchRelevant(dto.getSpuId(), dto.getQuestion(), 5);
        String context = docs.stream().map(d -> d.getTitle() + ": " + d.getContent()).collect(Collectors.joining("\n"));
        String systemPrompt = "You are a helpful shopping assistant. Based on the following product information:\n" + context + "\n\nAnswer the customer's question accurately and helpfully.";
        String answer = aiClient.chat(systemPrompt, dto.getQuestion());

        Long sessionId = dto.getSessionId();
        if (sessionId == null) {
            AiChatSessionEntity session = new AiChatSessionEntity();
            session.setUserId(userId);
            session.setSessionType("RAG");
            session.setSpuId(dto.getSpuId());
            session.setTitle(dto.getQuestion().length() > 50 ? dto.getQuestion().substring(0, 50) : dto.getQuestion());
            session.setStatus(1);
            sessionMapper.insert(session);
            sessionId = session.getId();
        }
        saveMessage(sessionId, "user", dto.getQuestion());
        saveMessage(sessionId, "assistant", answer);

        RagAnswerVO vo = new RagAnswerVO();
        vo.setQuestion(dto.getQuestion());
        vo.setAnswer(answer);
        vo.setSessionId(sessionId);
        vo.setReferenceDocTitles(docs.stream().map(ProductKnowledgeDocEntity::getTitle).collect(Collectors.toList()));
        return vo;
    }

    public List<ChatMessageVO> getChatHistory(Long sessionId) {
        return messageMapper.selectList(new LambdaQueryWrapper<AiChatMessageEntity>()
            .eq(AiChatMessageEntity::getSessionId, sessionId).orderByAsc(AiChatMessageEntity::getCreateTime))
            .stream().map(m -> { ChatMessageVO v = new ChatMessageVO(); v.setRole(m.getRole()); v.setContent(m.getContent()); v.setCreateTime(m.getCreateTime()); return v; })
            .collect(Collectors.toList());
    }

    private void saveMessage(Long sessionId, String role, String content) {
        AiChatMessageEntity msg = new AiChatMessageEntity();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        messageMapper.insert(msg);
    }
}
