package com.peterservice.example.camel.restjdbc;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.restlet.engine.header.Header;
import org.restlet.util.Series;
import org.springframework.util.StringUtils;

/**
 * Created by fdman on 30.09.2015.
 */
public class HeaderMovingProcessor implements Processor {

    public static final String ORG_RESTLET_HTTP_HEADERS = "org.restlet.http.headers";
    public static final String FIRST_NAME = "firstName";
    public static final String USER_ID = "userId";
    public static final String LAST_NAME = "lastName";

    @Override
    public void process(Exchange exchange) throws Exception {
        Series<Header> headers = (Series<Header>) exchange.getIn().getHeaders().get(ORG_RESTLET_HTTP_HEADERS);
        Message in = exchange.getIn();
        moveHeader(USER_ID, headers, in);
        moveHeader(FIRST_NAME, headers, in);
        moveHeader(LAST_NAME, headers, in);
    }

    private void moveHeader(String headerName, Series<Header> headers, Message in) {
        String firstValue = headers.getFirstValue(headerName);
        if (!StringUtils.isEmpty(firstValue)) {
            in.setHeader(headerName, firstValue);
        }
    }
}
