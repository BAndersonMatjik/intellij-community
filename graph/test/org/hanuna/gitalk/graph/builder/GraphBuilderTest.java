package org.hanuna.gitalk.graph.builder;

import org.hanuna.gitalk.commitmodel.Commit;
import org.hanuna.gitalk.graph.Graph;
import org.hanuna.gitalk.graph.mutable.GraphBuilder;
import org.hanuna.gitalk.parser.SimpleCommitListParser;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.hanuna.gitalk.GraphStrUtils.toStr;

/**
 * @author erokhins
 */
public class GraphBuilderTest {

    public void runTest(String input, String out) throws IOException {
        SimpleCommitListParser parser = new SimpleCommitListParser(new StringReader(input));
        List<Commit> commits = parser.readAllCommits();
        Graph model = GraphBuilder.build(commits);
        assertEquals(out, toStr(model));

    }

    @Test
    public void simple1() throws IOException {
        runTest("12|-", "12|-|-|-COMMIT_NODE|-12|-0");
    }

    @Test
    public void simple2() throws IOException {
        runTest(
                "12|-af\n" +
                "af|-",

                "12|-|-12:af:USUAL:12|-COMMIT_NODE|-12|-0\n" +
                "af|-12:af:USUAL:12|-|-COMMIT_NODE|-12|-1"
        );
    }

    /**
     *   a0
     *  |  \
     *  |  a1
     *  |  | \
     *  | a2  \
     *  |/ |\  \
     * a3  | \ |
     *  |  | | a4
     *  |  | | /
     *  | a5 |/
     *  |  |/|
     *  a6 * |
     *  | / /
     *  a7 /
     *    /
     *  a8
     * @throws IOException
     */
    @Test
    public void moreBranches() throws IOException {
        runTest(
                "a0|-a3 a1\n" +
                "a1|-a2 a4\n" +
                "a2|-a3 a5 a8\n" +
                "a3|-a6\n" +
                "a4|-a7\n" +
                "a5|-a7\n" +
                "a6|-a7\n" +
                "a7|-\n" +
                "a8|-",

                "a0|-|-a0:a1:USUAL:a1 a0:a3:USUAL:a0|-COMMIT_NODE|-a0|-0\n" +
                "a1|-a0:a1:USUAL:a1|-a1:a2:USUAL:a1 a1:a4:USUAL:a4|-COMMIT_NODE|-a1|-1\n" +
                "a2|-a1:a2:USUAL:a1|-a2:a3:USUAL:a1 a2:a5:USUAL:a5 a2:a8:USUAL:a8|-COMMIT_NODE|-a1|-2\n" +
                "a3|-a0:a3:USUAL:a0 a2:a3:USUAL:a1|-a3:a6:USUAL:a0|-COMMIT_NODE|-a0|-3\n" +
                "a4|-a1:a4:USUAL:a4|-a4:a7:USUAL:a4|-COMMIT_NODE|-a4|-4\n" +
                "a5|-a2:a5:USUAL:a5|-a5:a7:USUAL:a5|-COMMIT_NODE|-a5|-5\n" +
                "a7|-a4:a7:USUAL:a4 a5:a7:USUAL:a5|-a7:a7:USUAL:a4|-EDGE_NODE|-a4|-6\n" +
                "   a6|-a3:a6:USUAL:a0|-a6:a7:USUAL:a0|-COMMIT_NODE|-a0|-6\n" +
                "a7|-a6:a7:USUAL:a0 a7:a7:USUAL:a4|-|-COMMIT_NODE|-a4|-7\n" +
                "a8|-a2:a8:USUAL:a8|-|-COMMIT_NODE|-a8|-8"
        );
    }

    @Test
    public void youngerBranches() throws IOException {
        runTest(
                "a0|-a5 a1 a3\n" +
                "a1|-a4\n" +
                "a2|-a6\n" +
                "a3|-a6\n" +
                "a4|-a6\n" +
                "a5|-a6\n" +
                "a6|-a7\n" +
                "a7|-a8\n" +
                "a8|-",

                "a0|-|-a0:a1:USUAL:a1 a0:a3:USUAL:a3 a0:a5:USUAL:a0|-COMMIT_NODE|-a0|-0\n" +
                "a1|-a0:a1:USUAL:a1|-a1:a4:USUAL:a1|-COMMIT_NODE|-a1|-1\n" +
                "a2|-|-a2:a6:USUAL:a2|-COMMIT_NODE|-a2|-2\n" +
                "a3|-a0:a3:USUAL:a3|-a3:a6:USUAL:a3|-COMMIT_NODE|-a3|-3\n" +
                "a6|-a2:a6:USUAL:a2 a3:a6:USUAL:a3|-a6:a6:USUAL:a2|-EDGE_NODE|-a2|-4\n" +
                "   a4|-a1:a4:USUAL:a1|-a4:a6:USUAL:a1|-COMMIT_NODE|-a1|-4\n" +
                "a6|-a4:a6:USUAL:a1 a6:a6:USUAL:a2|-a6:a6:USUAL:a2|-EDGE_NODE|-a2|-5\n" +
                "   a5|-a0:a5:USUAL:a0|-a5:a6:USUAL:a0|-COMMIT_NODE|-a0|-5\n" +
                "a6|-a5:a6:USUAL:a0 a6:a6:USUAL:a2|-a6:a7:USUAL:a2|-COMMIT_NODE|-a2|-6\n" +
                "a7|-a6:a7:USUAL:a2|-a7:a8:USUAL:a2|-COMMIT_NODE|-a2|-7\n" +
                "a8|-a7:a8:USUAL:a2|-|-COMMIT_NODE|-a2|-8"
        );
    }

    @Test
    public void moreEdgesNodes() throws IOException {
        runTest(
                "1|-2 3 4\n" +
                "2|-5\n" +
                "3|-8\n" +
                "4|-6 8\n" +
                "5|-8\n" +
                "6|-8 7\n" +
                "7|-8\n" +
                "8|-"
                ,
                "1|-|-1:2:USUAL:1 1:3:USUAL:3 1:4:USUAL:4|-COMMIT_NODE|-1|-0\n" +
                "2|-1:2:USUAL:1|-2:5:USUAL:1|-COMMIT_NODE|-1|-1\n" +
                "3|-1:3:USUAL:3|-3:8:USUAL:3|-COMMIT_NODE|-3|-2\n" +
                "4|-1:4:USUAL:4|-4:6:USUAL:4 4:8:USUAL:8|-COMMIT_NODE|-4|-3\n" +
                "8|-3:8:USUAL:3 4:8:USUAL:8|-8:8:USUAL:3|-EDGE_NODE|-3|-4\n" +
                "   5|-2:5:USUAL:1|-5:8:USUAL:1|-COMMIT_NODE|-1|-4\n" +
                "8|-5:8:USUAL:1 8:8:USUAL:3|-8:8:USUAL:3|-EDGE_NODE|-3|-5\n" +
                "   6|-4:6:USUAL:4|-6:7:USUAL:7 6:8:USUAL:4|-COMMIT_NODE|-4|-5\n" +
                "8|-6:8:USUAL:4 8:8:USUAL:3|-8:8:USUAL:3|-EDGE_NODE|-3|-6\n" +
                "   7|-6:7:USUAL:7|-7:8:USUAL:7|-COMMIT_NODE|-7|-6\n" +
                "8|-7:8:USUAL:7 8:8:USUAL:3|-|-COMMIT_NODE|-3|-7"
                 );
    }

    @Test
    public void notFullLog() throws IOException  {
        runTest(
                "1|-2 3 4\n" +
                "2|-5\n" +
                "3|-8\n" +
                "4|-6 8\n" +
                "5|-8\n" +
                "6|-8 7"
                ,
                "1|-|-1:2:USUAL:1 1:3:USUAL:3 1:4:USUAL:4|-COMMIT_NODE|-1|-0\n" +
                "2|-1:2:USUAL:1|-2:5:USUAL:1|-COMMIT_NODE|-1|-1\n" +
                "3|-1:3:USUAL:3|-3:8:USUAL:3|-COMMIT_NODE|-3|-2\n" +
                "4|-1:4:USUAL:4|-4:6:USUAL:4 4:8:USUAL:8|-COMMIT_NODE|-4|-3\n" +
                "8|-3:8:USUAL:3 4:8:USUAL:8|-8:8:USUAL:3|-EDGE_NODE|-3|-4\n" +
                "   5|-2:5:USUAL:1|-5:8:USUAL:1|-COMMIT_NODE|-1|-4\n" +
                "8|-5:8:USUAL:1 8:8:USUAL:3|-8:8:USUAL:3|-EDGE_NODE|-3|-5\n" +
                "   6|-4:6:USUAL:4|-6:7:USUAL:7 6:8:USUAL:4|-COMMIT_NODE|-4|-5\n" +
                "7|-6:7:USUAL:7|-|-END_COMMIT_NODE|-7|-6\n" +
                "   8|-6:8:USUAL:4 8:8:USUAL:3|-|-END_COMMIT_NODE|-3|-6"
        );
    }

}
