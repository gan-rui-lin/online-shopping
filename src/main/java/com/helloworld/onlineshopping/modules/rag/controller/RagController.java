package com.helloworld.onlineshopping.modules.rag.controller;
import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.rag.dto.RagAskDTO;
import com.helloworld.onlineshopping.modules.rag.service.KnowledgeService;
import com.helloworld.onlineshopping.modules.rag.service.RagService;
import com.helloworld.onlineshopping.modules.rag.vo.ChatMessageVO;
import com.helloworld.onlineshopping.modules.rag.vo.RagAnswerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "RAG", description = "RAG Product Q&A APIs")
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {
    private final RagService ragService;
    private final KnowledgeService knowledgeService;

    @Operation(summary = "Ask product question")
    @PostMapping("/ask")
    public Result<RagAnswerVO> ask(@Valid @RequestBody RagAskDTO dto) {
        return Result.success(ragService.ask(dto));
    }

    @Operation(summary = "Chat history")
    @GetMapping("/session/{sessionId}/history")
    public Result<List<ChatMessageVO>> history(@PathVariable Long sessionId) {
        return Result.success(ragService.getChatHistory(sessionId));
    }

    @Operation(summary = "Import product knowledge")
    @PostMapping("/knowledge/import/{spuId}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<Void> importKnowledge(@PathVariable Long spuId) {
        knowledgeService.importFromProduct(spuId);
        return Result.success();
    }
}
