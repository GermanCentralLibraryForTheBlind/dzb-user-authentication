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

import javax.naming.InitialContext;

/**
 * @author <a href="mailto:lars.voigt@dzb.de">Lars Voigt</a>
 * @version $Revision: 1 $
 */
public class DzbUserStorageProviderFactory implements UserStorageProviderFactory<DzbUserStorageProvider> {

    private static final Logger logger = Logger.getLogger(DzbUserStorageProviderFactory.class);
    public static final String PROVIDER_NAME = "dzb-userstore";


    @Override
    public String getId() {
        return PROVIDER_NAME;
    }


//    @Override
//    public void init(Config.Scope config) {
//
//    }

    @Override
    public DzbUserStorageProvider create(KeycloakSession session, ComponentModel model) {

        logger.info("create userstorage provider ...");
        try {
            InitialContext ctx = new InitialContext();

            String lookupId = "java:global/dzb-userstore-keycloak-spi-adapter/" + DzbUserStorageProvider.class.getSimpleName();
            DzbUserStorageProvider provider = (DzbUserStorageProvider) ctx.lookup(lookupId);
            provider.setModel(model);
            provider.setSession(session);

            logger.info("userstorage provider created ...");
            return provider;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }
}
