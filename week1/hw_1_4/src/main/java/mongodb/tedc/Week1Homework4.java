/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package mongodb.tedc;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Week1Homework4 {
    private static final Logger logger = LoggerFactory.getLogger("logger");

    public static void main(String[] args) throws IOException {
        /* ------------------------------------------------------------------------ */
        /* You should do this ONLY ONCE in the whole application life-cycle:        */

        /* Create and adjust the configuration singleton */
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
        cfg.setDirectoryForTemplateLoading(new File("src/main/resources"));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);

        MongoClient client = new MongoClient(new ServerAddress("localhost", 27017));

        DB database = client.getDB("m101");
        final DBCollection collection = database.getCollection("funnynumbers");

        get("/", (req, res) ->  {

                StringWriter writer = new StringWriter();
                try {
                    // Not necessary yet to understand this.  It's just to prove that you
                    // are able to run a command on a mongod server
                    DBObject groupFields = new BasicDBObject("_id", "$value");
                    groupFields.put("count", new BasicDBObject("$sum", 1));
                    DBObject group = new BasicDBObject("$group", groupFields);

                    DBObject match = new BasicDBObject("$match", new BasicDBObject("count", new BasicDBObject("$lte", 2)));

                    DBObject sort = new BasicDBObject("$sort", new BasicDBObject("_id", 1));

                    // run aggregation
                    List<DBObject> pipeline = Arrays.asList(group, match, sort);

                    AggregationOutput output = collection.aggregate(pipeline);

                    int answer = 0;
                    for (DBObject doc : output.results()) {
                        answer += (Double) doc.get("_id");
                    }

                    /* Create a data-model */
                    Map<String, String> answerMap = new HashMap<>();
                    answerMap.put("answer", Integer.toString(answer));

                    /* Get the template (uses cache internally) */
                    Template helloTemplate = cfg.getTemplate("answer.ftl");

                    /* Merge data-model with template */
                    helloTemplate.process(answerMap, writer);


                } catch (Exception e) {
                    logger.error("Failed", e);
                    halt(500);
                }
                return writer;
        });
    }
}
