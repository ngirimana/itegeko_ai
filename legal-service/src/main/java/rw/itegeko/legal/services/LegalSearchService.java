package rw.itegeko.legal.services;

import rw.itegeko.legal.payloads.SearchResponse;

public interface LegalSearchService {
    SearchResponse search(String query, int page, int size);
}
