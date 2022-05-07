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

@TypeValue(DatasourcePropsModel.TYPE)
public interface DatasourcePropsModel extends PropsModel {

    String TYPE = "DatasourcePropsModel";
    String URL = "url";
    String PORT = "port";
    String DRIVER = "driver";
    String USERNAME = "username";
    String PASSWORD = "password";

    @Property(URL)
    String getUrl();

    @Property(URL)
    DatasourcePropsModel setUrl(String url);

    @Property(DRIVER)
    String getDriver();

    @Property(DRIVER)
    void setDriver(String driver);

    @Property(USERNAME)
    String getUsername();

    @Property(USERNAME)
    void setUsername(String driver);

    @Property(PASSWORD)
    String getPassword();

    @Property(PASSWORD)
    void setPassword(String driver);
}
