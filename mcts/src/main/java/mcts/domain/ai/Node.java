package mcts.domain.ai;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Getter
@Setter
public class Node {
    private State state;
    private Node parent;
    private List<Node> childArray;
    private boolean chanceNode;
    private boolean groupNode;

    public Node() {
        this.childArray = new ArrayList<>();
        this.state = new State();
    }

    public Node(Node node) {
        this.state = new State(node.getState());
        this.childArray = node.getChildArray();
        this.parent = node.getParent();
        this.chanceNode = node.isChanceNode();

    }

    public Node(State state) {
        this.state = state;
        this.childArray = new ArrayList<>();
    }

    /**
     * A method for selecting a random node out of all the child nodes
     *
     * @return a random node
     */
    public Node getRandomChildNode() {
        Random random = new Random();
        return childArray.get(random.nextInt(childArray.size()));
    }

    /**
     * A method which is used to select the node with the highest score out of all the child nodes
     *
     * @return the node with highest score
     */
    public Node getChildWithMaxScore() {
        Node winningNode = childArray.stream().max(Comparator.comparing(node -> node.state.getWinScore()/node.state.getVisitCount()))
                    .orElseThrow(() -> new IllegalArgumentException("No nodes were found when selecting the child with max score"));
        if (winningNode.isChanceNode()) {
            winningNode = winningNode.getChildByProbability();
        }
        if (winningNode.isGroupNode()) {
            winningNode = winningNode.getChildWithMaxScore();
        }
        return winningNode;
    }

    /**
     * A method for chance nodes to choose th child by probability
     *
     * @return a node by probability
     */
    public Node getChildByProbability() {
        if (!chanceNode) {
            throw new IllegalArgumentException("Tried to get child by probability but node is not a chance node");
        }
        Random random = new Random();
        double randomDouble = random.nextDouble();
        double cumulativeProbability = 0.0;
        for (Node child : childArray) {
            cumulativeProbability += child.getState().getProbability();
            if (randomDouble <= cumulativeProbability) {
                return child;
            }
        }
        throw new IllegalArgumentException("Tried to get child by probability but probability doesn't add up to 100%");
    }
}