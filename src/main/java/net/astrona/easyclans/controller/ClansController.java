package net.astrona.easyclans.controller;

import net.astrona.easyclans.ClansPlugin;
import net.astrona.easyclans.models.CPlayer;
import net.astrona.easyclans.models.Clan;
import net.astrona.easyclans.models.Log;
import net.astrona.easyclans.models.LogType;
import net.astrona.easyclans.storage.SQLStorage;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ClansController {
    private PlayerController playerController;
    private final Map<Integer, Clan> clans;
    private final ClansPlugin plugin;
    private final SQLStorage sqlStorage;

    public ClansController(ClansPlugin plugin, SQLStorage sqlStorage, PlayerController playerController) {
        this.plugin = plugin;
        this.sqlStorage = sqlStorage;
        this.playerController = playerController;
        this.clans = new HashMap<>();

        loadClans();
    }

    private void loadClans() {
        for (var clan : sqlStorage.getAllClans()) {
            this.clans.put(clan.getId(), clan);

        }
    }


    private void addClan(Clan clan) {
        clans.put(clan.getId(), clan);
    }

    /**
     * Adds a new clan to the clan list.
     *
     * @param owner                the UUID of the player who owns the clan.
     * @param name                 the name of the clan.
     * @param displayName          the display name of the clan.
     * @param autoKickTime         the auto-kick time for inactive members.
     * @param joinPointsPrice      the points price for joining the clan.
     * @param joinMoneyPrice       the money price for joining the clan.
     * @param banner               the banner item for the clan.
     * @param bank                 the initial bank balance of the clan.
     * @param interestRate         the interest rate
     * @param tag                  the tag associated with the clan.
     * @param members              the list of UUIDs of clan members.
     */
    public Clan createClan(UUID owner, String name, String displayName, int autoKickTime,
                           int joinPointsPrice, double joinMoneyPrice,
                           ItemStack banner, double bank, double interestRate, String tag, List<UUID> members) {

        Clan clan = new Clan(-1, owner, name, displayName, autoKickTime, joinPointsPrice, joinMoneyPrice,
                banner, bank, interestRate, tag, members, System.currentTimeMillis());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            sqlStorage.saveClan(clan); // clan gets new ID when this is executed.
            // TODO: update player sent new clan id.
            var player = playerController.getPlayer(clan.getOwner());
            player.setClanID(clan.getId());
            playerController.updatePlayer(player);
            addClan(clan);
        });
        return clan;
    }

    /**
     * Retrieves a clan object by its id.
     *
     * @param id the id of the clan you want to retrieve.
     * @return the clan object associated with the provided id, or null if
     *         no clan with the given id is found.
     */
    public Clan getClan(int id) {
        if (clans.containsKey(id))
            return clans.get(id);
        return null;
    }

    public void updateClan(Clan clan){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            sqlStorage.updateClan(clan);
        });

    }

    public List<Clan> getClans() {
        return clans.values().stream().toList();
    }


    public void processClans(){


        for(var player : playerController.getPlayers()){
            if(player.getClanID() == -1) continue;
            var clan = clans.get(player.getClanID());

            // add interest rate based on players rank.
            double interestToAdd = plugin.getConfig().getDouble("rank_interest_value." + player.getRank());
            if(!Double.isNaN(interestToAdd) && interestToAdd > 0){
                var currentInterestRate = clan.getInterestRate();
                currentInterestRate += interestToAdd;

                if(currentInterestRate > plugin.getConfig().getDouble("clan.max_interest_rate"))
                    currentInterestRate = plugin.getConfig().getDouble("clan.max_interest_rate");

                sqlStorage.addLog(new Log(String.valueOf(currentInterestRate), null, clan.getId(), LogType.INTEREST_ADD));
                clan.setInterestRate(currentInterestRate);
            }


            if(clan.getOwner().equals(player.getUuid())) continue;
            if(System.currentTimeMillis() - player.getLastActive() > clan.getAutoKickTime()){
                player.setClanID(-1);
                // TODO: send kick notification
                sqlStorage.addLog(new Log("", player.getUuid(), clan.getId(), LogType.AUTO_KICK));
                sqlStorage.updatePlayer(player);
            }
        }


        // interest update blabla
        for(var clan : clans.values()){
            var cOwner = playerController.getPlayer(clan.getOwner());
            if(System.currentTimeMillis() - cOwner.getLastActive() > 7 * 24 * 60 * 60 * 1000){
                sqlStorage.addLog(new Log(String.valueOf(clan.getInterestRate()), null, clan.getId(), LogType.INTEREST_RESET));
                clan.setInterestRate(0);
            }/*else{
                var currentInterestRate = clan.getInterestRate();
                currentInterestRate += plugin.getConfig().getDouble("clan.interest_rate_increase");

                if(currentInterestRate > plugin.getConfig().getDouble("clan.max_interest_rate"))
                    currentInterestRate = plugin.getConfig().getDouble("clan.max_interest_rate");

                sqlStorage.addLog(new Log(String.valueOf(currentInterestRate), null, clan.getId(), LogType.INTEREST_ADD));
                clan.setInterestRate(currentInterestRate);
            }
            if(clan.getBank() != 0 && clan.getInterestRate() != 0){
                var addMoney = clan.getBank() * clan.getInterestRate();
                clan.setBank(clan.getBank() + addMoney);
                sqlStorage.addLog(new Log(String.valueOf(addMoney), null, clan.getId(), LogType.MONEY_ADD));
            }*/



            sqlStorage.updateClan(clan);
        }
    }
}
