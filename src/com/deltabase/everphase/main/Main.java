package com.deltabase.everphase.main;

import com.deltabase.everphase.api.EverPhaseApi;
import com.deltabase.everphase.callback.CursorPositionCallback;
import com.deltabase.everphase.callback.KeyCallback;
import com.deltabase.everphase.callback.MouseButtonCallback;
import com.deltabase.everphase.callback.ScrollCallback;
import com.deltabase.everphase.engine.audio.AudioHelper;
import com.deltabase.everphase.entity.EntityPlayer;
import com.deltabase.everphase.level.Level;
import com.deltabase.everphase.level.LevelOverworld;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.WGLEXTSwapControl.wglSwapIntervalEXT;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private static KeyCallback keyCallback;
    private static MouseButtonCallback mouseButtonCallback;
    private static CursorPositionCallback cursorPosCallback;
    private static ScrollCallback scrollCallback;

    private static Level currentLevel;
    private static long lastFrameTime;
    private static float delta;
    private static long window;
    private static EntityPlayer player;

    public static void main(String[] args) {
        for (String arg : args) {
            switch (arg.toLowerCase()) {
                case "fullscreen":
                    Settings.fullscreen = true;
                    System.out.println("[Settings] Starting in fullscreen mode");
                    break;
                case "vsync":
                    Settings.vSync = true;
                    System.out.println("[Settings] VSync enabled");
                    break;
                case "antialiasing":
                    Settings.antialiasing = true;
                    System.out.println("[Settings] Anti aliasing enabled");
                    break;
                case "anisotropicfilter":
                    Settings.anisotropicFilter = true;
                    System.out.println("[Settings] Anisotropic filtering enabled");
                    break;
                case "bloom":
                    Settings.bloom = true;
                    System.out.println("[Settings] Bloom filter enabled");
                    break;
            }

            if (arg.toLowerCase().startsWith("shadowquality")) {
                Settings.shadowQuality = (int) Math.pow(2, Integer.parseInt(arg.split("_")[1]));

                switch (Settings.shadowQuality) {
                    case 0:
                        System.out.println("[Settings] Dynamic shadows disabled");
                        break;
                    case 1:
                        System.out.println("[Settings] Low quality dynamic shadows enabled");
                        break;
                    case 2:
                        System.out.println("[Settings] Medium quality dynamic shadows enabled");
                        break;
                    case 4:
                        System.out.println("[Settings] High quality dynamic shadows enabled");
                        break;
                    case 8:
                        System.out.println("[Settings] Ultra High quality dynamic shadows enabled");
                        break;
                }
            }

            else if(arg.toLowerCase().startsWith("mipmaplevel"))
                Settings.mipmappingLevel = Integer.parseInt(arg.split("_")[1]);
            else if(arg.toLowerCase().startsWith("mipmaptype")) {
                switch (Integer.parseInt(arg.split("_")[1])) {
                    case 1:
                        Settings.mipmappingType = GL_LINEAR;
                        System.out.println("[Settings] Linear mipmapping enabled");
                        break;
                    case 2:
                        Settings.mipmappingType = GL_LINEAR_MIPMAP_NEAREST;
                        System.out.println("[Settings] Bilinear mipmapping enabled");
                        break;
                    case 3:
                        Settings.mipmappingType = GL_LINEAR_MIPMAP_LINEAR;
                        System.out.println("[Settings] Trilinear mipmapping enabled");
                        break;
                    default:
                        Settings.mipmappingType = GL_NONE;
                        System.out.println("[Settings] Mipmapping disabled");
                        break;
                }
            }
        }

        run();
    }

    private static void init() {
        if(!glfwInit()){
            System.err.println("[GLFW] Initialization failed!");
        }

        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());


        window = glfwCreateWindow(Settings.fullscreen ? vidmode.width() : 1080, Settings.fullscreen ? vidmode.height() : 720, "EverPhase", Settings.fullscreen ? glfwGetPrimaryMonitor() : NULL, NULL);

        if(window == NULL) {
            System.err.println("[GLFW] Could not create window!");
        }

        glfwSetKeyCallback(window, keyCallback = new KeyCallback());
        glfwSetMouseButtonCallback(window, mouseButtonCallback = new MouseButtonCallback());
        glfwSetCursorPosCallback(window, cursorPosCallback = new CursorPositionCallback());
        glfwSetScrollCallback(window, scrollCallback = new ScrollCallback());
        glfwSetWindowShouldClose(window, false);

        glfwSetWindowSizeCallback(window, (window, width, height) -> {
            GL11.glViewport(0, 0, width, height);
            glfwSetWindowSize(window, width, height);
        });

        glfwSetWindowPos(window, 100, 100);
        glfwMakeContextCurrent(window);

        glfwShowWindow(window);

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);

        if(Settings.antialiasing)
            glEnable(GL13.GL_MULTISAMPLE);

        if(Settings.vSync)
            wglSwapIntervalEXT(1);

        lastFrameTime = getCurrentTime();
        setCursorVisibility(false);
        EverPhaseApi.EVENT_BUS.registerEventHandlers();

        System.out.println("[OpenGL] Version:" + glGetString(GL_VERSION));

        AudioHelper.createContext();
        AudioHelper.loadSoundFile("random");
    }

    public static void setCursorVisibility(boolean visible) {
        glfwSetInputMode(window, GLFW_CURSOR, visible ? GLFW_CURSOR_NORMAL : GLFW_CURSOR_DISABLED);
    }

    private static void run() {
        init();

        player = new EntityPlayer(new Vector3f(0, 10, 0), new Vector3f(0, 0, 0), 1);

        currentLevel = new LevelOverworld(player);

        currentLevel.initLevel();

        currentLevel.applyPostProcessingEffects();

        while(true) {
            glfwSwapBuffers(window);

            AudioHelper.setListener(player);
            EverPhaseApi.EVENT_BUS.processEvents();
            currentLevel.updateLevel();
            currentLevel.handleInput();
            currentLevel.renderLevel();
            currentLevel.renderGUI();

            long currFrameTime = getCurrentTime();
            delta = (currFrameTime - lastFrameTime) / 1000.0F;
            lastFrameTime = currFrameTime;

            if(glfwWindowShouldClose(window)) {
                System.out.println("[EverPhase] Window closed");
                break;
            }
        }

        glfwSetWindowShouldClose(window, false);
        glfwDestroyWindow(window);
        currentLevel.clean();
        keyCallback.free();
        mouseButtonCallback.free();
        cursorPosCallback.free();
    }


    public static long getWindow() {
        return window;
    }

    public static int[] getWindowSize() {
        IntBuffer w = BufferUtils.createIntBuffer(4);
        IntBuffer h = BufferUtils.createIntBuffer(4);

        glfwGetWindowSize(window, w, h);

        return new int[]{w.get(0), h.get(0)};
    }

    public static float getAspectRatio() {
        return (float) getWindowSize()[0] / (float) getWindowSize()[1];
    }

    private static long getCurrentTime() {
        return (long) (GLFW.glfwGetTime() * 1000);
    }

    public static float getFrameTimeSeconds() {
        return delta;
    }

    public static EntityPlayer getPlayer() {
        return player;
    }
}
