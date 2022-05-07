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

package io.tackle.tcd.windup;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.PropertiesModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

import io.tackle.tcd.windup.model.DatasourcePropsModel;
import io.tackle.tcd.windup.model.MainProfileModel;
import io.tackle.tcd.windup.model.ProfileModel;
import io.tackle.tcd.windup.model.ServerPropsModel;
import io.tackle.tcd.windup.model.SpringBootProfileModel;
import io.tackle.tcd.windup.model.TestProfileModel;
import io.tackle.tcd.windup.service.GetOrCreateGraphService;
import io.tackle.tcd.windup.service.SingletonGraphService;

public class SpringBootAnalysis {

    public static final class SpringProfileExtraction extends GraphOperation {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context) {
            // System.out.println(TCDRuleProvider.this.getClass().getClassLoader());
            System.out.println(event);
            System.out.println(context);

            GraphService<PropertiesModel> properties = new GraphService<>(event.getGraphContext(),
                    PropertiesModel.class);

            for (PropertiesModel file : properties.findAll()) {
                try {
                    System.out.println(file.getFileName() + " => " + file.getProperties());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            collectProfiles(event);

            GraphService<SpringBootProfileModel> profiles = new GraphService<>(event.getGraphContext(),
                    SpringBootProfileModel.class);

            for (SpringBootProfileModel profile : profiles.findAll()) {
                System.out.println(profile);
            }

            Configuration conf = event.getGraphContext().getGraph().configuration();
            System.out.println(conf);

            // GraphService<JavaSourceFileModel> sources = new
            // GraphService<>(event.getGraphContext(), JavaSourceFileModel.class);
            // for (JavaSourceFileModel src: sources.findAll()) {
            // System.out.println(src);
            // }

        }
    }

    public static Pattern profilePropertiesRegex = Pattern.compile("^application-(.+)\\.properties$");

    public static void collectProfiles(GraphRewrite event) {

        GetOrCreateGraphService<SpringBootProfileModel> profiles = new GetOrCreateGraphService<>(
                event.getGraphContext(), SpringBootProfileModel.class);
        SingletonGraphService<MainProfileModel> main = new SingletonGraphService<>(event.getGraphContext(),
                MainProfileModel.class);
        SingletonGraphService<TestProfileModel> test = new SingletonGraphService<>(event.getGraphContext(),
                TestProfileModel.class);

        GraphService<DatasourcePropsModel> datasources = new GraphService<>(event.getGraphContext(),
                DatasourcePropsModel.class);
        GraphService<ServerPropsModel> servers = new GraphService<>(event.getGraphContext(), ServerPropsModel.class);

        GraphService<PropertiesModel> properties = new GraphService<>(event.getGraphContext(), PropertiesModel.class);
        for (PropertiesModel file : properties.findAll()) {

            ProfileModel profile = main.singleton();

            Matcher m = profilePropertiesRegex.matcher(file.getFileName());
            if (m.matches()) {
                SpringBootProfileModel springProfile = profiles.create();
                springProfile.setName(m.group(1));
                springProfile.setWithPropertiesFile(true);
                profile = springProfile;
            } else if (file.getFileName().equals("application.properties")) {
                Path path = Paths.get(file.getPrettyPath());
                for (Path p : path) {
                    if (p.toString().equals("test")) {
                        profile = test.singleton();
                        break;
                    }
                }
            } else {
                continue;
            }

            DatasourcePropsModel datasource = null;
            Properties p;
            try {
                p = file.getProperties();
            } catch (IOException e) {
                continue;
            }
            if (p.containsKey("spring.datasource.url")) {
                if (datasource == null)
                    datasource = datasources.create();
                datasource.setUrl(p.getProperty("spring.datasource.url"));
            }
            if (p.containsKey("spring.datasource.driver-class-name")) {
                if (datasource == null) {
                    datasource = datasources.create();
                }
                datasource.setDriver(p.getProperty("spring.datasource.driver-class-name"));
            }
            if (p.containsKey("spring.datasource.username")) {
                if (datasource == null)
                    datasource = datasources.create();
                datasource.setUsername(p.getProperty("spring.datasource.username"));
            }
            if (p.containsKey("spring.datasource.password")) {
                if (datasource == null)
                    datasource = datasources.create();
                datasource.setPassword(p.getProperty("spring.datasource.password"));
            }
            if (profile != null && datasource != null) {
                datasource.addProfile(profile);
                profile.addProperties(datasource);
            }

            ServerPropsModel server = null;
            if (p.containsKey("server.context-path")) {
                if (server == null)
                    server = servers.create();
                server.setContextPath(p.getProperty("server.context-path"));
            }            
            if (p.containsKey("server.servlet.context-path")) {
                if (server == null)
                    server = servers.create();
                server.setServletContextPath(p.getProperty("server.servlet.context-path"));
            }
            if (p.containsKey("server.port")) {
                if (server == null)
                    server = servers.create();
                server.setPort(Integer.parseInt(p.getProperty("server.port")));
            }
            if (profile != null && server != null) {
                server.addProfile(profile);
                profile.addProperties(server);
            }
        }

//        GraphService<JavaAnnotationTypeReferenceModel> refs = new GraphService<>(event.getGraphContext(), JavaAnnotationTypeReferenceModel.class);
//        for (JavaAnnotationTypeReferenceModel ref: refs.findAll()) {
//            // profiles.
//            System.out.println(ref);
//        }

        GraphService<JavaAnnotationTypeReferenceModel> refs = new GraphService<>(event.getGraphContext(),
                JavaAnnotationTypeReferenceModel.class);
        for (JavaAnnotationTypeReferenceModel ref : refs.findAll()) {
            if (ref.getResolvedSourceSnippit().equals("org.springframework.context.annotation.Profile")) {
                JavaAnnotationLiteralTypeValueModel literal = (JavaAnnotationLiteralTypeValueModel) ref
                        .getAnnotationValues().get("value");
                SpringBootProfileModel model = profiles.getOrCreate(SpringBootProfileModel.NAME,
                        literal.getLiteralValue());
                model.setWithAnnotations(true);
            }
        }

    }

}
