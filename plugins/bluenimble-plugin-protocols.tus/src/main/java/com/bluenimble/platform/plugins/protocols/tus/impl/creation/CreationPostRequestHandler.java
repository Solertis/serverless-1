package com.bluenimble.platform.plugins.protocols.tus.impl.creation;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bluenimble.platform.plugins.protocols.tus.impl.HttpHeader;
import com.bluenimble.platform.plugins.protocols.tus.impl.HttpMethod;
import com.bluenimble.platform.plugins.protocols.tus.impl.upload.UploadInfo;
import com.bluenimble.platform.plugins.protocols.tus.impl.upload.UploadStorageService;
import com.bluenimble.platform.plugins.protocols.tus.impl.util.AbstractRequestHandler;
import com.bluenimble.platform.plugins.protocols.tus.impl.util.TusServletRequest;
import com.bluenimble.platform.plugins.protocols.tus.impl.util.TusServletResponse;
import com.bluenimble.platform.plugins.protocols.tus.impl.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Server MUST acknowledge a successful upload creation with the 201 Created status.
 * The Server MUST set the Location header to the URL of the created resource. This URL MAY be absolute or relative.
 */
public class CreationPostRequestHandler extends AbstractRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(CreationPostRequestHandler.class);

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.POST.equals(method);
    }

    @Override
    public void process (HttpMethod method, TusServletRequest servletRequest,
                        TusServletResponse servletResponse, UploadStorageService uploadStorageService,
                        String ownerKey) throws IOException {

        UploadInfo info = buildUploadInfo (servletRequest);
        info = uploadStorageService.create (info, ownerKey);

        //We've already validated that the current request URL matches our upload URL so we can safely use it.
        String url = servletRequest.getRequestURL().append("/").append(info.getId()).toString();
        servletResponse.setHeader(HttpHeader.LOCATION, url);
        servletResponse.setStatus(HttpServletResponse.SC_CREATED);

        log.debug("Create upload location {}", url);
    }

    private UploadInfo buildUploadInfo(HttpServletRequest servletRequest) {
        UploadInfo info = new UploadInfo();

        Long length = Utils.getLongHeader(servletRequest, HttpHeader.UPLOAD_LENGTH);
        if (length != null) {
            info.setLength(length);
        }

        String metadata = Utils.getHeader(servletRequest, HttpHeader.UPLOAD_METADATA);
        if (StringUtils.isNotBlank(metadata)) {
            info.setEncodedMetadata(metadata);
        }

        return info;
    }
}
