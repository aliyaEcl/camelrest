/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peterservice.example.camel.restjdbc;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class ExampleUsersRouteConfiguration extends RouteBuilder {

    private Processor headerMovingProcessor;

    public ExampleUsersRouteConfiguration(HeaderMovingProcessor headerMovingProcessor) {
        this.headerMovingProcessor = headerMovingProcessor;
    }

    @Override
    public void configure() {
        from("restlet:/users?restletMethod=POST")
                .process(headerMovingProcessor)
                .setBody(simple("insert into users(firstName, lastName) values('${headers.firstName}','${headers.lastName}')"))
                .to("jdbc:dataSource")
                .setBody(simple("select * from users where id in (select max(id) from users)"))
                .to("jdbc:dataSource");

        from("restlet:/users/{userId}?restletMethods=GET,PUT,DELETE")
                .process(headerMovingProcessor)
                .choice()
                .when(simple("${header.CamelHttpMethod} == 'GET'"))
                .setBody(simple("select * from users where id = ${header.userId}"))
                .when(simple("${header.CamelHttpMethod} == 'PUT'"))
                .setBody(simple("update users set firstName='${header.firstName}', lastName='${header.lastName}' where id = ${header.userId}"))
                .when(simple("${header.CamelHttpMethod} == 'DELETE'"))
                .setBody(simple("delete from users where id = ${header.userId}"))
                .otherwise()
                .stop()
                .end()
                .to("jdbc:dataSource");

        from("restlet:/users?restletMethod=GET")
                .setBody(simple("select * from users"))
                .to("jdbc:dataSource");
    }


}

