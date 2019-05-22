package logic;

import interfaces.UITask;
import javafx.application.Platform;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

public class EDTInvocationHandler implements InvocationHandler {

    private Object invocationResult = null;
    private UITask UI;

    public EDTInvocationHandler(UITask UI) {
        this.UI = UI;
    }

    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) throws
            InvocationTargetException, IllegalAccessException, ExecutionException, InterruptedException {
        if (Platform.isFxApplicationThread()) {
            invocationResult = method.invoke(UI, args);
        } else {
            FXUtilities.runAndWait(() -> {
                try {
                    invocationResult = method.invoke(UI, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        }
        return invocationResult;
    }

}