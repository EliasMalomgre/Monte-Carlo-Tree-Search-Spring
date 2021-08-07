package mcts.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Game {

    String id;

    public Game(){}

    public Game(Game game) {
        this.id = game.getId();
    }
}
