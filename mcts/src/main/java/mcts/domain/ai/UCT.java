package mcts.domain.ai;

import lombok.RequiredArgsConstructor;
import mcts.config.AiConfig;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class UCT {

    private final AiConfig aiConfig;

    /**
     * This method is used to calculate a value, which is used to select the best node to use, for a node.
     * When a node hasn't been visited yet, it is given the highest score possible so it will be selected first
     *
     * @param totalVisit the total amount of times a node has been visited
     * @param nodeWinScore the score the node has been given
     * @param nodeVisit the total amount of times this node has been visited
     * @return a score which is used to select the best node
     */
    public double uctValue(int totalVisit, double nodeWinScore, int nodeVisit) {
        if (nodeVisit == 0) {
            return Integer.MAX_VALUE;
        }
        return (nodeWinScore / (double) nodeVisit)
                + aiConfig.getLearningRate() * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);
    }

    /**
     * This method is used to find the best node to be used
     *
     * @param node the parent node from whom you want to tess the child nodes
     * @return the node with the highest uct value
     */
    public Node findBestNodeWithUCT(Node node) {
        int parentVisit = node.getState().getVisitCount();
        return Collections.max(
                node.getChildArray(),
                Comparator.comparing(c -> uctValue(parentVisit,
                        c.getState().getWinScore(), c.getState().getVisitCount())));
    }
}
