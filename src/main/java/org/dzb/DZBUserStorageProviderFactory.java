/*
 * Copyright 2019 DZB Leipzig
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dzb;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;


import java.util.Properties;

/**
 * @author <a href="mailto:lars.voigt@dzb.de">Lars Voigt</a>
 * @version $Revision: 1 $
 */
public class DZBUserStorageProviderFactory implements UserStorageProviderFactory<DZBUserStorageProvider> {

    private static final Logger logger = Logger.getLogger(DZBUserStorageProviderFactory.class);
    public static final String PROVIDER_NAME = "dzb-userstore";
    protected MSSQLDatabaseAdapter mssqlDatabaseAdapter;
    private static String SERVER_NAME = "";
    private static String DB = "";
    private static String USER = "";
    private static String PASSWORD = "";

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }


    @Override
    public void init(Config.Scope config) {

        Properties properties = new Properties();
        properties.setProperty("serverName", SERVER_NAME);
        properties.setProperty("db", DB);
        properties.setProperty("user", USER);
        properties.setProperty("password", PASSWORD);

        try {
            mssqlDatabaseAdapter = new MSSQLDatabaseAdapter(properties);
            mssqlDatabaseAdapter.getConnection();
        } catch (Exception ex) {
            logger.error("Failed to connect database", ex);
        }
    }

    @Override
    public DZBUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new DZBUserStorageProvider(session, model, mssqlDatabaseAdapter);
    }

}
