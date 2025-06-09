import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

public class Kort {

    int färg; // 1 = Hjärter, 2 = Spader, 3 = Ruter, 4 = Klöver
    int valör; // Värde = valör, 1 = Ess, 11 = Knekt, 12 = Dam, 13 = Kung

    public Kort(int färg, int valör) {
        this.färg = färg;
        this.valör = valör;
    }

    public void render(Graphics g, ImageObserver observer, int x, int y) {
        String path = "/bilder/" + färg + "_" + valör + ".png";
        try {
            Image bild = Toolkit.getDefaultToolkit().getImage(getClass().getResource(path));
            g.drawImage(bild, x, y, observer);
        } catch (Exception e) {
            g.drawString("Kunde inte ladda in bilden", x, y);
        }
    }

}
