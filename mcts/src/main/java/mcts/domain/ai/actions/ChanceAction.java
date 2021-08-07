package mcts.domain.ai.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mcts.domain.Game;

@Getter
@AllArgsConstructor
public class ChanceAction implements Action {
    private final double probability;

    @Override
    public Game performAction(Game game) {
        return null;
    }
}
