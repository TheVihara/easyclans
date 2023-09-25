package net.astrona.easyclans.gui.ui;

import net.astrona.easyclans.ClansPlugin;
import net.astrona.easyclans.controller.ClansController;
import net.astrona.easyclans.controller.LanguageController;
import net.astrona.easyclans.controller.LogController;
import net.astrona.easyclans.gui.GUI;
import net.astrona.easyclans.gui.Icon;
import net.astrona.easyclans.models.Clan;
import net.astrona.easyclans.models.Log;
import net.astrona.easyclans.models.LogType;
import net.astrona.easyclans.utils.AbstractChatUtil;
import net.astrona.easyclans.utils.Formatter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.astrona.easyclans.controller.LanguageController.getLocalizedList;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.sound.Sound.sound;

public class ClanSettingsGUI extends GUI {

    private Player player;
    private Clan clan;
    private ClansController clansController;
    private GUI previous;
    private ClansPlugin plugin;
    private LogController logController;
    public ClanSettingsGUI(Player player, Clan clan, ClansController clansController,
                           GUI previous, ClansPlugin plugin, LogController logController) {
        super(45, "title");
        this.player = player;
        this.clan = clan;
        this.clansController = clansController;
        this.previous = previous;
        this.plugin = plugin;
        this.logController = logController;

        addCloseAction((ignored) -> {
            clansController.updateClan(clan);
            if(previous != null){
                previous.open(player);
                previous.refresh(player);
            }
        });

        init();
        fancyBackground();

        open(player);

    }

    private void init(){
        setIcon(11, kickTimeIcon());
        setIcon(13, nameIcon());
        setIcon(15, joinPriceIcon());
    }


    // kick time

    private ItemStack kickTimeItem(){
        var item = new ItemStack(Material.BOOK);
        var meta = item.getItemMeta();
        meta.displayName(ClansPlugin.MM.deserialize(LanguageController.getLocalized("settings.menu.kickIcon.title")));

        var loreText = getLocalizedList("settings.menu.kickIcon.lore");
        meta.lore(loreText.stream().map(it ->
                ClansPlugin.MM.deserialize(it
                        .replace("{time}", DurationFormatUtils.formatDurationWords(clan.getAutoKickTime(), true,false))
                )
        ).toList());

        item.setItemMeta(meta);
        return item;
    }


    private Icon kickTimeIcon(){
        var icon = new Icon(kickTimeItem(), (self, player) -> {
            self.itemStack = kickTimeItem();
        });
        icon.addLeftClickAction((player1 -> {
            setForceClose(true);
            new AbstractChatUtil(player, (event) ->  {
                try{
                    int value = Integer.parseInt(event.message());
                    if(value < -1){
                        player.playSound(sound(key("block.note_block.didgeridoo"), Sound.Source.MASTER, 1f, 1.19f));
                        return;
                    }
                    logController.addLog(new Log( "kickTime:" + value, player.getUniqueId(), clan.getId(), LogType.CLAN_SETTING_CHANGED));

                    clan.setAutoKickTime(value);
                    player.playSound(sound(key("block.note_block.cow_bell"), Sound.Source.MASTER, 1f, 1.19f));
                }catch (NumberFormatException e){
                    player.playSound(sound(key("block.note_block.didgeridoo"), Sound.Source.MASTER, 1f, 1.19f));
                }
            }, plugin).setOnClose(() -> {
                setForceClose(false);
                open(player);
                refresh(player);
            });
        }));

        icon.addRightClickAction(player1 -> {
            clan.setAutoKickTime(7*24*60*60*1000); // 7 days
            player.playSound(sound(key("block.note_block.cow_bell"), Sound.Source.MASTER, 1f, 1.19f));
            refresh(player);
        });

        return icon;
    }


    // join price

    private ItemStack joinPriceItem(){
        var item = new ItemStack(Material.BOOK);

        var meta = item.getItemMeta();
        meta.displayName(ClansPlugin.MM.deserialize(LanguageController.getLocalized("settings.menu.priceIcon.title")));

        var loreText = getLocalizedList("settings.menu.priceIcon.lore");
        meta.lore(loreText.stream().map(it ->
                ClansPlugin.MM.deserialize(it
                        .replace("{price:money}", Formatter.formatMoney(clan.getJoinMoneyPrice()))
                )
        ).toList());
        item.setItemMeta(meta);

        return item;
    }


    private Icon joinPriceIcon(){
        var icon = new Icon(joinPriceItem(), (self, player) -> {
            self.itemStack = joinPriceItem();
        });

        icon.addLeftClickAction((player1 -> {
            setForceClose(true);
            new AbstractChatUtil(player, (event) ->  {
                try{
                    double value = Double.parseDouble(event.message());
                    if(value < 0){
                        player.playSound(sound(key("block.note_block.didgeridoo"), Sound.Source.MASTER, 1f, 1.19f));
                        return;
                    }
                    clan.setJoinMoneyPrice(value);
                    player.playSound(sound(key("block.note_block.cow_bell"), Sound.Source.MASTER, 1f, 1.19f));
                    logController.addLog(new Log( "joinPrice:" + value, player.getUniqueId(), clan.getId(), LogType.CLAN_SETTING_CHANGED));
                }catch (NumberFormatException e){
                    player.playSound(sound(key("block.note_block.didgeridoo"), Sound.Source.MASTER, 1f, 1.19f));
                }
            }, plugin).setOnClose(() -> {
                setForceClose(false);
                open(player);
                refresh(player);
            });
        }));


        icon.addRightClickAction(player1 -> {
            clan.setJoinMoneyPrice(10000.0);
            player.playSound(sound(key("block.note_block.cow_bell"), Sound.Source.MASTER, 1f, 1.19f));
            refresh(player);
        });


        return icon;
    }


    // name
    private ItemStack nameItem(){
        var item = new ItemStack(Material.BOOK);

        var meta = item.getItemMeta();
        meta.displayName(ClansPlugin.MM.deserialize(LanguageController.getLocalized("settings.menu.nameIcon.title")));

        var loreText = getLocalizedList("settings.menu.nameIcon.lore");
        meta.lore(loreText.stream().map(it ->
                ClansPlugin.MM.deserialize(it
                        .replace("{name}", clan.getName())
                )
        ).toList());
        item.setItemMeta(meta);

        return item;
    }


    private Icon nameIcon(){
        var icon = new Icon(nameItem(), (self, player) -> {
            self.itemStack = nameItem();
        });

        icon.addLeftClickAction((player1 -> {
            setForceClose(true);
            new AbstractChatUtil(player, (event) ->  {
                if(event.message().isBlank() || event.message().isEmpty()){
                    player.playSound(sound(key("block.note_block.didgeridoo"), Sound.Source.MASTER, 1f, 1.19f));
                    return;
                }
                clan.setName(event.message().trim().replace(" ", "_"));
                player.playSound(sound(key("block.note_block.cow_bell"), Sound.Source.MASTER, 1f, 1.19f));
                logController.addLog(new Log( "name:" + clan.getName(), player.getUniqueId(), clan.getId(), LogType.CLAN_SETTING_CHANGED));
            }, plugin).setOnClose(() -> {
                setForceClose(false);
                open(player);
                refresh(player);
            });
        }));


        icon.addRightClickAction(player1 -> {
            clan.setName("RANDOM_NAME_GENERATOR!");
            player.playSound(sound(key("block.note_block.cow_bell"), Sound.Source.MASTER, 1f, 1.19f));
            refresh(player);
        });


        return icon;
    }


    // display name



    // banner




}