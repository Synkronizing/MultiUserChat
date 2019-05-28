package com.muc;

/**
 * Layout for online and offline methods
 */
public interface UserStatusListener {
    public void online(String login);
    public void offline(String login);
}
