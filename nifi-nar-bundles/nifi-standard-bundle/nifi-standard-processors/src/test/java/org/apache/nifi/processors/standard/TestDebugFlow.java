/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.processors.standard;

import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestDebugFlow {

    private DebugFlow debugFlow;
    private TestRunner runner;
    private ProcessContext context;
    private ProcessSession session;

    private final String content1 = "Hello, World 1!";
    private final String content2 = "Hello, World 2!";
    private final String content3 = "Hello, World 3!";

    private Map<String, String> attribs1 = new HashMap<>();
    private Map<String, String> attribs2 = new HashMap<>();
    private Map<String, String> attribs3 = new HashMap<>();
    private Map<String, String> namesToContent = new HashMap<>();

    @Before
    public void setup() throws IOException {
        debugFlow = new DebugFlow();
        runner = TestRunners.newTestRunner(debugFlow);
        context = runner.getProcessContext();
        session = runner.getProcessSessionFactory().createSession();

        String filename1 = "testFile1.txt";
        attribs1.put(CoreAttributes.FILENAME.key(), filename1);
        attribs1.put(CoreAttributes.UUID.key(), "TESTING-1234-TESTING");

        String filename2 = "testFile2.txt";
        attribs2.put(CoreAttributes.FILENAME.key(), filename2);
        attribs2.put(CoreAttributes.UUID.key(), "TESTING-2345-TESTING");

        String filename3 = "testFile3.txt";
        attribs1.put(CoreAttributes.FILENAME.key(), filename3);
        attribs3.put(CoreAttributes.UUID.key(), "TESTING-3456-TESTING");

        namesToContent.put(filename1, content1);
        namesToContent.put(filename2, content2);
        namesToContent.put(filename3, content3);

        // by default flowfiles go to success
        runner.setProperty(DebugFlow.FF_SUCCESS_ITERATIONS, "1");
        runner.setProperty(DebugFlow.FF_FAILURE_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_ROLLBACK_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_ROLLBACK_YIELD_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_ROLLBACK_PENALTY_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_EXCEPTION_ITERATIONS, "0");

        // by default if triggered without flowfile nothing happens
        runner.setProperty(DebugFlow.NO_FF_EXCEPTION_ITERATIONS, "0");
        runner.setProperty(DebugFlow.NO_FF_YIELD_ITERATIONS, "0");
        runner.setProperty(DebugFlow.NO_FF_SKIP_ITERATIONS, "1");
    }

    @Test
    public void testGetSupportedPropertyDescriptors() throws Exception {
        assertEquals(9, debugFlow.getPropertyDescriptors().size());
    }

    @Test
    public void testGetRelationships() throws Exception {
        assertEquals(2, debugFlow.getRelationships().size());
    }

    @Test
    public void testFlowFileMaxSuccessIsZeroUntilOnScheduled() throws Exception {
        assertEquals(0, debugFlow.flowFileMaxSuccess.intValue());
        runner.assertValid();
        runner.run();
        assertEquals(context.getProperty(DebugFlow.FF_SUCCESS_ITERATIONS).asInteger().intValue(), debugFlow.flowFileMaxSuccess.intValue());
    }

    @Test
    public void testNoFlowFileMaxSkipIsZeroUntilOnScheduled() throws Exception {
        assertEquals(0, debugFlow.noFlowFileMaxSkip.intValue());
        runner.assertValid();
        runner.run();
        assertEquals(context.getProperty(DebugFlow.NO_FF_SKIP_ITERATIONS).asInteger().intValue(), debugFlow.noFlowFileMaxSkip.intValue());
    }

    @Test
    public void testSuccess() {
        runner.assertValid();

        runner.enqueue(content1.getBytes(), attribs1);
        runner.enqueue(content2.getBytes(), attribs2);
        runner.enqueue(content3.getBytes(), attribs3);

        runner.run(4);
        runner.assertTransferCount(DebugFlow.REL_SUCCESS, 3);
        runner.assertTransferCount(DebugFlow.REL_FAILURE, 0);

        runner.getFlowFilesForRelationship(DebugFlow.REL_SUCCESS).get(0).assertContentEquals(content1);
        runner.getFlowFilesForRelationship(DebugFlow.REL_SUCCESS).get(1).assertContentEquals(content2);
        runner.getFlowFilesForRelationship(DebugFlow.REL_SUCCESS).get(2).assertContentEquals(content3);
    }

    @Test
    public void testFailure() {
        runner.setProperty(DebugFlow.FF_SUCCESS_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_FAILURE_ITERATIONS, "1");
        runner.setProperty(DebugFlow.FF_ROLLBACK_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_ROLLBACK_YIELD_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_ROLLBACK_PENALTY_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_EXCEPTION_ITERATIONS, "0");
        runner.assertValid();

        runner.enqueue(content1.getBytes(), attribs1);
        runner.enqueue(content2.getBytes(), attribs2);
        runner.enqueue(content3.getBytes(), attribs3);

        runner.run(4);
        runner.assertTransferCount(DebugFlow.REL_SUCCESS, 0);
        runner.assertTransferCount(DebugFlow.REL_FAILURE, 3);

        runner.getFlowFilesForRelationship(DebugFlow.REL_FAILURE).get(0).assertContentEquals(content1);
        runner.getFlowFilesForRelationship(DebugFlow.REL_FAILURE).get(1).assertContentEquals(content2);
        runner.getFlowFilesForRelationship(DebugFlow.REL_FAILURE).get(2).assertContentEquals(content3);
    }

    @Test
    public void testSuccessAndFailure() {
        runner.setProperty(DebugFlow.FF_SUCCESS_ITERATIONS, "1");
        runner.setProperty(DebugFlow.FF_FAILURE_ITERATIONS, "1");
        runner.setProperty(DebugFlow.FF_ROLLBACK_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_ROLLBACK_YIELD_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_ROLLBACK_PENALTY_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_EXCEPTION_ITERATIONS, "0");
        runner.assertValid();

        runner.enqueue(content1.getBytes(), attribs1);
        runner.enqueue(content2.getBytes(), attribs2);
        runner.enqueue(content3.getBytes(), attribs3);

        runner.run(4);
        runner.assertTransferCount(DebugFlow.REL_SUCCESS, 2);
        runner.assertTransferCount(DebugFlow.REL_FAILURE, 1);

        runner.getFlowFilesForRelationship(DebugFlow.REL_SUCCESS).get(0).assertContentEquals(content1);
        runner.getFlowFilesForRelationship(DebugFlow.REL_SUCCESS).get(1).assertContentEquals(content3);
        runner.getFlowFilesForRelationship(DebugFlow.REL_FAILURE).get(0).assertContentEquals(content2);
    }

    @Test
    public void testYield() {
        runner.setProperty(DebugFlow.FF_SUCCESS_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_FAILURE_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_ROLLBACK_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_ROLLBACK_YIELD_ITERATIONS, "1");
        runner.setProperty(DebugFlow.FF_ROLLBACK_PENALTY_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_EXCEPTION_ITERATIONS, "0");
        runner.assertValid();

        runner.enqueue(content1.getBytes(), attribs1);
        runner.enqueue(content2.getBytes(), attribs2);
        runner.enqueue(content3.getBytes(), attribs3);

        runner.run(4);
        runner.assertTransferCount(DebugFlow.REL_SUCCESS, 0);
        runner.assertTransferCount(DebugFlow.REL_FAILURE, 0);

        runner.assertQueueNotEmpty();
        assertEquals(3, runner.getQueueSize().getObjectCount());
    }

    @Test
    public void testRollback() throws IOException {
        runner.setProperty(DebugFlow.FF_SUCCESS_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_FAILURE_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_ROLLBACK_ITERATIONS, "1");
        runner.setProperty(DebugFlow.FF_ROLLBACK_YIELD_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_ROLLBACK_PENALTY_ITERATIONS, "0");
        runner.setProperty(DebugFlow.FF_EXCEPTION_ITERATIONS, "0");
        runner.assertValid();

        runner.enqueue(content1.getBytes(), attribs1);
        runner.enqueue(content2.getBytes(), attribs2);
        runner.enqueue(content3.getBytes(), attribs3);

        runner.run(4);
        runner.assertTransferCount(DebugFlow.REL_SUCCESS, 0);
        runner.assertTransferCount(DebugFlow.REL_FAILURE, 0);

        runner.assertQueueNotEmpty();
        assertEquals(3, runner.getQueueSize().getObjectCount());

        MockFlowFile ff1 = (MockFlowFile) session.get();
        assertNotNull(ff1);
        assertEquals(namesToContent.get(ff1.getAttribute(CoreAttributes.FILENAME.key())), new String(ff1.toByteArray()));
        session.rollback();
    }
}
