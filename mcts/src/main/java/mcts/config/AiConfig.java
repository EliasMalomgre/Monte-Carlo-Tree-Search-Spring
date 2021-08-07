package mcts.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    //metrics to decide best branch with
    private double winScore;
    private double drawScore;
    private boolean useOtherMetrics;

    //settings to alter monte carlo algorithm
    private boolean useNumberOfSimulations;
    private int numberOfSimulations;
    private long simulationTime;
    private double learningRate;
    private int ongoingGame;
    private long skippedSimulation;
    private boolean useRandomActionType;
    private int draw;
    private int searchDepth;
    private boolean lessTimeFewActions;
    private long fewActionsSimulationTime;
    private int fewActions;
    private boolean onlyRandomMoves;
    private boolean useChanceNodes;
    private boolean useGroupNodes;
}