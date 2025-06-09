import java.util.Random;

public class Kortlek {

    Kort[] lek = new Kort[52];
    int antalKort = 0;

    public Kortlek() {
        for (int färg = 1; färg <= 4; färg++) {
            for (int valör = 1; valör <= 13; valör++) {
                lek[antalKort++] = new Kort(färg, valör);
            }
        }
    }

    public void nyLek() {
        //Ta bort alla nuvarnde kort i leken
        for (int i = 0; i < lek.length; i++) {
          lek[i] = null;
        }
        antalKort = 0;
        //Lägg till nya kort
        for (int färg = 1; färg <= 4; färg++) {
            for (int valör = 1; valör <= 13; valör++) {
                lek[antalKort++] = new Kort(färg, valör);
            }
        }
    }

    public void blanda() {
        Random r = new Random();
        for (int i = antalKort - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);

            Kort temp = lek[i];
            lek[i] = lek[j];
            lek[j] = temp;
        }
    }

    public Kort geKort() {
        Kort k = lek[antalKort - 1];
        antalKort--;
        return k;
    }

}
