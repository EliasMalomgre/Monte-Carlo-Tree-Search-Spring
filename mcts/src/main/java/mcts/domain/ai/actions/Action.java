package mcts.domain.ai.actions;


import mcts.domain.Game;

/**
 * An action for the ai to perform
 */
public interface Action {

    /**
     * To logic to be able to perform an action
     *
     * @param game  the start game state
     * @return the game state after performing the action
     */
    Game performAction(Game game);

}
