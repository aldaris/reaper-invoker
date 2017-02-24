/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017 ForgeRock AS.
 */
package org.forgerock.openam.reaper;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import org.forgerock.opendj.ldap.ByteStringBuilder;
import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.GeneralizedTime;
import org.forgerock.opendj.ldap.LDAPConnectionFactory;
import org.forgerock.opendj.ldap.requests.Requests;
import org.forgerock.util.Options;

public class ReaperInvoker {

    public static final byte TYPE_SEARCH_BASE_ELEMENT = (byte) 0x80;
    public static final byte TYPE_SEARCH_FILTER_ELEMENT = (byte) 0x81;
    public static final byte TYPE_RESULT_COUNT_ELEMENT = (byte) 0x80;

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.out.println("Usage: java -jar reaper.jar <host> <port> <binddn> <bindpwd> <basedn>");
        }
        try (LDAPConnectionFactory cf = new LDAPConnectionFactory(args[0], Integer.valueOf(args[1]),
                Options.defaultOptions()
                        .set(LDAPConnectionFactory.AUTHN_BIND_REQUEST,
                                Requests.newSimpleBindRequest(args[2],
                                        args[3].getBytes(StandardCharsets.UTF_8))));
                Connection conn = cf.getConnection()) {
            conn.extendedRequest(new SearchAndDeleteExtendedRequestImpl(args[4], "coreTokenExpirationDate<="
                    + GeneralizedTime.currentTime().toString()));
        }
    }
}
