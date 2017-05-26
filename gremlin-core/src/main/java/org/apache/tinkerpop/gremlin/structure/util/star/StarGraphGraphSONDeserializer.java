/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.structure.util.star;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONTokens;
import org.apache.tinkerpop.gremlin.structure.util.Attachable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Provides helper functions for reading vertex and edges from their serialized GraphSON forms.
 */
public class StarGraphGraphSONDeserializer {

    /**
     * A helper function for reading vertex edges from a serialized {@link org.apache.tinkerpop.gremlin.structure.util.star.StarGraph} (i.e. a {@link java.util.Map}) generated by
     * {@link org.apache.tinkerpop.gremlin.structure.util.star.StarGraphGraphSONSerializerV1d0}.
     */
    public static void readStarGraphEdges(final Function<Attachable<Edge>, Edge> edgeMaker,
                                          final StarGraph starGraph,
                                          final Map<String, Object> vertexData,
                                          final String direction) throws IOException {
        final Map<String, List<Map<String,Object>>> edgeDatas = (Map<String, List<Map<String,Object>>>) vertexData.get(direction);
        for (Map.Entry<String, List<Map<String,Object>>> edgeData : edgeDatas.entrySet()) {
            for (Map<String,Object> inner : edgeData.getValue()) {
                final StarGraph.StarEdge starEdge;
                if (direction.equals(GraphSONTokens.OUT_E))
                    starEdge = (StarGraph.StarEdge) starGraph.getStarVertex().addOutEdge(edgeData.getKey(), starGraph.addVertex(T.id, inner.get(GraphSONTokens.IN)), T.id, inner.get(GraphSONTokens.ID));
                else
                    starEdge = (StarGraph.StarEdge) starGraph.getStarVertex().addInEdge(edgeData.getKey(), starGraph.addVertex(T.id, inner.get(GraphSONTokens.OUT)), T.id, inner.get(GraphSONTokens.ID));

                if (inner.containsKey(GraphSONTokens.PROPERTIES)) {
                    final Map<String, Object> edgePropertyData = (Map<String, Object>) inner.get(GraphSONTokens.PROPERTIES);
                    for (Map.Entry<String, Object> epd : edgePropertyData.entrySet()) {
                        starEdge.property(epd.getKey(), epd.getValue());
                    }
                }

                if (edgeMaker != null) edgeMaker.apply(starEdge);
            }
        }
    }

    /**
     * A helper function for reading a serialized {@link org.apache.tinkerpop.gremlin.structure.util.star.StarGraph} from a {@link java.util.Map} generated by
     * {@link org.apache.tinkerpop.gremlin.structure.util.star.StarGraphGraphSONSerializerV1d0}.
     */
    public static StarGraph readStarGraphVertex(final Map<String, Object> vertexData) throws IOException {
        final StarGraph starGraph = StarGraph.open();
        starGraph.addVertex(T.id, vertexData.get(GraphSONTokens.ID), T.label, vertexData.get(GraphSONTokens.LABEL));
        if (vertexData.containsKey(GraphSONTokens.PROPERTIES)) {
            final Map<String, List<Map<String, Object>>> properties = (Map<String, List<Map<String, Object>>>) vertexData.get(GraphSONTokens.PROPERTIES);
            for (Map.Entry<String, List<Map<String, Object>>> property : properties.entrySet()) {
                for (Map<String, Object> p : property.getValue()) {
                    final StarGraph.StarVertexProperty vp = (StarGraph.StarVertexProperty) starGraph.getStarVertex().property(VertexProperty.Cardinality.list, property.getKey(), p.get(GraphSONTokens.VALUE), T.id, p.get(GraphSONTokens.ID));
                    if (p.containsKey(GraphSONTokens.PROPERTIES)) {
                        final Map<String, Object> edgePropertyData = (Map<String, Object>) p.get(GraphSONTokens.PROPERTIES);
                        for (Map.Entry<String, Object> epd : edgePropertyData.entrySet()) {
                            vp.property(epd.getKey(), epd.getValue());
                        }
                    }
                }
            }
        }

        return starGraph;
    }

}