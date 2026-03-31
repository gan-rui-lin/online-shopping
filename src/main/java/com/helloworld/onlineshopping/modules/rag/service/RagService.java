package com.helloworld.onlineshopping.modules.rag.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.rag.dto.RagAskDTO;
import com.helloworld.onlineshopping.modules.rag.entity.*;
import com.helloworld.onlineshopping.modules.rag.mapper.*;
import com.helloworld.onlineshopping.modules.rag.vo.ChatMessageVO;
import com.helloworld.onlineshopping.modules.rag.vo.RagAnswerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
        List<Long> spuIds = resolveSpuIds(dto);
        List<String> snapshots = new ArrayList<>();
        for (Long spuId : spuIds) {
            String snapshot = knowledgeService.buildProductSnapshot(spuId);
            if (!snapshot.isBlank()) {
                snapshots.add("[SPU " + spuId + "]\n" + snapshot);
            }
        }
        List<ProductKnowledgeDocEntity> docs = new ArrayList<>();
        for (Long spuId : spuIds) {
            List<ProductKnowledgeDocEntity> perSpu = knowledgeService.searchRelevant(spuId, dto.getQuestion(), 4);
            if (perSpu.isEmpty()) {
                knowledgeService.importFromProduct(spuId);
                perSpu = knowledgeService.searchRelevant(spuId, dto.getQuestion(), 4);
            }
            docs.addAll(perSpu);
        }

        if (docs.size() > 12) {
            docs = docs.subList(0, 12);
        }

        String docContext = docs.stream()
            .map(d -> "[SPU " + d.getSpuId() + "] " + d.getTitle() + ": " + clip(d.getContent(), 260))
            .collect(Collectors.joining("\n"));

        String systemPrompt = "You are a professional e-commerce assistant.\n"
            + "Rules:\n"
            + "1) Use the provided product context as grounding, but you may also use your general knowledge to answer compatibility or usage questions.\n"
            + "2) If a statement is based on general knowledge rather than the product context, label it as \"补充\".\n"
            + "3) Keep the answer concise and practical; avoid empty generic statements.\n"
            + "4) If multiple products are selected, compare by key dimensions (performance, scenario fit, price/value, risks).\n"
            + "5) Each product must include at least one concrete fact from the snapshot (price/spec/detail).\n"
            + "6) Output in Chinese Markdown with short bullets; add a brief recommendation at the end.\n\n"
            + "Selected SPU IDs: " + spuIds + "\n"
            + "Product snapshots:\n" + String.join("\n\n", snapshots) + "\n\n"
            + "Knowledge docs:\n" + docContext;
        String answer = aiClient.chat(systemPrompt, dto.getQuestion());

        Long sessionId = dto.getSessionId();
        if (sessionId == null) {
            AiChatSessionEntity session = new AiChatSessionEntity();
            session.setUserId(userId);
            session.setSessionType("RAG");
            session.setSpuId(spuIds.get(0));
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

    private List<Long> resolveSpuIds(RagAskDTO dto) {
        LinkedHashSet<Long> deduplicated = new LinkedHashSet<>();
        if (dto.getSpuIds() != null) {
            dto.getSpuIds().stream().filter(id -> id != null && id > 0).forEach(deduplicated::add);
        }
        if (dto.getSpuId() != null && dto.getSpuId() > 0) {
            deduplicated.add(dto.getSpuId());
        }
        if (deduplicated.isEmpty()) {
            throw new BusinessException("At least one product is required");
        }
        return new ArrayList<>(deduplicated);
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

    private String clip(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text == null ? "" : text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
