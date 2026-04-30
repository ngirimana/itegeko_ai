import { LEGAL_API_PATHS } from "@/src/constants/api.constants";
import type {
  AskQuestionRequest,
  AskQuestionResponse,
  CreateLegalDocumentRequest,
  CreateLegalDocumentResponse,
  LegalCatalogResponse,
  LegalSearchResponse
} from "@/src/features/legal-assistant/types/legal.types";
import { FetchHttpClient, type HttpClient } from "./http-client";

export interface LegalApiService {
  catalog(): Promise<LegalCatalogResponse>;
  search(query: string): Promise<LegalSearchResponse>;
  ask(request: AskQuestionRequest): Promise<AskQuestionResponse>;
  createLegalDocument(request: CreateLegalDocumentRequest, accessToken: string): Promise<CreateLegalDocumentResponse>;
}

export class LegalApiFetchService implements LegalApiService {
  constructor(private readonly httpClient: HttpClient = new FetchHttpClient()) {}

  catalog(): Promise<LegalCatalogResponse> {
    return this.httpClient.get<LegalCatalogResponse>(LEGAL_API_PATHS.catalog);
  }

  search(query: string): Promise<LegalSearchResponse> {
    const params = new URLSearchParams({ q: query });
    return this.httpClient.get<LegalSearchResponse>(`${LEGAL_API_PATHS.search}?${params.toString()}`);
  }

  ask(request: AskQuestionRequest): Promise<AskQuestionResponse> {
    return this.httpClient.post<AskQuestionRequest, AskQuestionResponse>(LEGAL_API_PATHS.ask, request);
  }

  createLegalDocument(
    request: CreateLegalDocumentRequest,
    accessToken: string
  ): Promise<CreateLegalDocumentResponse> {
    return this.httpClient.post<CreateLegalDocumentRequest, CreateLegalDocumentResponse>(
      LEGAL_API_PATHS.adminLegalDocuments,
      request,
      { Authorization: `Bearer ${accessToken}` }
    );
  }
}
