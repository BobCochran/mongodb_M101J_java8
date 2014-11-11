package course

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by tedc on 11/4/14.
 */
class BlogControllerTest extends Specification {

    def bc = new BlogController()

    @Unroll
    def "ExtractTags #tags to #expect_tags"() {

        expect:
            bc.extractTags(tags) == expect_tags

        where:
        tags <<
        ["aaa, bbb, ccc,  ddd,  , ,ccc",
        "aaa,bbb,ccc,ccc,bbb,aaa,ddd",
        "aaa, bbb, ccc, , ,....,ddd"]

        expect_tags = ["aaa", "bbb", "ccc", "ddd"]
    }
}
