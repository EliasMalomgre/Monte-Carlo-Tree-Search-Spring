package mcts.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mcts.config.AiConfig;
import mcts.domain.Game;
import mcts.domain.SimulationResult;
import mcts.domain.ai.*;
import mcts.domain.ai.actions.Action;
import mcts.domain.ai.actions.ChanceAction;
import mcts.domain.ai.actions.GroupNode;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonteCarloService {

    private final AiConfig aiConfig;
    private final AiService aiService;
    private final UCT uct;

    /**
     * This method is used to let the ai calculate the best possible for a given game state
     *
     * @param game the game state for which you want to find the best move
     * @return the game state after you performed the best possible move
     */
    public Game findNextMove(Game game) {
        return findNextState(game, aiService.getCurrentPlayer(game)).getGame();
    }

    public SimulationResult findNextState(Game game, int playerId) {
        Tree tree = new Tree();
        int simulations = 0;
        Node rootNode = tree.getRoot();
        rootNode.getState().setGame(game);
        //TODO NOT CURRENT PLAYER!!!! When other AI's take over, they will run SimulatePlayout, and award games won by currentPlayer instead of themselves
        rootNode.getState().setPlayerNo(playerId);

        //Ask for the optimal simulation time
        SimulationTimeStruct simStruct = aiService.getSimulationTime(game);
        //If aiService has returned a negative time that means the simulation has been skipped and we can return the game
        if (simStruct.getTime() < 0) {
            return new SimulationResult(simStruct.getGame(), simStruct.getAction());
        }

        long end = System.currentTimeMillis() + simStruct.getTime();

        //start running games for allowed time
        do {
            Node promisingNode = selectPromisingNode(rootNode);
            if (aiService.getStatus(promisingNode.getState().getGame(), false) == aiConfig.getOngoingGame()) {
                expandNode(promisingNode);
            }
            Node nodeToExplore = promisingNode;

            if (promisingNode.getChildArray().size() > 0) {
                nodeToExplore = promisingNode.getRandomChildNode();
                if (nodeToExplore.isChanceNode()){
                    nodeToExplore = nodeToExplore.getChildByProbability();
                }
                if (nodeToExplore.isGroupNode()) {
                    nodeToExplore = nodeToExplore.getChildWithMaxScore();
                }
            }
            int winningPlayerId = simulateRandomPlayout(nodeToExplore);

            simulations++;

            backPropagation(nodeToExplore, winningPlayerId);

        } while (/*System.currentTimeMillis() < end*/ simulations < 10000);

        Node winnerNode = rootNode.getChildWithMaxScore();
        log.info("Game[{}]: AI[{}] simulated {} games, ran {}", game.getId(), winnerNode.getState().getPlayerNo(), simulations, winnerNode.getState().getAction());

        return new SimulationResult(new Game(winnerNode.getState().getGame()), winnerNode.getState().getAction());
    }

    /**
     * Select the best node out of the child nodes of the root node
     *
     * @param rootNode the node from whom you want to test it's child nodes
     * @return the best child node
     */
    public Node selectPromisingNode(Node rootNode) {
        Node node = rootNode;
        while (node.getChildArray().size() != 0) {
            if (node.isChanceNode()) {
                node = node.getChildByProbability();
            }
            else {
                node = uct.findBestNodeWithUCT(node);
            }
        }
        return node;
    }

    /**
     * Add all the possible child nodes to a node
     *
     * @param node the node you want to expand
     */
    public void expandNode(Node node) {
        List<State> possibleStates = aiService.getAllStates(node.getState().getGame());

        if (aiConfig.isUseGroupNodes()) {
            createGroupNodes(node, possibleStates);
        }

        if(aiConfig.isUseChanceNodes()) {
            createChanceNodes(node, possibleStates);
        }

        possibleStates.forEach(state -> {
            Node newNode = new Node(state);
            newNode.setParent(node);
            node.getChildArray().add(newNode);
        });
    }

    /**
     * This method is called to create all group nodes and filters out all the group states
     *
     * @param node              the node to expand
     * @param possibleStates    all the possible states
     */
    private void createGroupNodes(Node node, List<State> possibleStates) {
        List<Class<? extends Action>> groupActionTypes = possibleStates.stream().filter(state ->
                state.getAction() != null && state.getAction().getClass().getSuperclass() == GroupNode.class)
                .map(state -> state.getAction().getClass()).distinct().collect(Collectors.toList());

        groupActionTypes.forEach(type -> {
            List<State> typeActions = possibleStates.stream().filter(state -> state.getAction() != null &&
                    state.getAction().getClass() == type).collect(Collectors.toList());
            possibleStates.removeAll(typeActions);
            createMoveGroupNode(node, typeActions);
        });
    }

    /**
     * This methode is called to create a group node
     *
     * @param node              the node to expand
     * @param moveGroupStates   all the chance nodes of a certain type
     */
    private void createMoveGroupNode(Node node, List<State> moveGroupStates) {
        Node groupNode = new Node();
        groupNode.setParent(node);
        groupNode.setGroupNode(true);
        groupNode.getState().setPlayerNo(moveGroupStates.get(0).getPlayerNo());
        node.getChildArray().add(groupNode);

        moveGroupStates.forEach(state -> {
            Node newNode = new Node(state);
            newNode.setParent(node);
            groupNode.getChildArray().add(newNode);
        });
    }


    /**
     * This method is called to create all chance nodes and filters out all the chance states
     *
     * @param node              the node to expand
     * @param possibleStates    all the possible states
     */
    private void createChanceNodes(Node node, List<State> possibleStates) {
        List<Class<? extends Action>> chanceActionTypes = possibleStates.stream().filter(state ->
                state.getAction() != null && state.getAction().getClass().getSuperclass() == ChanceAction.class)
                .map(state -> state.getAction().getClass()).distinct().collect(Collectors.toList());

        chanceActionTypes.forEach(type -> {
            List<State> typeActions = possibleStates.stream().filter(state -> state.getAction() != null &&
                    state.getAction().getClass() == type).collect(Collectors.toList());
            possibleStates.removeAll(typeActions);
            createChanceNode(node, typeActions);
        });
    }

    /**
     * This methode is called to create a chance node
     *
     * @param node          the node to expand
     * @param chanceStates  all the chance nodes of a certain type
     */
    private void createChanceNode(Node node, List<State> chanceStates) {
        Node chanceNode = new Node();
        chanceNode.setParent(node);
        chanceNode.setChanceNode(true);
        chanceNode.getState().setPlayerNo(chanceStates.get(0).getPlayerNo());
        node.getChildArray().add(chanceNode);

        chanceStates.forEach(state -> {
            state.setProbability(((ChanceAction)state.getAction()).getProbability());
            Node newNode = new Node(state);
            newNode.setParent(node);
            chanceNode.getChildArray().add(newNode);
        });
    }

    /**
     * Update all the parent nodes from the result the simulation
     *
     * @param nodeToExplore the child node where the simulation has ended
     * @param winningPlayerNo the winner of the game
     */
    public void backPropagation(Node nodeToExplore, int winningPlayerNo) {
        Node tempNode = nodeToExplore;
        double bonusScore = aiService.calculateVirtualWins(tempNode);

        do {
            //increase visitcount for this particular node with 1
            tempNode.getState().incrementVisit();

            //give points if player won the game
            if (tempNode.getState().getPlayerNo() == winningPlayerNo) {
                tempNode.getState().addScore(aiConfig.getWinScore() + bonusScore);
            }
            else if (winningPlayerNo == aiConfig.getDraw()) {
                tempNode.getState().addScore(aiConfig.getDrawScore() + bonusScore);
            }

            //move over to parent
            tempNode = tempNode.getParent();

        } while (tempNode != null);
    }

    /**
     * Simulation of a game starting from a given node
     *
     * @param node the node from which you want to start simualting
     * @return the winner of the game
     */
    public int simulateRandomPlayout(Node node) {
        Node tempNode = new Node(node);
        State tempState = tempNode.getState();
        int boardStatus = aiService.getStatus(tempState.getGame(), false);
        int depth = aiConfig.getSearchDepth();

        while (boardStatus == aiConfig.getOngoingGame()) {
            depth--;
            tempState.setGame(aiService.randomAction(tempState.getGame()));
            tempState.setPlayerNo(aiService.getCurrentPlayer(tempState.getGame()));
            boardStatus = aiService.getStatus(tempNode.getState().getGame(), depth<=0);
        }
        return boardStatus;
    }
}
