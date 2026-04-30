import type { AskQuestionResponse } from "@/src/features/legal-assistant/types/legal.types";

export function extractAnswerText(response: AskQuestionResponse): string {
  return response.answer?.answerText || response.answerText || "No answer returned.";
}
