import java.awt.Graphics;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

public class Spelare {
    
    ArrayList<Kort> hand = new ArrayList<>();

    String namn;
    int pengar = 500;
    int poäng = 0;
    int bet;

    public Spelare(String namn) {
        this.namn = namn;
    }

    public void fårKort(Kort kort) {
        hand.add(kort);
        if (kort.valör == 1) {
            if (poäng > 10) {
                poäng++;
            } else if (poäng <= 10) {
                poäng += 11;
            }
        } else if (kort.valör <= 10) {
            poäng += kort.valör;
        } else {
            poäng += 10;
        }
    }

    public void renderKort(Graphics g, ImageObserver observer, int x) {
        if (!hand.isEmpty()) {
            for (int i = 0; i < hand.size(); i++) {
                hand.get(i).render(g, observer, x + i * 50, 440 + i * 10);//Offset för korten
            }
        }

    }

}
