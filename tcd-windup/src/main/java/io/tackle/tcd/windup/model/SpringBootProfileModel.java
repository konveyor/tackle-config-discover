/*
Copyright IBM Corporation 2021
Licensed under the Eclipse Public License 2.0, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package io.tackle.tcd.windup.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

@TypeValue(SpringBootProfileModel.TYPE)
public interface SpringBootProfileModel extends ProfileModel {

    String TYPE = "SpringBootProfileModel";
    String NAME = "name";
    String WITH_PROPERTIES_FILE = "withPropertiesFile";
    String WITH_ANNOTATIONS = "withAnnotations";

    @Property(NAME)
    @Indexed
    String getName();

    @Property(NAME)
    SpringBootProfileModel setName(String name);

    @Property(WITH_PROPERTIES_FILE)
    boolean getWithPropertiesFile();

    @Property(WITH_PROPERTIES_FILE)
    SpringBootProfileModel setWithPropertiesFile(boolean flag);

    @Property(WITH_ANNOTATIONS)
    boolean getWithAnnotations();

    @Property(WITH_ANNOTATIONS)
    SpringBootProfileModel setWithAnnotations(boolean flag);

}
