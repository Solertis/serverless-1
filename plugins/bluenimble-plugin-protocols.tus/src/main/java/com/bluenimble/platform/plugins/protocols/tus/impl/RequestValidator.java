package com.bluenimble.platform.plugins.protocols.tus.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.bluenimble.platform.plugins.protocols.tus.impl.exception.TusException;
import com.bluenimble.platform.plugins.protocols.tus.impl.upload.UploadStorageService;

/**
 * Interface for request validators
 */
public interface RequestValidator {

    /**
     * Validate if the request should be processed
     * @param method The HTTP method of this request (do not use {@link HttpServletRequest#getMethod()}!)
     * @param request The {@link HttpServletRequest} to validate
     * @param uploadStorageService The current upload storage service
     * @param ownerKey A key representing the owner of the upload
     * @throws TusException When validation fails and the request should not be processed
     */
    void validate(HttpMethod method, HttpServletRequest request,
                  UploadStorageService uploadStorageService, String ownerKey)
            throws TusException, IOException;

    /**
     * Test if this validator supports the given HTTP method
     * @param method The current HTTP method
     * @return true if supported, false otherwise
     */
    boolean supports(HttpMethod method);
}
