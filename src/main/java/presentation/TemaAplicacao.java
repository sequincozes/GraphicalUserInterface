package presentation;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import javax.swing.UIManager;
import java.awt.Color;
public final class TemaAplicacao {
    private TemaAplicacao() { }
    public static void configurar() {
        FlatMacLightLaf.setup();
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Component.focusColor", Color.decode("#005C43"));
        UIManager.put("Table.rowHeight", 38);
        UIManager.put("Table.selectionBackground", Color.decode("#EBF5F0"));
        UIManager.put("Table.selectionForeground", Color.decode("#141A17"));
    }
}
