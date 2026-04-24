package com.helloworld.onlineshopping.modules.rag.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.rag.dto.RagAskDTO;
import com.helloworld.onlineshopping.modules.rag.entity.AiChatMessageEntity;
import com.helloworld.onlineshopping.modules.rag.entity.AiChatSessionEntity;
import com.helloworld.onlineshopping.modules.rag.entity.ProductKnowledgeDocEntity;
import com.helloworld.onlineshopping.modules.rag.mapper.AiChatMessageMapper;
import com.helloworld.onlineshopping.modules.rag.mapper.AiChatSessionMapper;
import com.helloworld.onlineshopping.modules.rag.vo.ChatMessageVO;
import com.helloworld.onlineshopping.modules.rag.vo.ChatSessionVO;
import com.helloworld.onlineshopping.modules.rag.vo.RagAnswerVO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagService {
    private static final int DOC_LIMIT = 12;
    private static final int HISTORY_LIMIT = 6;

    private final KnowledgeService knowledgeService;
    private final AiClient aiClient;
    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final Cache<String, Object> ragAnswerCache;

    public RagService(
        KnowledgeService knowledgeService,
        AiClient aiClient,
        AiChatSessionMapper sessionMapper,
        AiChatMessageMapper messageMapper,
        @Qualifier("intelligenceRagCache") Cache<String, Object> ragAnswerCache
    ) {
        this.knowledgeService = knowledgeService;
        this.aiClient = aiClient;
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.ragAnswerCache = ragAnswerCache;
    }

    @Transactional
    public RagAnswerVO ask(RagAskDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<Long> spuIds = resolveSpuIds(dto);
        String askedQuestion = dto.getQuestion().trim();

        Long sessionId = ensureSession(dto.getSessionId(), userId, spuIds, askedQuestion);
        List<AiChatMessageEntity> recentMessages = loadRecentMessages(sessionId, HISTORY_LIMIT);
        String retrievalQuestion = buildRetrievalQuestion(askedQuestion, recentMessages);

        List<String> snapshots = new ArrayList<>();
        List<ProductKnowledgeDocEntity> docBuffer = new ArrayList<>();
        for (Long spuId : spuIds) {
            knowledgeService.ensureKnowledgeImported(spuId);
            String snapshot = knowledgeService.buildProductSnapshot(spuId);
            if (!snapshot.isBlank()) {
                snapshots.add("[SPU " + spuId + "]\n" + snapshot);
            }
            docBuffer.addAll(knowledgeService.searchRelevant(spuId, retrievalQuestion, 4));
        }

        List<ProductKnowledgeDocEntity> docs = deduplicateDocs(docBuffer);
        if (docs.size() > DOC_LIMIT) {
            docs = docs.subList(0, DOC_LIMIT);
        }

        List<String> referenceDocTitles = docs.stream()
            .map(ProductKnowledgeDocEntity::getTitle)
            .filter(title -> title != null && !title.isBlank())
            .distinct()
            .collect(Collectors.toList());

        String answer;
        if (snapshots.isEmpty() && docs.isEmpty()) {
            answer = emptyEvidenceAnswer(dto.getLocale());
        } else {
            String cacheKey = buildCacheKey(userId, spuIds, askedQuestion, dto.getLocale(), recentMessages);
            CachedRagAnswer cached = asCachedAnswer(ragAnswerCache.getIfPresent(cacheKey));
            if (cached != null) {
                answer = appendReferences(cached.answer(), cached.referenceDocTitles(), isEnglish(dto.getLocale()));
                referenceDocTitles = cached.referenceDocTitles();
            } else {
                String docContext = docs.stream()
                    .map(d -> "[SPU " + d.getSpuId() + "] " + d.getTitle() + ": " + clip(d.getContent(), 260))
                    .collect(Collectors.joining("\n"));

                String historyContext = buildHistoryContext(recentMessages);
                String systemPrompt;
                if (isEnglish(dto.getLocale())) {
                    systemPrompt = "You are a professional e-commerce assistant.\n"
                        + "Rules:\n"
                        + "1) Ground the answer on provided product context first.\n"
                        + "2) Mark non-product assumptions as \"Supplement\".\n"
                        + "3) Keep concise and practical. If multiple products are selected, compare key dimensions.\n"
                        + "4) If context is insufficient, clearly say what is missing.\n"
                        + "5) Output in English Markdown bullet points.\n\n"
                        + "Selected SPU IDs: " + spuIds + "\n"
                        + "Recent history:\n" + historyContext + "\n\n"
                        + "Product snapshots:\n" + String.join("\n\n", snapshots) + "\n\n"
                        + "Knowledge docs:\n" + docContext;
                } else {
                    systemPrompt = "你是专业的电商导购助手。\n"
                        + "规则：\n"
                        + "1) 优先依据商品上下文回答。\n"
                        + "2) 基于常识推断的内容标注为“补充”。\n"
                        + "3) 回答简洁可执行，多商品时做关键维度对比。\n"
                        + "4) 证据不足时明确说明缺失信息，禁止编造。\n"
                        + "5) 使用中文 Markdown 要点输出。\n\n"
                        + "已选 SPU: " + spuIds + "\n"
                        + "最近会话：\n" + historyContext + "\n\n"
                        + "商品快照：\n" + String.join("\n\n", snapshots) + "\n\n"
                        + "知识文档：\n" + docContext;
                }
                answer = aiClient.chat(systemPrompt, askedQuestion);
                ragAnswerCache.put(cacheKey, new CachedRagAnswer(answer, referenceDocTitles));
                answer = appendReferences(answer, referenceDocTitles, isEnglish(dto.getLocale()));
            }
        }

        saveMessage(sessionId, "user", askedQuestion);
        saveMessage(sessionId, "assistant", answer);
        touchSession(sessionId);

        RagAnswerVO vo = new RagAnswerVO();
        vo.setQuestion(askedQuestion);
        vo.setAnswer(answer);
        vo.setSessionId(sessionId);
        vo.setReferenceDocTitles(referenceDocTitles);
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

    private Long ensureSession(Long sessionId, Long userId, List<Long> spuIds, String question) {
        if (sessionId != null) {
            AiChatSessionEntity session = sessionMapper.selectById(sessionId);
            if (session == null || !userId.equals(session.getUserId())) {
                throw new BusinessException("No permission");
            }
            return sessionId;
        }

        AiChatSessionEntity session = new AiChatSessionEntity();
        session.setUserId(userId);
        session.setSessionType("RAG");
        session.setSpuId(spuIds.get(0));
        session.setTitle(question.length() > 50 ? question.substring(0, 50) : question);
        session.setStatus(1);
        sessionMapper.insert(session);
        return session.getId();
    }

    public List<ChatMessageVO> getChatHistory(Long sessionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        AiChatSessionEntity session = sessionMapper.selectById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new BusinessException("No permission");
        }
        return messageMapper.selectList(new LambdaQueryWrapper<AiChatMessageEntity>()
                .eq(AiChatMessageEntity::getSessionId, sessionId)
                .orderByAsc(AiChatMessageEntity::getCreateTime))
            .stream().map(m -> {
                ChatMessageVO v = new ChatMessageVO();
                v.setRole(m.getRole());
                v.setContent(m.getContent());
                v.setCreateTime(m.getCreateTime());
                return v;
            }).collect(Collectors.toList());
    }

    public List<ChatSessionVO> listMySessions() {
        Long userId = SecurityUtil.getCurrentUserId();
        return sessionMapper.selectList(new LambdaQueryWrapper<AiChatSessionEntity>()
                .eq(AiChatSessionEntity::getUserId, userId)
                .eq(AiChatSessionEntity::getSessionType, "RAG")
                .eq(AiChatSessionEntity::getStatus, 1)
                .orderByDesc(AiChatSessionEntity::getUpdateTime))
            .stream().map(session -> {
                ChatSessionVO vo = new ChatSessionVO();
                vo.setSessionId(session.getId());
                vo.setTitle(session.getTitle());
                vo.setSessionType(session.getSessionType());
                vo.setSpuId(session.getSpuId());
                vo.setUpdateTime(session.getUpdateTime());
                vo.setCreateTime(session.getCreateTime());
                return vo;
            }).collect(Collectors.toList());
    }

    private void saveMessage(Long sessionId, String role, String content) {
        AiChatMessageEntity msg = new AiChatMessageEntity();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        messageMapper.insert(msg);
    }

    private List<AiChatMessageEntity> loadRecentMessages(Long sessionId, int limit) {
        List<AiChatMessageEntity> messages = messageMapper.selectList(new LambdaQueryWrapper<AiChatMessageEntity>()
            .eq(AiChatMessageEntity::getSessionId, sessionId)
            .orderByDesc(AiChatMessageEntity::getCreateTime)
            .last("LIMIT " + Math.max(1, limit)));
        Collections.reverse(messages);
        return messages;
    }

    private String buildRetrievalQuestion(String question, List<AiChatMessageEntity> recentMessages) {
        if (!isContextDependentQuestion(question) || recentMessages.isEmpty()) {
            return question;
        }
        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            AiChatMessageEntity msg = recentMessages.get(i);
            if ("user".equalsIgnoreCase(msg.getRole()) && msg.getContent() != null && !msg.getContent().isBlank()) {
                return clip(msg.getContent(), 120) + " " + question;
            }
        }
        return question;
    }

    private boolean isContextDependentQuestion(String question) {
        if (question == null) {
            return false;
        }
        String normalized = question.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("这个") || normalized.contains("那个")
            || normalized.contains("它") || normalized.contains("这款")
            || normalized.contains("that") || normalized.contains("this")
            || normalized.contains("it");
    }

    private String buildHistoryContext(List<AiChatMessageEntity> recentMessages) {
        if (recentMessages == null || recentMessages.isEmpty()) {
            return "(none)";
        }
        return recentMessages.stream()
            .map(msg -> "[" + msg.getRole() + "] " + clip(msg.getContent(), 160))
            .collect(Collectors.joining("\n"));
    }

    private List<ProductKnowledgeDocEntity> deduplicateDocs(List<ProductKnowledgeDocEntity> docs) {
        Map<String, ProductKnowledgeDocEntity> unique = new LinkedHashMap<>();
        for (ProductKnowledgeDocEntity doc : docs) {
            String key = doc.getId() == null
                ? doc.getSpuId() + "|" + doc.getTitle() + "|" + clip(doc.getContent(), 40)
                : doc.getId().toString();
            unique.putIfAbsent(key, doc);
        }
        return new ArrayList<>(unique.values());
    }

    private String buildCacheKey(Long userId, List<Long> spuIds, String question, String locale, List<AiChatMessageEntity> recentMessages) {
        String recentDigest = recentMessages.stream()
            .filter(msg -> msg.getContent() != null)
            .map(msg -> msg.getRole() + ":" + clip(msg.getContent(), 24))
            .collect(Collectors.joining("|"));
        return "u=" + userId
            + "|spu=" + spuIds
            + "|locale=" + (locale == null ? "zh-CN" : locale)
            + "|q=" + question.trim().toLowerCase(Locale.ROOT)
            + "|ctx=" + recentDigest;
    }

    private String emptyEvidenceAnswer(String locale) {
        if (isEnglish(locale)) {
            return "I could not find enough product evidence for this question. "
                + "Please select products or ask with clearer model/spec details.";
        }
        return "当前未检索到足够的商品依据，请先选择商品或补充更明确的型号/规格信息后再提问。";
    }

    private String appendReferences(String answer, List<String> references, boolean english) {
        if (references == null || references.isEmpty()) {
            return answer;
        }
        String header = english ? "References" : "参考来源";
        String refs = references.stream().limit(5).map(r -> "- " + r).collect(Collectors.joining("\n"));
        if (answer != null && answer.contains(header)) {
            return answer;
        }
        return (answer == null ? "" : answer.trim()) + "\n\n" + header + ":\n" + refs;
    }

    private void touchSession(Long sessionId) {
        AiChatSessionEntity update = new AiChatSessionEntity();
        update.setId(sessionId);
        sessionMapper.updateById(update);
    }

    private CachedRagAnswer asCachedAnswer(Object value) {
        if (value instanceof CachedRagAnswer cached) {
            return cached;
        }
        return null;
    }

    private String clip(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text == null ? "" : text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private boolean isEnglish(String locale) {
        return locale != null && locale.toLowerCase(Locale.ROOT).startsWith("en");
    }

    private record CachedRagAnswer(String answer, List<String> referenceDocTitles) {}
}
