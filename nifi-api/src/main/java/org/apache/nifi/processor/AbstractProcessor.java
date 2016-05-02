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
package org.apache.nifi.processor;

import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.util.NiFiProperties;

public abstract class AbstractProcessor extends AbstractSessionFactoryProcessor {

    private static boolean rollbackLogUnackFFEnabled;
    private static long rollbackLogUnackFFMax;

    @Override
    protected void init(ProcessorInitializationContext context) {
        super.init(context);
        rollbackLogUnackFFEnabled = NiFiProperties.getInstance().isRollbackLogUnackFFEnabled();
        rollbackLogUnackFFMax = NiFiProperties.getInstance().getRollbackLogUnackFFMax();
    }

    @Override
    public final void onTrigger(final ProcessContext context, final ProcessSessionFactory sessionFactory) throws ProcessException {
        final ProcessSession session = sessionFactory.createSession();
        try {
            onTrigger(context, session);
            session.commit();
        } catch (final Throwable t) {
            final String unackMsg = (rollbackLogUnackFFEnabled) ?
                    String.format("(unacknowledged flowfiles %s) ", session.getUnacknowledgedFlowfileInfo(rollbackLogUnackFFMax)) : "";
            getLogger().error("{} failed to process {}due to {}; rolling back session", new Object[]{this, unackMsg, t});
            session.rollback(true);
            throw t;
        }
    }

    public abstract void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException;
}
