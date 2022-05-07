/*
 * Copyright IBM Corporation 2021, 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tackle.tcd.windup.model;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

@TypeValue(ServerPropsModel.TYPE)
public interface ServerPropsModel extends PropsModel {

    String TYPE = "ServerPropsModel";
    String CONTEXT_PATH = "contextPath";
    String SERVLET_CONTEXT_PATH = "servletContextPath";
    String PORT = "port";

    @Property(CONTEXT_PATH)
    String getContextPath();

    @Property(CONTEXT_PATH)
    void setContextPath(String contextPath);
    
    @Property(SERVLET_CONTEXT_PATH)
    String getServletContextPath();

    @Property(SERVLET_CONTEXT_PATH)
    void setServletContextPath(String contextPath);

    @Property(PORT)
    int getPort();

    @Property(PORT)
    void setPort(int port);    

}
