/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.lwm2m.demo.client.engine;

import org.eclipse.leshan.client.servers.ServerInfo;
import org.eclipse.leshan.core.request.UplinkRequest;

/**
 * Since LWM2M v1.1, endpoint name is optional in REGISTER and BOOTSTRAP request. An {@link ClientEndpointNameProvider}
 * is determine the endpoint name value which should be used in Register/BootstrapRequest.
 *
 * @see <a href="https://github.com/eclipse-leshan/leshan/issues/1457"></a>
 * @see DefaultClientEndpointNameProvider
 */
public interface ClientEndpointNameProvider {

    /**
     * @return the default endpoint name
     */
    String getEndpointName();

    /**
     * @return endpointName or null if it could(wanted) to be ignore for given kind of request.
     */
    String getEndpointNameFor(ServerInfo clientIdentity, Class<? extends UplinkRequest<?>> requestType);
}
