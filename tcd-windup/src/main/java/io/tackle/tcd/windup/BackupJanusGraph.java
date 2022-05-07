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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.janusgraph.core.EdgeLabel;
import org.janusgraph.core.VertexLabel;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class BackupJanusGraph extends GraphOperation {

    @Override
    public void perform(GraphRewrite event, EvaluationContext context) {
        
        GraphContext graph = event.getGraphContext();
        
        try {
            graph.commit();
            graph.close();

            File sourceDirectory = graph.getGraphDirectory().toFile();
            File destinationDirectory = new File("janusgraph-backup/");
            FileUtils.copyDirectory(sourceDirectory, destinationDirectory);

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            graph.load();

        }
    }

}
