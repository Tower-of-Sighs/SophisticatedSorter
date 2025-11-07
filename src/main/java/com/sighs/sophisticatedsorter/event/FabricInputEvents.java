package com.sighs.sophisticatedsorter.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class FabricInputEvents {
    private FabricInputEvents() {
    }

    // MouseButton.Pre：返回 true 表示取消
    public interface MouseButtonPreCallback {
        boolean onMouseButtonPre(InputEvent.MouseButton.Pre event);
    }

    // MouseButton.Post：不可取消
    public interface MouseButtonPostCallback {
        void onMouseButtonPost(InputEvent.MouseButton.Post event);
    }

    // Key：不可取消
    public interface KeyCallback {
        void onKey(InputEvent.Key event);
    }

    // MouseScrollingEvent：返回 true 表示取消
    public interface MouseScrollCallback {
        boolean onMouseScroll(InputEvent.MouseScrollingEvent event);
    }

    public static final Event<MouseButtonPreCallback> MOUSE_BUTTON_PRE =
            EventFactory.createArrayBacked(MouseButtonPreCallback.class, callbacks -> event -> {
                for (MouseButtonPreCallback cb : callbacks) {
                    if (cb.onMouseButtonPre(event)) {
                        return true;
                    }
                }
                return false;
            });

    public static final Event<MouseButtonPostCallback> MOUSE_BUTTON_POST =
            EventFactory.createArrayBacked(MouseButtonPostCallback.class, callbacks -> event -> {
                for (MouseButtonPostCallback cb : callbacks) {
                    cb.onMouseButtonPost(event);
                }
            });

    public static final Event<KeyCallback> KEY =
            EventFactory.createArrayBacked(KeyCallback.class, callbacks -> event -> {
                for (KeyCallback cb : callbacks) {
                    cb.onKey(event);
                }
            });

    public static final Event<MouseScrollCallback> MOUSE_SCROLL =
            EventFactory.createArrayBacked(MouseScrollCallback.class, callbacks -> event -> {
                for (MouseScrollCallback cb : callbacks) {
                    if (cb.onMouseScroll(event)) {
                        return true;
                    }
                }
                return false;
            });
}