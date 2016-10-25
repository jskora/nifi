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
package org.apache.nifi.provenance;

import org.apache.nifi.authorization.Authorizer;
import org.apache.nifi.authorization.user.NiFiUser;
import org.apache.nifi.events.EventReporter;
import org.apache.nifi.provenance.lineage.ComputeLineageSubmission;
import org.apache.nifi.provenance.search.Query;
import org.apache.nifi.provenance.search.QuerySubmission;
import org.apache.nifi.provenance.search.SearchableField;
import org.apache.nifi.util.NiFiProperties;

import java.io.IOException;
import java.util.*;

public class NullProvenanceRepository implements ProvenanceRepository {

    /**
     * Default no args constructor for service loading only
     */
    public NullProvenanceRepository() {
    }

    public NullProvenanceRepository(final NiFiProperties nifiProperties) {
    }

    @Override
    public void initialize(final EventReporter eventReporter, final Authorizer authorizer, final ProvenanceAuthorizableFactory resourceFactory) throws IOException {
    }

    @Override
    public ProvenanceEventRepository getProvenanceEventRepository() {
        return this;
    }

    @Override
    public ProvenanceEventBuilder eventBuilder() {
        return new StandardProvenanceEventRecord.Builder();
    }

    @Override
    public void registerEvent(final ProvenanceEventRecord event) {
    }

    @Override
    public void registerEvents(final Iterable<ProvenanceEventRecord> events) {
    }

    @Override
    public List<ProvenanceEventRecord> getEvents(final long firstRecordId, final int maxRecords) throws IOException {
        return getEvents(firstRecordId, maxRecords, null);
    }

    @Override
    public List<ProvenanceEventRecord> getEvents(final long firstRecordId, final int maxRecords, final NiFiUser user) throws IOException {
        return new ArrayList<>();
    }

    @Override
    public Long getMaxEventId() {
        return 0L;
    }

    @Override
    public ProvenanceEventRecord getEvent(final long id) {
        return null;
    }

    @Override
    public ProvenanceEventRecord getEvent(final long id, final NiFiUser user) {
        return null;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public List<SearchableField> getSearchableFields() {
        return new ArrayList<>();
    }

    @Override
    public List<SearchableField> getSearchableAttributes() {
        return new ArrayList<>();
    }

    @Override
    public QuerySubmission submitQuery(final Query query, final NiFiUser user) {
        return new AsyncQuerySubmission(query, 1, user.getIdentity());
    }

    @Override
    public QuerySubmission retrieveQuerySubmission(final String queryIdentifier, final NiFiUser user) {
        return null;
    }

    @Override
    public ComputeLineageSubmission submitLineageComputation(final long eventId, final NiFiUser user) {
        return null;
    }

    @Override
    public AsyncLineageSubmission submitLineageComputation(final String flowFileUuid, final NiFiUser user) {
        return null;
    }

    @Override
    public ComputeLineageSubmission retrieveLineageSubmission(String lineageIdentifier, final NiFiUser user) {
        return null;
    }

    @Override
    public ComputeLineageSubmission submitExpandParents(final long eventId, final NiFiUser user) {
        return null;
    }

    @Override
    public ComputeLineageSubmission submitExpandChildren(final long eventId, final NiFiUser user) {
        return null;
    }
}
