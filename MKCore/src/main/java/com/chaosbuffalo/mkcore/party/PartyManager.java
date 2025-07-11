package com.chaosbuffalo.mkcore.party;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class PartyManager {

    private static PlayerTeam getTeam(MinecraftServer server, String name) {
        PlayerTeam team = server.getScoreboard().getPlayerTeam(name);
        if (team == null) {
            team = server.getScoreboard().addPlayerTeam(name);
        }
        team.setAllowFriendlyFire(false);
        team.setDisplayName(Component.translatable("mk.core.party.name", name));
        return team;
    }

    public static PlayerTeam getTeamForPlayer(MinecraftServer server, Player player) {
        String name = player.getScoreboardName();
        return getTeam(server, name);
    }

    public static void handleInviteAccept(MinecraftServer server, Player invitingPlayer, Player acceptingPlayer) {
        Scoreboard board = server.getScoreboard();
        PlayerTeam invitingTeam = invitingPlayer.getTeam();
        if (invitingTeam == null) {
            invitingTeam = getTeamForPlayer(server, invitingPlayer);
            board.addPlayerToTeam(invitingPlayer.getScoreboardName(), invitingTeam);
        }
        board.addPlayerToTeam(acceptingPlayer.getScoreboardName(), invitingTeam);
    }

    public static void removePlayerFromParty(MinecraftServer server, Player player) {
        Scoreboard board = server.getScoreboard();
        PlayerTeam leavingTeam = player.getTeam();
        board.removePlayerFromTeam(player.getScoreboardName());
        // If the team is now empty destroy it
        if (leavingTeam != null && leavingTeam.getPlayers().isEmpty()) {
            board.removePlayerTeam(leavingTeam);
        }
    }

}
