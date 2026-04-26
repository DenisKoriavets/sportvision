package com.github.deniskoriavets.sportvision.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TelegramLinkRequest(
    @NotBlank String chatId
) {}