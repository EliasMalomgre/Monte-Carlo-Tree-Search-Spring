package mcts.domain.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mcts.domain.Game;
import mcts.domain.ai.actions.Action;

@Getter
@AllArgsConstructor
public class SimulationTimeStruct {
    private final Long time;
    private final Game game;
    private final Action action;
}
