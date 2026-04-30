"use client";

import { useMemo, useState } from "react";
import { DEFAULT_LEGAL_QUESTION, DEFAULT_SEARCH_QUERY } from "@/src/constants/app.constants";
import { LegalApiFetchService, type LegalApiService } from "@/src/services/legal-api.service";
import { extractAnswerText } from "@/src/utils/answer-format";
import type { LegalArticleResult } from "../types/legal.types";

export function useLegalAssistant(apiService?: LegalApiService) {
  const legalApi = useMemo(() => apiService ?? new LegalApiFetchService(), [apiService]);
  const [query, setQuery] = useState(DEFAULT_SEARCH_QUERY);
  const [question, setQuestion] = useState(DEFAULT_LEGAL_QUESTION);
  const [results, setResults] = useState<LegalArticleResult[]>([]);
  const [answer, setAnswer] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function runSearch() {
    await runSafely(async () => {
      const payload = await legalApi.search(query);
      setResults(payload.results || []);
    });
  }

  async function askQuestion() {
    await runSafely(async () => {
      const payload = await legalApi.ask({ question });
      setAnswer(extractAnswerText(payload));
    });
  }

  async function runSafely(action: () => Promise<void>) {
    setLoading(true);
    setError("");
    try {
      await action();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Something went wrong.");
    } finally {
      setLoading(false);
    }
  }

  return {
    query,
    setQuery,
    question,
    setQuestion,
    results,
    answer,
    loading,
    error,
    runSearch,
    askQuestion
  };
}
