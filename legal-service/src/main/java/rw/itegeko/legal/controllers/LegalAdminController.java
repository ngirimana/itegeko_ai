package rw.itegeko.legal.controllers;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import rw.itegeko.legal.constants.ApiPaths;
import rw.itegeko.legal.payloads.admin.CreateLegalDocumentRequest;
import rw.itegeko.legal.payloads.admin.CreateLegalDocumentResponse;
import rw.itegeko.legal.services.LegalAdminService;

@RestController
public class LegalAdminController {
    private final LegalAdminService adminService;

    public LegalAdminController(LegalAdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping(ApiPaths.ADMIN_LEGAL_DOCUMENTS)
    @PreAuthorize("hasRole('ADMIN')")
    public CreateLegalDocumentResponse createDocument(@Valid @RequestBody CreateLegalDocumentRequest request) {
        return adminService.createDocument(request);
    }
}
