package net.astrona.easyclans.gui.ui;

import net.astrona.easyclans.ClansPlugin;
import net.astrona.easyclans.controller.ClansController;
import net.astrona.easyclans.controller.LanguageController;
import net.astrona.easyclans.controller.PlayerController;
import net.astrona.easyclans.controller.RequestsController;
import net.astrona.easyclans.gui.GUI;
import net.astrona.easyclans.gui.Icon;
import net.astrona.easyclans.models.Clan;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.astrona.easyclans.controller.LanguageController.getLocalizedDesiralizedList;

public class ClanGUI extends GUI {
    private Clan clan;
    private ClansController clansController;
    private PlayerController playerController;
    private RequestsController requestsController;


    public ClanGUI(Player player, Clan clan, ClansController clansController, PlayerController playerController,
                   RequestsController requestsController) {
        super(54, clan.getName() + "Clan");

        this.clan = clan;
        this.clansController = clansController;
        this.playerController = playerController;
        this.requestsController = requestsController;

        construct();
        fancyBackground();
        open(player);
    }

    private void construct() {
        setIcon(11, membersIcon());
        //  33, 31, 29

    }

    ItemStack clanInfoIconItem() {
        ItemStack itemStack = clan.getBanner().clone();
        var meta = itemStack.getItemMeta();
        meta.displayName(ClansPlugin.MM.deserialize(LanguageController.getLocalized("clan.menu.clan.name").replace("{clan}", clan.getName())));
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

            new MembersGUI(player, clan, clansController, playerController, this);
        }));
        return icon;
    }
}
