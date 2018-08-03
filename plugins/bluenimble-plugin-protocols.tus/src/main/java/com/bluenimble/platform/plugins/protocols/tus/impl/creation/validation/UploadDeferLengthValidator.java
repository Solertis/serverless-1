package com.bluenimble.platform.plugins.protocols.tus.impl.creation.validation;

import javax.servlet.http.HttpServletRequest;

import com.bluenimble.platform.plugins.protocols.tus.impl.HttpHeader;
import com.bluenimble.platform.plugins.protocols.tus.impl.HttpMethod;
import com.bluenimble.platform.plugins.protocols.tus.impl.RequestValidator;
import com.bluenimble.platform.plugins.protocols.tus.impl.exception.InvalidUploadLengthException;
import com.bluenimble.platform.plugins.protocols.tus.impl.exception.TusException;
import com.bluenimble.platform.plugins.protocols.tus.impl.upload.UploadStorageService;
import com.bluenimble.platform.plugins.protocols.tus.impl.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * The request MUST include one of the following headers:
 * a) Upload-Length to indicate the size of an entire upload in bytes.
 * b) Upload-Defer-Length: 1 if upload size is not known at the time.
 */
public class UploadDeferLengthValidator implements RequestValidator {

    @Override
    public void validate(HttpMethod method, HttpServletRequest request,
                         UploadStorageService uploadStorageService, String ownerKey)
            throws TusException {

        boolean uploadLength = false;
        boolean deferredLength = false;
        boolean concatenatedUpload = false;

        if (NumberUtils.isCreatable(Utils.getHeader(request, HttpHeader.UPLOAD_LENGTH))) {
            uploadLength = true;
        }

        if (Utils.getHeader(request, HttpHeader.UPLOAD_DEFER_LENGTH).equals("1")) {
            deferredLength = true;
        }

        String uploadConcatValue = request.getHeader(HttpHeader.UPLOAD_CONCAT);
        if (StringUtils.startsWithIgnoreCase(uploadConcatValue, "final")) {
            concatenatedUpload = true;
        }

        if (!concatenatedUpload && !uploadLength && !deferredLength) {
            throw new InvalidUploadLengthException("No valid value was found in headers " + HttpHeader.UPLOAD_LENGTH
                    + " and " + HttpHeader.UPLOAD_DEFER_LENGTH);
        } else if (uploadLength && deferredLength) {
            throw new InvalidUploadLengthException("A POST request cannot contain both " + HttpHeader.UPLOAD_LENGTH
                    + " and " + HttpHeader.UPLOAD_DEFER_LENGTH + " headers.");
        }
    }

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.POST.equals(method);
    }
}
