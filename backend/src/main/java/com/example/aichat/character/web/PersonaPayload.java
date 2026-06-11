package com.example.aichat.character.web;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Structured character persona rules used during debate.")
public record PersonaPayload(
        @Schema(description = "Character identity.", example = "정책 분석가")
        @NotBlank String identity,
        @Schema(description = "Role this character plays in a debate.", example = "현실성 검증자")
        @NotBlank String debateRole,
        @ArraySchema(schema = @Schema(description = "Core value.", example = "실증성"))
        @NotEmpty List<@NotBlank String> coreValues,
        @ArraySchema(schema = @Schema(description = "Expertise area.", example = "공공정책"))
        @NotNull List<@NotBlank String> expertise,
        @Schema(description = "Default stance formation rule.",
                example = "선의보다 실행 가능성과 부작용을 먼저 본다.")
        @NotBlank String defaultStance,
        @Schema(description = "Preferred evidence style.",
                example = "통계, 비교 사례, 비용-편익 분석을 선호한다.")
        @NotBlank String evidenceStyle,
        @ArraySchema(schema = @Schema(description = "Debate behavior rule.",
                example = "상대 주장의 숨은 전제를 찾는다."))
        @NotNull List<@NotBlank String> debateBehavior,
        @Valid @NotNull VoiceStyle voiceStyle,
        @Valid @NotNull Boundaries boundaries,
        @ArraySchema(schema = @Schema(description = "Example line.",
                example = "그 주장의 선의는 이해하지만, 실행 조건을 봐야 합니다."))
        @NotNull List<@NotBlank String> exampleLines
) {
    @Schema(description = "Persona voice style.")
    public record VoiceStyle(
            @Schema(description = "Tone.", example = "차분하지만 날카로움")
            @NotBlank String tone,
            @Schema(description = "Sentence length preference.", example = "중간")
            @NotBlank String sentenceLength,
            @Schema(description = "Rhetorical style.", example = "질문과 구조적 반박 중심")
            @NotBlank String rhetoricalStyle,
            @ArraySchema(schema = @Schema(description = "Signature phrase.",
                    example = "핵심은 의도가 아니라 실행 조건입니다."))
            @NotNull List<@NotBlank String> signaturePhrases
    ) {
    }

    @Schema(description = "Required and forbidden debate behavior.")
    public record Boundaries(
            @ArraySchema(schema = @Schema(description = "Required behavior.",
                    example = "상대 주장을 먼저 요약한다."))
            @NotEmpty List<@NotBlank String> mustDo,
            @ArraySchema(schema = @Schema(description = "Forbidden behavior.",
                    example = "인신공격하지 않는다."))
            @NotEmpty List<@NotBlank String> mustNotDo
    ) {
    }
}
