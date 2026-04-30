package rw.itegeko.legal.services;

import rw.itegeko.legal.payloads.admin.CreateLegalDocumentRequest;
import rw.itegeko.legal.payloads.admin.CreateLegalDocumentResponse;

public interface LegalAdminService {
    CreateLegalDocumentResponse createDocument(CreateLegalDocumentRequest request);
}
