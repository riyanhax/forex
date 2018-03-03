package live;

import org.springframework.stereotype.Service;

@Service
class LiveWatcher {
    public void run() {
        System.out.println("*** Ran live watcher");
    }
}
