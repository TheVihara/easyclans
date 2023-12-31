package net.astrona.easyclans.gui.ui;

import net.astrona.easyclans.ClansPlugin;
import net.astrona.easyclans.controller.*;
import net.astrona.easyclans.gui.GUI;
import net.astrona.easyclans.gui.Icon;
import net.astrona.easyclans.models.Clan;
import net.astrona.easyclans.models.Log;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import static net.astrona.easyclans.controller.LanguageController.getLocalizedDesiralizedList;

public class AdminClanGUI extends GUI {
    private Clan clan;
    private ClansController clansController;
    private PlayerController playerController;
    private RequestsController requestsController;
    private LogController logController;
    private CurrenciesController currenciesController;
    private ClansPlugin plugin;


    public AdminClanGUI(Player player, Clan clan, ClansController clansController, PlayerController playerController,
                        RequestsController requestsController, ClansPlugin plugin, LogController logController,
                        CurrenciesController currenciesController) {
        super(54, "Admin " + clan.getName() + " Clan");

        this.clan = clan;
        this.clansController = clansController;
        this.playerController = playerController;
        this.requestsController = requestsController;
        this.plugin = plugin;
        this.logController = logController;
        this.currenciesController = currenciesController;

        construct();
        fancyBackground();
        open(player);
    }

    private void construct() {
        setIcon(13, clanInfoIcon());
        setIcon(11, membersIcon());
        // setIcon(15, invitesIcon()); TODO
        setIcon(29, bankIcon());
        setIcon(31, clanSettingsIcon());
        setIcon(33, requestsIcon());
    }

    ItemStack clanInfoIconItem() {
        ItemStack itemStack = clan.getBanner().clone();
        var meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.displayName(ClansPlugin.MM.deserialize(LanguageController.getLocalized("clan.menu.clan_admin.name").replace("{clan}", clan.getName())));
        var loreStrings = LanguageController.getLocalizedList("clan.menu.clan_admin.lore");

        meta.lore(loreStrings.stream().map(it ->
                ClansPlugin.MM.deserialize(it
                                .replace("{clan}", String.valueOf(clan.getName()))
                                .replace("{clan_name}", String.valueOf(clan.getDisplayName()))
                                .replace("{interest_rate}", String.format("%.5f", clan.getInterestRate()))
        )).toList());

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    Icon clanInfoIcon() {
        Icon icon = new Icon(clanInfoIconItem(), (it, player) -> {
            it.itemStack = clanInfoIconItem();
        });
        icon.addClickAction((this::refresh));
        return icon;
    }

    Icon membersIcon() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        var meta = itemStack.getItemMeta();
        meta.displayName(ClansPlugin.MM.deserialize(LanguageController.getLocalized("clan.menu.members.name")));
        meta.lore(getLocalizedDesiralizedList("clan.menu.members.lore"));
        itemStack.setItemMeta(meta);
        Icon icon = new Icon(itemStack);
        icon.addClickAction((player -> {
            player.closeInventory();

            new MembersGUI(player, clan, clansController, playerController, this, logController, plugin);
        }));
        return icon;
    }

    Icon invitesIcon() {
        ItemStack itemStack = new ItemStack(Material.BOOK);
        var meta = itemStack.getItemMeta();
        meta.displayName(ClansPlugin.MM.deserialize(LanguageController.getLocalized("clan.menu.invites.name")));
        meta.lore(getLocalizedDesiralizedList("clan.menu.invites.lore"));
        itemStack.setItemMeta(meta);
        Icon icon = new Icon(itemStack);
        icon.addClickAction((player -> {
            // TODO: open the invites menu
        }));
        return icon;
    }

    ItemStack bankIconItem() {
        ItemStack itemStack = new ItemStack(Material.SUNFLOWER);
        var meta = itemStack.getItemMeta();
        meta.displayName(ClansPlugin.MM.deserialize(LanguageController.getLocalized("clan.menu.bank.name")));
        meta.lore(getLocalizedDesiralizedList("clan.menu.bank.lore"));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    Icon bankIcon() {
        Icon icon = new Icon(bankIconItem(), ((it, player) -> {
            it.itemStack = bankIconItem();
        }));

        icon.addClickAction(player -> {
            player.closeInventory();
            new CurrenciesGUI(player, clan, clansController, playerController, this, logController, plugin, currenciesController);
            //new BankGUI(player, clan, this, plugin, clansController, logController, currenciesController);
        });

        return icon;
    }

    Icon clanSettingsIcon() {
        ItemStack itemStack = new ItemStack(Material.ANVIL);
        var meta = itemStack.getItemMeta();
        meta.displayName(ClansPlugin.MM.deserialize(LanguageController.getLocalized("clan.menu.settings.name")));
        meta.lore(getLocalizedDesiralizedList("clan.menu.settings.lore"));
        itemStack.setItemMeta(meta);
        Icon icon = new Icon(itemStack);
        icon.addClickAction((player -> {
            new ClanSettingsGUI(player, clan, clansController, this, plugin, logController, currenciesController);
        }));
        return icon;
    }

    Icon requestsIcon() {
        ItemStack itemStack = new ItemStack(Material.CHEST);
        var meta = itemStack.getItemMeta();
        meta.displayName(ClansPlugin.MM.deserialize(LanguageController.getLocalized("clan.menu.requests.name")));
        meta.lore(getLocalizedDesiralizedList("clan.menu.requests.lore"));
        itemStack.setItemMeta(meta);
        Icon icon = new Icon(itemStack);
        icon.addClickAction((player -> {
            new RequestsGUI(player, clan, clansController,
                    playerController, requestsController, this, logController, plugin, currenciesController);
        }));
        return icon;
    }

}
