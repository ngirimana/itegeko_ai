export interface HttpClient {
  get<TResponse>(url: string, headers?: HeadersInit): Promise<TResponse>;
  post<TRequest, TResponse>(url: string, body: TRequest, headers?: HeadersInit): Promise<TResponse>;
}

export class FetchHttpClient implements HttpClient {
  constructor(private readonly timeoutMs = 15000) {}

  async get<TResponse>(url: string, headers?: HeadersInit): Promise<TResponse> {
    return this.request<TResponse>(url, { headers });
  }

  async post<TRequest, TResponse>(url: string, body: TRequest, headers?: HeadersInit): Promise<TResponse> {
    return this.request<TResponse>(url, {
      method: "POST",
      headers: { "accept": "application/json", "content-type": "application/json", ...headers },
      body: JSON.stringify(body)
    });
  }

  private async request<TResponse>(url: string, init?: RequestInit): Promise<TResponse> {
    const controller = new AbortController();
    const timeout = window.setTimeout(() => controller.abort(), this.timeoutMs);
    try {
      const response = await fetch(url, {
        ...init,
        headers: { "accept": "application/json", ...init?.headers },
        signal: controller.signal
      });
      return this.parse<TResponse>(response);
    } catch (caught) {
      if (caught instanceof DOMException && caught.name === "AbortError") {
        throw new Error("Request timed out. Please try again.");
      }
      throw caught;
    } finally {
      window.clearTimeout(timeout);
    }
  }

  private async parse<TResponse>(response: Response): Promise<TResponse> {
    const text = await response.text();
    const payload = text ? JSON.parse(text) : undefined;
    if (!response.ok) {
      throw new Error(payload?.message || `Request failed with status ${response.status}`);
    }
    return payload as TResponse;
  }
}
