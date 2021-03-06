/*-
 * #%L
 * Amazon Athena Query Federation SDK
 * %%
 * Copyright (C) 2019 - 2020 Amazon Web Services
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.amazonaws.athena.connector.lambda.serde.v2;

import com.amazonaws.athena.connector.lambda.domain.Split;
import com.amazonaws.athena.connector.lambda.domain.spill.S3SpillLocation;
import com.amazonaws.athena.connector.lambda.domain.spill.SpillLocation;
import com.amazonaws.athena.connector.lambda.metadata.GetSplitsResponse;
import com.amazonaws.athena.connector.lambda.request.FederationResponse;
import com.amazonaws.athena.connector.lambda.security.EncryptionKey;
import com.amazonaws.athena.connector.lambda.serde.TypedSerDeTest;
import com.fasterxml.jackson.core.JsonEncoding;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class GetSplitsResponseSerDeTest extends TypedSerDeTest<FederationResponse>
{
    private static final Logger logger = LoggerFactory.getLogger(GetSplitsResponseSerDeTest.class);

    @Before
    public void beforeTest()
            throws IOException
    {
        String yearCol = "year";
        String monthCol = "month";
        String dayCol = "day";

        SpillLocation spillLocation1 = S3SpillLocation.newBuilder()
                .withBucket("athena-virtuoso-test")
                .withPrefix("lambda-spill")
                .withQueryId("test-query-id")
                .withSplitId("test-split-id-1")
                .withIsDirectory(true)
                .build();
        EncryptionKey encryptionKey1 = new EncryptionKey("test-key-1".getBytes(), "test-nonce-1".getBytes());
        Split split1 = Split.newBuilder(spillLocation1, encryptionKey1)
                .add(yearCol, "2017")
                .add(monthCol, "11")
                .add(dayCol, "1")
                .build();

        SpillLocation spillLocation2 = S3SpillLocation.newBuilder()
                .withBucket("athena-virtuoso-test")
                .withPrefix("lambda-spill")
                .withQueryId("test-query-id")
                .withSplitId("test-split-id-2")
                .withIsDirectory(true)
                .build();
        EncryptionKey encryptionKey2 = new EncryptionKey("test-key-2".getBytes(), "test-nonce-2".getBytes());
        Split split2 = Split.newBuilder(spillLocation2, encryptionKey2)
                .add(yearCol, "2017")
                .add(monthCol, "11")
                .add(dayCol, "2")
                .build();

        expected = new GetSplitsResponse(
                "test-catalog",
                ImmutableSet.of(split1, split2),
                "test-continuation-token");

        String expectedSerDeFile = utils.getResourceOrFail("serde/v2", "GetSplitsResponse.json");
        expectedSerDeText = utils.readAllAsString(expectedSerDeFile).trim();
    }

    @Test
    public void serialize()
            throws IOException
    {
        logger.info("serialize: enter");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        mapper.writeValue(outputStream, expected);

        String actual = new String(outputStream.toByteArray(), JsonEncoding.UTF8.getJavaName());
        logger.info("serialize: serialized text[{}]", actual);

        assertEquals(expectedSerDeText, actual);

        logger.info("serialize: exit");
    }

    @Test
    public void deserialize()
            throws IOException
    {
        logger.info("deserialize: enter");
        InputStream input = new ByteArrayInputStream(expectedSerDeText.getBytes());

        GetSplitsResponse actual = (GetSplitsResponse) mapper.readValue(input, FederationResponse.class);

        logger.info("deserialize: deserialized[{}]", actual);

        assertEquals(expected, actual);

        logger.info("deserialize: exit");
    }
}
