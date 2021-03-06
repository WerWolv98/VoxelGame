package com.deltabase.everphase.entity;

import com.deltabase.everphase.api.EverPhaseApi;
import com.deltabase.everphase.api.event.player.OpenGuiEvent;
import com.deltabase.everphase.api.event.player.PlayerItemUseEvent;
import com.deltabase.everphase.api.event.player.PlayerMoveEvent;
import com.deltabase.everphase.callback.CursorPositionCallback;
import com.deltabase.everphase.callback.KeyCallback;
import com.deltabase.everphase.callback.MouseButtonCallback;
import com.deltabase.everphase.collision.AABB;
import com.deltabase.everphase.data.PlayerData;
import com.deltabase.everphase.gui.Gui;
import com.deltabase.everphase.inventory.InventoryPlayer;
import com.deltabase.everphase.item.ItemStack;
import com.deltabase.everphase.main.Main;
import com.deltabase.everphase.terrain.Terrain;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class EntityPlayer extends Entity {

    protected static final float PLAYER_HEIGHT = 6.0F;    //Height of the player to render the camera above the ground.
    private final PlayerData playerData = (PlayerData) new PlayerData().deserialize();
    private InventoryPlayer inventoryPlayer = new InventoryPlayer(28, 99);
    private int selectedItemIndex = 0;
    private float speedX, speedY, speedZ;               //Speed of the player in different directions.
    private boolean isInAir = false;                    //Is the player currently in the air?
    private boolean canFly = false;
    private Gui currentGui;
    private ItemStack heldItem;

    public EntityPlayer(Vector3f rotation, float scale) {
        super("", "", rotation, scale, new Vector3f(2.5F, 2.5F, 2.5F), false);
    }

    @Override
    public void update() {
        super.update();
        this.boundingBox = new AABB(new Vector3f(position.x, position.y - PLAYER_HEIGHT, position.z), bbSize.mul(scale));
    }


    public void onMove(Terrain terrain) {

        speedX = speedZ = 0.0F;

        speedY += canFly ? -speedY : GRAVITY * Main.getFrameTimeSeconds();      //Add gravity to the player to keep it on the ground

        //Keybindings to move the player in different directions based of the pressed buttons and the direction of the camera
        if (this.getCurrentGui() == null) {
            if (KeyCallback.isKeyPressed(GLFW_KEY_W)) {
                speedX += SPEED.getValue() * (float) Math.sin(Math.toRadians(getYaw()));
                speedZ -= SPEED.getValue() * (float) Math.cos(Math.toRadians(getYaw()));
            }
            if (KeyCallback.isKeyPressed(GLFW_KEY_S)) {
                speedX -= SPEED.getValue() * (float) Math.sin(Math.toRadians(getYaw()));
                speedZ += SPEED.getValue() * (float) Math.cos(Math.toRadians(getYaw()));
            }
            if (KeyCallback.isKeyPressed(GLFW_KEY_A)) {
                speedX -= SPEED.getValue() * (float) Math.cos(Math.toRadians(getYaw()));
                speedZ -= SPEED.getValue() * (float) Math.sin(Math.toRadians(getYaw()));
            }
            if (KeyCallback.isKeyPressed(GLFW_KEY_D)) {
                speedX += SPEED.getValue() * (float) Math.cos(Math.toRadians(getYaw()));
                speedZ += SPEED.getValue() * (float) Math.sin(Math.toRadians(getYaw()));
            }

            if (canFly) {
                if (KeyCallback.isKeyPressed(GLFW_KEY_SPACE))
                    speedY += 40F;

                if (KeyCallback.isKeyPressed(GLFW_KEY_LEFT_SHIFT))
                    speedY -= 40F;
            } else {
                if (KeyCallback.isKeyPressed(GLFW_KEY_SPACE) && !isInAir) {
                    speedY += 30F;
                    isInAir = true;
                }
            }
        }

        EverPhaseApi.EVENT_BUS.postEvent(new PlayerMoveEvent(this, position, new Vector3f(speedX, speedY, speedZ), new Vector3f(pitch, yaw, roll)));

        float terrainHeight = terrain.getHeightOfTerrain(position.x, position.z) + PLAYER_HEIGHT;   //Get the height of the terrain at the current position of the player
        if (this.position.y < terrainHeight) {        //If the player is under the terrain plane...
            speedY = 0;                         //...reset the downwards speed
            isInAir = false;                    //...set the player not in the air anymore
            setPosition(new Vector3f(getPosition().x, terrainHeight, getPosition().z));         //...and set the y position of the player to the height of the the terrain
        }
    }

    public void onInteract() {
        if (this.getCurrentGui() != null) return;

        PlayerItemUseEvent.Action currAction = PlayerItemUseEvent.Action.NONE;
        if(MouseButtonCallback.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            if(KeyCallback.isKeyPressed(GLFW_MOD_SHIFT)) currAction = PlayerItemUseEvent.Action.SHIFT_LEFT_CLICK;
            else if(KeyCallback.isKeyPressed(GLFW_MOD_CONTROL)) currAction = PlayerItemUseEvent.Action.CTRL_LEFT_CLICK;
            else currAction = PlayerItemUseEvent.Action.LEFT_CLICK;
        } else if(MouseButtonCallback.isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
            if(KeyCallback.isKeyPressed(GLFW_MOD_SHIFT)) currAction = PlayerItemUseEvent.Action.SHIFT_RIGHT_CLICK;
            else if(KeyCallback.isKeyPressed(GLFW_MOD_CONTROL)) currAction = PlayerItemUseEvent.Action.CTRL_RIGHT_CLICK;
            else currAction = PlayerItemUseEvent.Action.RIGHT_CLICK;
        }

        if(this.getHeldItem() != null && currAction != PlayerItemUseEvent.Action.NONE)
            this.heldItem = this.getHeldItem().getItem().onItemClick(this.getHeldItem(), this, currAction);

    }

    public void pickUpItem(Entity entityItem) {
        /*EverPhaseApi.EVENT_BUS.postEvent(new ItemPickupEvent(entityItem));

        this.inventoryPlayer.addItemStackToInventory(entityItem.getItemStack());*/
        entityItem.setDead();
    }

    /* Getters and Setters */

    public void addPosition(float x, float y, float z) {
        this.position.x += x * Main.getFrameTimeSeconds();
        this.position.y += y * Main.getFrameTimeSeconds();
        this.position.z += z * Main.getFrameTimeSeconds();

    }

    public Gui getCurrentGui() {
        return currentGui;
    }

    public void setCurrentGui(Gui currentGui) {
        if (currentGui != null) {
            EverPhaseApi.EVENT_BUS.postEvent(new OpenGuiEvent(this, currentGui));
            Main.setCursorVisibility(true);
            CursorPositionCallback.enableCursorListener(false);
        } else {
            Main.setCursorVisibility(false);
            CursorPositionCallback.enableCursorListener(true);
        }

        this.currentGui = currentGui;
    }

    public ItemStack getHeldItem() {
        return this.heldItem;
    }

    public int getSelectedItemIndex() {
        return selectedItemIndex;
    }

    public void setSelectedItemIndex(int index) {
        this.selectedItemIndex = index;
    }

    public void toggleFlight() {
        canFly = !canFly;
    }

    public InventoryPlayer getInventoryPlayer() {
        return this.inventoryPlayer;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }
}
