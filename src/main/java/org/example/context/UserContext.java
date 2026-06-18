package org.example.context;

import org.example.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserContext {
    private final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public void setCurrentUser(User user) {
        currentUser.set(user);
    }

    public User getCurrentUser() {
        return currentUser.get();
    }

    public void clear() {
        currentUser.remove();
    }

    public boolean isAuthenticated() {
        return currentUser.get() != null;
    }
}