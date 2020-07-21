package com.gsaikiran.ping;

/**
 * @author sairepos
 */

public interface PingInterceptor {

    /**
     * The Methods marked with <code>@Ping</code> will be executed if this method returns true.
     * @return boolean
     */
    public boolean ping();
}
