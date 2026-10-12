package dev.limanoel.clashOneBlockRace.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrivateGamesManager {


    private final Map<String, Integer> playerCountPerPrivateMatch = new ConcurrentHashMap<>();

    public int getPlayerCount(String matchId) {
        return playerCountPerPrivateMatch.getOrDefault(matchId, 0);
    }

    public void setPlayerCount(String matchId, int count) {
        playerCountPerPrivateMatch.put(matchId, count);
    }

    public void incrementPlayerCount(String matchId) {
        playerCountPerPrivateMatch.merge(matchId, 1, Integer::sum);
    }

    public void decrementPlayerCount(String matchId) {
        playerCountPerPrivateMatch.computeIfPresent(matchId, (id, count) -> Math.max(0, count - 1));
    }

    public void removeMatch(String matchId) {
        playerCountPerPrivateMatch.remove(matchId);
    }

    public Map<String, Integer> getAllPlayerCounts() {
        return playerCountPerPrivateMatch;
    }
}