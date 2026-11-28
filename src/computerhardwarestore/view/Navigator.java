package computerhardwarestore.view;

/** What a screen may ask of the main window. Keeps screens independent of MainFrame. */
public interface Navigator {
    void show(String screen);

    /** Re-reads the cart count shown in the header. */
    void refreshHeader();
}
