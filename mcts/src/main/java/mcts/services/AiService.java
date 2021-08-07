package mcts.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mcts.config.AiConfig;
import mcts.domain.Game;
import mcts.domain.ai.Node;
import mcts.domain.ai.SimulationTimeStruct;
import mcts.domain.ai.State;
import mcts.domain.ai.actions.Action;
import mcts.domain.ai.actions.ChanceAction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiConfig aiConfig;


    /**
     * This method is called to determine the ideal simulation time. When there is only 1 possible action the game will
     * executes this action and return the next game state. When the move has been skipped, a negative number
     * will be returned
     *
     * @param game the game that has to be simulated
     * @return a pair of a time and a game state
     */
    public SimulationTimeStruct getSimulationTime(Game game) {
        List<Action> actions = getLegalActions(game);

        int currentPlayerAtStartSimulation = getCurrentPlayer(game);

        //Get all the types of actions
        List<Class<? extends Action>> actionTypes = actions.stream().map(Action::getClass).distinct().collect(Collectors.toList());

        //If there only have to be played random moves
        if (aiConfig.isOnlyRandomMoves()) {
            //TODO: Turn all the chance actions in normal actions

            //Select a random action and execute it
            Action randomAction = chooseRandomAction(actions);
            log.info("Game[{}]: AI[{}] did random move, ran {}", game.getId(), currentPlayerAtStartSimulation, randomAction);
            game = getNextState(game, randomAction);
            return new SimulationTimeStruct(aiConfig.getSkippedSimulation(), game, randomAction);
        }

        //If the only type of action is a ChanceRollDiceAction convert it to a normal dice roll action and execute it
        if (actionTypes.size() == 1 && actionTypes.get(0).getSuperclass() == ChanceAction.class) {
            //TODO: Turn all the chance actions in normal actions
        }

        //If there is only 1 action execute it
        if (actions.size() == 1) {
            Action action = actions.get(0);
            game = getNextState(game, action);
            log.info("Game[{}]: AI[{}] skipped simulation, ran {}", game.getId(), currentPlayerAtStartSimulation, action);
            return new SimulationTimeStruct(aiConfig.getSkippedSimulation(), game, action);
        }

        //If the number of actions is fewer than the setting return the reduced simulation time
        if (aiConfig.isLessTimeFewActions() && actions.size() <= aiConfig.getFewActions()) {
            return new SimulationTimeStruct(aiConfig.getFewActionsSimulationTime(), game, null);
        }

        //else return the default simulation time
        return new SimulationTimeStruct(aiConfig.getSimulationTime(), game, null);
    }

    /**
     * Calculate all the possible state you can reach from a given game state
     *
     * @param game the game state from which you want to start
     * @return all the possible states
     */
    public List<State> getAllStates(Game game) {
        int playerId = getCurrentPlayer(game);
        List<Action> actions = getLegalActions(game);

        return actions.parallelStream().map(action -> new State(getNextState(game, action), playerId, action)).collect(Collectors.toList());
    }

    /**
     * get the next game state after performing an action to a game state
     *
     * @param game   the game state on which you want to perform an action
     * @param action the action you want to perform on the game state
     * @return the next game state after performing the action
     */
    public Game getNextState(Game game, Action action) {
        return new Game(action.performAction(new Game(game)));
    }

    /**
     * Get the current player of a game state
     *
     * @param game the game state from whom you want to get the current player
     * @return the current player
     */
    public int getCurrentPlayer(Game game) {
        //TODO: return the player which has to act
        return 0;
    }

    /**
     * Perform a random action on a game state
     *
     * @param game the start game state
     * @return the next game state after performing the action
     */
    public Game randomAction(Game game) {
        List<Action> actions = getLegalActions(game);
        if (actions.isEmpty()) {
            return game;
        }
        return getNextState(game, chooseRandomAction(actions));
    }

    /**
     * This method selects a random action out of list of actions
     *
     * @param actions the actions from whom a random action has to be picked
     * @return a random action
     */
    public Action chooseRandomAction(List<Action> actions) {
        //TODO: you can also implement something different where certain actions have different probabilities of being chosen
        Random random = new Random();

        //Rather than giving every action the same chance of being picked give each action type the same chance of being picked
        if (aiConfig.isUseRandomActionType()) {
            //Get all the classes from all the action types
            List<Class<? extends Action>> actionTypes = actions.stream().map(Action::getClass).distinct().collect(Collectors.toList());
            //Choose a random class
            Class<? extends Action> actionType = actionTypes.get(random.nextInt(actionTypes.size()));
            //Filter actions on this class
            actions = actions.stream().filter(action -> action.getClass() == actionType).collect(Collectors.toList());
        }

        int randomAction = random.nextInt(actions.size());

        return actions.get(randomAction);
    }

    /**
     * Get the status of a game state
     * aiConfig.getOngoingGame() = ongoing game
     * other number = the player who won the game
     * ai.config.getDraw() = game ended in a draw
     *
     * @param game the game state you want to check
     * @return the status of the game
     */
    public int getStatus(Game game, boolean stoppedSimulation) {
        //TODO: return 'aiConfig.getOngoingGame()' when game is ongoing and 'stoppedSimulation' is false
        //TODO: return the number of the player that won when the game has finished
        //TODO: return 'ai.config.getDraw()' when the game ended in a draw
        return 0;
    }

    /**
     * Adds domain logic to the calculations by awarding extra points fo doing certain things
     * <p>
     * This is only executed if 'useOtherMetrics' is set to true
     *
     * @param node this should be the last node of the branch, the one on which backpropagation starts
     * @return the score gotten upon reaching the end of the branch
     */
    public double calculateVirtualWins(Node node) {

        double virtualScore = 0.0;

        if (aiConfig.isUseOtherMetrics() && node.getState().getGame() != null) {
            //TODO: implement points awarding logic
        }


        return virtualScore;
    }

    /**
     * Calculates all the possible action for a player from a given game state
     *
     * @param game the game state from which you want to start
     * @return a list of all the possible actions
     */
    public List<Action> getLegalActions(Game game) {
        List<Action> legalActions = new ArrayList<>();

        //TODO: implement creating an Action object for all possible actions

        return legalActions;
    }

}
