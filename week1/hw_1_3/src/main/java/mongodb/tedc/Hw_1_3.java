package mongodb.tedc;


import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tedc on 10/31/14.
 */
public class Hw_1_3 {


    public static void main(String[] args) {
        try {
            MongoClient client = new MongoClient();

            DB database = client.getDB("m101");
            DBCollection collection = database.getCollection("funnynumbers");

            // Not necessary yet to understand this.  It's just to prove that you
            // are able to run a command on a mongod server

            DBObject groupFields = new BasicDBObject("_id", "$value");
            groupFields.put("count", new BasicDBObject("$sum", 1));
            DBObject group = new BasicDBObject("$group", groupFields);

            DBObject match = new BasicDBObject("$match", new BasicDBObject("count", new BasicDBObject("$gt", 2)));

            DBObject sort = new BasicDBObject("$sort", new BasicDBObject("_id", -1));

            // run aggregation
            List<DBObject> pipeline = Arrays.asList(group, match, sort);

            AggregationOutput output = collection.aggregate(pipeline);

            int answer = 0;
            for (DBObject doc : output.results()) {
                //System.out.println(doc.get("count"));
                answer += (Double) doc.get("_id");
            }

            System.out.println("THE ANSWER IS: " + answer);


        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}
