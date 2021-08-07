package mcts.domain.ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tree {
    Node root;

    public Tree() {
        root = new Node(new State());
    }
}