/**
 * This file is part of Everit - Liquibase-QueryDSL Model Generator.
 *
 * Everit - Liquibase-QueryDSL Model Generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Liquibase-QueryDSL Model Generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Liquibase-QueryDSL Model Generator.  If not, see <http://www.gnu.org/licenses/>.
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

    public LQMGChangeExecListener(ConfigurationContainer configurationContainer) {
        this.configurationContainer = configurationContainer;
    }

    @Override
    public void preconditionErrored(PreconditionErrorException error, ErrorOption onError) {
    }

    @Override
    public void preconditionFailed(PreconditionFailedException error, FailOption onFail) {
    }

    @Override
    public void ran(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {
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
    public void ran(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ExecType execType) {
    }

    @Override
    public void rolledBack(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
    }

    @Override
    public void willRun(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {
    }

    @Override
    public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, RunStatus runStatus) {
    }

}
