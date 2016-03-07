package observers;

public interface IRCObserver {

    void ircMessageReceived(IRCObservable o, String channel, String name, String message);

}
