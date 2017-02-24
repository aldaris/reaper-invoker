package org.forgerock.openam.reaper;

import static com.forgerock.opendj.ldap.CoreMessages.*;
import static com.forgerock.opendj.util.StaticUtils.*;

import java.io.IOException;

import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.opendj.io.ASN1;
import org.forgerock.opendj.io.ASN1Reader;
import org.forgerock.opendj.io.ASN1Writer;
import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.ByteStringBuilder;
import org.forgerock.opendj.ldap.DecodeException;
import org.forgerock.opendj.ldap.DecodeOptions;
import org.forgerock.opendj.ldap.ResultCode;
import org.forgerock.opendj.ldap.controls.Control;
import org.forgerock.opendj.ldap.requests.AbstractExtendedRequest;
import org.forgerock.opendj.ldap.requests.ExtendedRequest;
import org.forgerock.opendj.ldap.requests.ExtendedRequestDecoder;
import org.forgerock.opendj.ldap.responses.AbstractExtendedResultDecoder;
import org.forgerock.opendj.ldap.responses.ExtendedResult;
import org.forgerock.opendj.ldap.responses.ExtendedResultDecoder;
import org.forgerock.opendj.ldap.responses.Responses;

public final class SearchAndDeleteExtendedRequestImpl
        extends AbstractExtendedRequest<SearchAndDeleteExtendedRequest, ExtendedResult>
        implements SearchAndDeleteExtendedRequest {

    static final class RequestDecoder implements
            ExtendedRequestDecoder<SearchAndDeleteExtendedRequest, ExtendedResult> {
        @Override
        public SearchAndDeleteExtendedRequest decodeExtendedRequest(final ExtendedRequest<?> request,
                                                           final DecodeOptions options) throws DecodeException {
            final ByteString requestValue = request.getValue();
            if (requestValue == null || requestValue.length() <= 0) {
                throw DecodeException.error(ERR_EXTOP_CANCEL_NO_REQUEST_VALUE.get());
            }

            ByteString searchBase = null, searchFilter = null;

            try {

                final ASN1Reader reader = ASN1.getReader(requestValue);
                reader.readStartSequence();
                if (reader.hasNextElement() && reader.peekType() == TYPE_SEARCH_BASE_ELEMENT) {
                    searchBase = reader.readOctetString();
                }
                if (reader.hasNextElement() && reader.peekType() == TYPE_SEARCH_FILTER_ELEMENT) {
                    searchFilter = reader.readOctetString();
                }
                reader.readEndSequence();

                if (searchBase.isEmpty() || searchFilter.isEmpty()) {
                    //error, you ded.
                    return null;
                }

                final SearchAndDeleteExtendedRequest newRequest
                        = new SearchAndDeleteExtendedRequestImpl(searchBase.toString(), searchFilter.toString());

                for (final Control control : request.getControls()) {
                    newRequest.addControl(control);
                }

                return newRequest;
            } catch (final IOException e) {
                final LocalizableMessage message =
                        ERR_EXTOP_CANCEL_CANNOT_DECODE_REQUEST_VALUE.get(getExceptionMessage(e));
                throw DecodeException.error(message, e);
            }
        }
    }

    private static final class ResultDecoder extends AbstractExtendedResultDecoder<ExtendedResult> {
        @Override
        public ExtendedResult decodeExtendedResult(final ExtendedResult result,
                                                   final DecodeOptions options) throws DecodeException {
            return result; //this will just contain the number of matched entries
        }

        @Override
        public ExtendedResult newExtendedErrorResult(final ResultCode resultCode,
                                                     final String matchedDN, final String diagnosticMessage) {
            return Responses.newGenericExtendedResult(resultCode).setMatchedDN(matchedDN)
                    .setDiagnosticMessage(diagnosticMessage);
        }
    }

    private static final ExtendedResultDecoder<ExtendedResult> RESULT_DECODER = new ResultDecoder();

    private final String searchBase;
    private final String searchFilter;

    SearchAndDeleteExtendedRequestImpl(final SearchAndDeleteExtendedRequest searchAndDeleteExtendedRequest) {
        super(searchAndDeleteExtendedRequest);
        this.searchBase = searchAndDeleteExtendedRequest.getSearchBase();
        this.searchFilter = searchAndDeleteExtendedRequest.getSearchFilter();
    }

    public SearchAndDeleteExtendedRequestImpl(String searchBase, String searchFilter) {
        this.searchBase = searchBase;
        this.searchFilter = searchFilter;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    @Override
    public String getOID() {
        return OID;
    }

    @Override
    public ExtendedResultDecoder<ExtendedResult> getResultDecoder() {
        return RESULT_DECODER;
    }

    @Override
    public ByteString getValue() {
        final ByteStringBuilder buffer = new ByteStringBuilder(6);
        final ASN1Writer writer = ASN1.getWriter(buffer);

        try {
            writer.writeStartSequence();
            writer.writeOctetString(TYPE_SEARCH_BASE_ELEMENT, searchBase);
            writer.writeOctetString(TYPE_SEARCH_FILTER_ELEMENT, searchFilter);
            writer.writeEndSequence();
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }

        return buffer.toByteString();
    }

    @Override
    public boolean hasValue() {
        return true;
    }
 }
