package org.forgerock.openam.reaper;

import org.forgerock.opendj.ldap.requests.ExtendedRequest;
import org.forgerock.opendj.ldap.responses.ExtendedResult;

public interface SearchAndDeleteExtendedRequest extends ExtendedRequest<ExtendedResult> {

    String OID = "1.3.6.1.4.1.36733.2.1.999.1";

    byte TYPE_SEARCH_BASE_ELEMENT = (byte) 0x80;
    byte TYPE_SEARCH_FILTER_ELEMENT = (byte) 0x81;
    byte TYPE_RESULT_COUNT_ELEMENT = (byte) 0x80;

    String getSearchBase();

    String getSearchFilter();

}
