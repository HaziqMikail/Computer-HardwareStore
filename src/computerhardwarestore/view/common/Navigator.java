package computerhardwarestore.view.common;

/** What a page may ask of its shell. Keeps pages independent of the shell that hosts them. */
public interface Navigator {
    void show(String page);

    /** Re-reads the cart count shown in the navigation (customer shell only). */
    void refreshHeader();
}
