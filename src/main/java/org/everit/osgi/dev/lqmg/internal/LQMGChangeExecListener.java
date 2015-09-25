/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.dev.lqmg.internal;

import java.util.Map;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.core.PreconditionContainer.ErrorOption;
import liquibase.precondition.core.PreconditionContainer.FailOption;
import liquibase.resource.ResourceAccessor;

import org.everit.osgi.dev.lqmg.LQMG;
import org.everit.osgi.dev.lqmg.LQMGException;
import org.everit.osgi.liquibase.bundle.OSGiResourceAccessor;
import org.osgi.framework.Bundle;

public class LQMGChangeExecListener implements ChangeExecListener {

    private final ConfigurationContainer configurationContainer;

    public LQMGChangeExecListener(final ConfigurationContainer configurationContainer) {
        this.configurationContainer = configurationContainer;
    }

    @Override
    public void preconditionErrored(final PreconditionErrorException error, final ErrorOption onError) {
    }

    @Override
    public void preconditionFailed(final PreconditionFailedException error, final FailOption onFail) {
    }

    @Override
    public void ran(final Change change, final ChangeSet changeSet, final DatabaseChangeLog changeLog,
            final Database database) {
        if (!(change instanceof AbstractChange)) {
            throw new LQMGException("Change must be a descendant of AbstractChange: " + change.getClass().getName(),
                    null);
        }
        AbstractChange abstractChange = (AbstractChange) change;
        ResourceAccessor resourceAccessor = abstractChange.getResourceAccessor();
        if (!(resourceAccessor instanceof OSGiResourceAccessor)) {
            throw new LQMGException("Resource accessor must have type OSGiResourceAccessor: "
                    + resourceAccessor.getClass().getName(), null);
        }

        OSGiResourceAccessor osgiResourceAccessor = (OSGiResourceAccessor) resourceAccessor;

        Bundle bundle = osgiResourceAccessor.getBundle();
        Map<String, Object> attributes = osgiResourceAccessor.getAttributes();

        String configPath = (String) attributes.get(LQMG.CAPABILITY_LQMG_CONFIG_RESOURCE);
        if (configPath != null) {
            configurationContainer.addConfiguration(new ConfigPath(bundle, configPath));
        }
    }

    @Override
    public void ran(final ChangeSet changeSet, final DatabaseChangeLog databaseChangeLog, final Database database,
            final ExecType execType) {
    }

    @Override
    public void rolledBack(final ChangeSet changeSet, final DatabaseChangeLog databaseChangeLog, final Database database) {
    }

    @Override
    public void willRun(final Change change, final ChangeSet changeSet, final DatabaseChangeLog changeLog,
            final Database database) {
    }

    @Override
    public void willRun(final ChangeSet changeSet, final DatabaseChangeLog databaseChangeLog, final Database database,
            final RunStatus runStatus) {
    }

}
