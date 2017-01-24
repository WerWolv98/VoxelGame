package com.werwolv.game.api.event.player;

import com.werwolv.game.api.event.Event;
import com.werwolv.game.entity.EntityPlayer;
import com.werwolv.game.item.Item;

public class PlayerItemUseEvent extends Event {

    private Item item;
    private EntityPlayer player;
    private Action useAction;

    public enum Action {
        LEFT_CLICK,
        RIGHT_CLICK,
        SHIFT_LEFT_CLICK,
        SHIFT_RIGHT_CLICK,
        CTRL_LEFT_CLICK,
        CTRL_RIGHT_CLICK,
        NONE
    }

    public PlayerItemUseEvent(Item item, EntityPlayer player, Action useAction) {
        super("PLAYERITEMUSEEVENT");

        this.item = item;
        this.player = player;
        this.useAction = useAction;
    }

}