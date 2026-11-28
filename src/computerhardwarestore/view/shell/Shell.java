package computerhardwarestore.view.shell;

import java.awt.Component;

/** A role-specific frame around the pages (navigation + content). AppWindow shows one after login. */
public interface Shell {
    Component component();

    /** Called right after login so the shell can open its landing page. */
    void start();
}
