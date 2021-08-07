package mcts.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import mcts.domain.ai.actions.Action;

@Getter
@Setter
@AllArgsConstructor
public class SimulationResult {
    private Game game;
    private Action action;
}
