
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Panel extends javax.swing.JPanel {

    public Panel() {
        initComponents();
        laddaFil();
        visaHighscoreSorted();
    }

    //Fil hantering
    String path = "Highscore.txt";
    File f = new File(path);
    Map<String, Integer> highscoreMap = new HashMap<>();

    //Spel funktion
    int tempX;
    Dealer dealer = new Dealer();
    boolean spelStartat = false;

    //Färg
    Color bakgrund = new Color(32, 77, 44); //Grön färg
    Color textColor = new Color(160, 160, 160); //Grå färg

    //Kortlek
    Kortlek lek = new Kortlek();

    //Spelare funktioner
    ArrayList<Spelare> spelareLista = new ArrayList<>();
    final int MAX_SPELARE = 3;
    int aktivSpelare = 0;
    int spelareKvar = 0;
    int spelareBettar = 0;
    int bet = 0;

    //Font
    Font font = new Font("Comic Sans MS", Font.BOLD, 24);

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Render spelyta
        g.setColor(bakgrund);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(textColor);
        g.setFont(font);
        g.drawString("Dealer, poäng: " + dealer.poäng, 295, 20);
        Kort kortHögen = new Kort(0, 0);
        for (int i = 0; i < 5; i++) {
            kortHögen.render(g, this, 200 + i * 2, 50 + i * 2);
        }

        //Spelare render
        if (!spelareLista.isEmpty()) {
            for (int i = 0; i < spelareLista.size(); i++) {
                if (aktivSpelare == i) {
                    g.setColor(Color.YELLOW);
                } else {
                    g.setColor(textColor);
                }
                tempX = 800 / spelareLista.size() * i + 100;
                g.drawString(spelareLista.get(i).namn, tempX, 400);
                g.drawString(spelareLista.get(i).pengar + "kr Poäng: " + spelareLista.get(i).poäng, tempX, 375);
                spelareLista.get(i).renderKort(g, this, tempX);
            }
        }

        //Spel startat
        if (spelStartat) {
            dealer.renderKort(g, this, 315);
            if (spelareKvar == 0) {
                if (dealer.poäng <= 16) {
                    dealer.dealerFåKort(lek.geKort());
                }
                if (dealer.poäng >= 17) {
                    beräknaVinster();
                    btnNyRunda.setEnabled(true);
                }
                btnHit.setEnabled(false);
                btnStand.setEnabled(false);
                btnDouble.setEnabled(false);
            }
        }
    }

    public final void laddaFil() {
        highscoreMap.clear();
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("—");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    int score;
                    try {
                        score = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    highscoreMap.put(name, score);
                }
            }
        } catch (FileNotFoundException e) {
            txaHighscore.setText("Filen kunde inte laddas in: " + e.getMessage());
        }
    }

    public void skrivTillFil() {
        try (FileWriter wr = new FileWriter(f)) {
            for (Map.Entry<String, Integer> entry : highscoreMap.entrySet()) {
                wr.write(entry.getKey() + " — " + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            txaHighscore.setText("Fel: " + e.getMessage());
        }
    }

    public void uppdateraHighscore(String namn, int pengar) {
        Integer current = highscoreMap.get(namn);
        if (current == null || pengar > current) {
            highscoreMap.put(namn, pengar);
            skrivTillFil();
        }
    }

    public final void visaHighscore() {
        txaHighscore.setText("");
        for (Map.Entry<String, Integer> entry : highscoreMap.entrySet()) {
            txaHighscore.append(entry.getKey() + " — " + entry.getValue() + "\n");
        }
    }

    public int sequentialSearch(ArrayList<Spelare> spelareLista, String namn) {
        for (int i = 0; i < spelareLista.size(); i++) {
            if (spelareLista.get(i).namn.equalsIgnoreCase(namn)) {
                return i;
            }
        }
        return -1;
    }

    public final void visaHighscoreSorted() {
        //Sortera hashmapens value i descending order och gör till en linkedhashmap för att bevara den nya
        LinkedHashMap<String, Integer> sortedHighscores = highscoreMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        txaHighscore.setText("");
        for (Map.Entry<String, Integer> entry : sortedHighscores.entrySet()) {
            txaHighscore.append(entry.getKey() + " — " + entry.getValue() + "\n");
        }
    }

    public void beräknaVinster() {
        for (Spelare s : spelareLista) {
            //Spelaren är tjock
            if (s.poäng > 21) {
                // Inget händer, pengar är redan borta.
            } // Om dealern är tjock och spelaren inte är det
            else if (dealer.poäng > 21) {
                s.pengar += s.bet * 2;
                uppdateraHighscore(s.namn, s.pengar);
            } // Oavgjort
            else if (s.poäng == dealer.poäng) {
                s.pengar += s.bet;
            } // Spelare vinst
            else if (s.poäng > dealer.poäng) {
                s.pengar += s.bet * 2;
                uppdateraHighscore(s.namn, s.pengar);
            } else {
                // Pengar redan borta, dealer vann
            }
            s.bet = 0;
        }
        bet = 0;
        visaHighscoreSorted();
        repaint();
    }

    public void knapparInvert(int spelareKvar, boolean state) {
        //Bet knappar
        cbnSpelare1.setEnabled(state);
        btnBet.setEnabled(state);
        sldBet.setEnabled(state);
        btnStarta.setEnabled(state);

        //Spelval knappar
        btnHit.setEnabled(!state);
        btnStand.setEnabled(!state);
        btnDouble.setEnabled(!state);

        //Bet knappar
        if (spelareKvar < 3 && spelareKvar > 1) {
            cbnSpelare2.setEnabled(state);
        }
        if (spelareKvar == 3) {
            cbnSpelare2.setEnabled(state);
            cbnSpelare3.setEnabled(state);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnLäggTillSpelare = new javax.swing.JButton();
        txfSpelareNamn = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btnHit = new javax.swing.JButton();
        btnStand = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txaHighscore = new javax.swing.JTextArea();
        btnStarta = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        btnNyRunda = new javax.swing.JButton();
        cbnSpelare1 = new javax.swing.JCheckBox();
        cbnSpelare2 = new javax.swing.JCheckBox();
        cbnSpelare3 = new javax.swing.JCheckBox();
        sldBet = new javax.swing.JSlider();
        txfBetMängd = new javax.swing.JTextField();
        btnBet = new javax.swing.JButton();
        btnDouble = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(1000, 700));

        btnLäggTillSpelare.setText("Lägg till spelare");
        btnLäggTillSpelare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLäggTillSpelareActionPerformed(evt);
            }
        });

        jLabel1.setText("Namn på spelare:");

        btnHit.setText("Hit");
        btnHit.setEnabled(false);
        btnHit.setPreferredSize(new java.awt.Dimension(112, 23));
        btnHit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHitActionPerformed(evt);
            }
        });

        btnStand.setText("Stand");
        btnStand.setEnabled(false);
        btnStand.setPreferredSize(new java.awt.Dimension(112, 23));
        btnStand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStandActionPerformed(evt);
            }
        });

        txaHighscore.setEditable(false);
        txaHighscore.setColumns(13);
        txaHighscore.setRows(5);
        txaHighscore.setFocusable(false);
        jScrollPane1.setViewportView(txaHighscore);

        btnStarta.setText("Starta Spel");
        btnStarta.setEnabled(false);
        btnStarta.setPreferredSize(new java.awt.Dimension(112, 23));
        btnStarta.setRequestFocusEnabled(false);
        btnStarta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartaActionPerformed(evt);
            }
        });

        jLabel2.setText("Highscore");

        btnNyRunda.setText("Ny Runda");
        btnNyRunda.setEnabled(false);
        btnNyRunda.setPreferredSize(new java.awt.Dimension(112, 23));
        btnNyRunda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNyRundaActionPerformed(evt);
            }
        });

        cbnSpelare1.setText("Spelare 1");
        cbnSpelare1.setEnabled(false);
        cbnSpelare1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbnSpelare1ActionPerformed(evt);
            }
        });

        cbnSpelare2.setText("Spelare 2");
        cbnSpelare2.setEnabled(false);
        cbnSpelare2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbnSpelare2ActionPerformed(evt);
            }
        });

        cbnSpelare3.setText("Spelare 3");
        cbnSpelare3.setEnabled(false);
        cbnSpelare3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbnSpelare3ActionPerformed(evt);
            }
        });

        sldBet.setEnabled(false);
        sldBet.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldBetStateChanged(evt);
            }
        });

        txfBetMängd.setEditable(false);
        txfBetMängd.setText("Bet");
        txfBetMängd.setFocusable(false);

        btnBet.setText("Bet");
        btnBet.setEnabled(false);
        btnBet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBetActionPerformed(evt);
            }
        });

        btnDouble.setText("Double");
        btnDouble.setEnabled(false);
        btnDouble.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDoubleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(554, 554, 554)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sldBet, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(cbnSpelare3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbnSpelare2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbnSpelare1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txfBetMängd)
                    .addComponent(btnBet, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1)
                    .addComponent(txfSpelareNamn, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnLäggTillSpelare, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                    .addComponent(btnStarta, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnHit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnStand, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNyRunda, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDouble, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(63, 63, 63))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txfSpelareNamn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnLäggTillSpelare)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnStarta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnHit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDouble)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnStand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(9, 9, 9)
                                .addComponent(btnNyRunda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbnSpelare1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbnSpelare2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbnSpelare3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sldBet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txfBetMängd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnBet)))))
                .addGap(210, 210, 210))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnLäggTillSpelareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLäggTillSpelareActionPerformed

        //Lägg till ny spelare
        String namn = txfSpelareNamn.getText();

        if (namn.isEmpty()) {
            txfSpelareNamn.setText("Ange ett namn!");
        } else {
            //Kolla om namnet redan används
            if (sequentialSearch(spelareLista, namn) == -1) {
                Spelare spelare = new Spelare(namn);
                txfSpelareNamn.setText("");
                spelareLista.add(spelare);
                spelareKvar++;
                knapparInvert(spelareKvar, true);
            } else {
                txfSpelareNamn.setText("Namnet finns redan!");
            }
        }

        //Limita spelare mängd
        if (spelareLista.size() >= MAX_SPELARE) {
            btnLäggTillSpelare.setEnabled(false);
        }

        repaint();


    }//GEN-LAST:event_btnLäggTillSpelareActionPerformed

    private void btnHitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHitActionPerformed
        spelareLista.get(aktivSpelare).fårKort(lek.geKort());
        if (spelareLista.get(aktivSpelare).poäng >= 21) {
            aktivSpelare++;
            spelareKvar--;
        }
        repaint();
    }//GEN-LAST:event_btnHitActionPerformed

    private void btnStandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStandActionPerformed
        aktivSpelare++;
        spelareKvar--;
        if (spelareKvar == 0) {
            btnHit.setEnabled(false);
            btnStand.setEnabled(false);
            btnDouble.setEnabled(false);
        }
        repaint();
    }//GEN-LAST:event_btnStandActionPerformed

    private void btnStartaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartaActionPerformed
        //Blanda lek och ge 2 kort till varje spelare (Starthanden)
        lek.blanda();
        dealer.dealerFåKort(lek.geKort());
        for (int i = 0; i < spelareLista.size(); i++) {
            for (int j = 0; j < 2; j++) {
                spelareLista.get(i).fårKort(lek.geKort());
            }
        }

        //Stäng av knappar
        knapparInvert(spelareKvar, false);
        btnLäggTillSpelare.setEnabled(false);
        txfSpelareNamn.setFocusable(false);
        txfSpelareNamn.setEditable(false);
        spelStartat = true;

        repaint();
    }//GEN-LAST:event_btnStartaActionPerformed

    private void btnNyRundaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNyRundaActionPerformed
        //Återställ dealers hand och poäng
        dealer.hand.clear();
        dealer.poäng = 0;

        //Återställ alla spelares händer och poäng
        for (Spelare s : spelareLista) {
            s.hand.clear();
            s.poäng = 0;
        }

        //Nytt värde på slider
        sldBet.setMaximum(spelareLista.get(spelareBettar).pengar);

        //Fixar ny lek
        lek.nyLek();
        lek.blanda();

        //Återställ värdena och knappar
        spelareKvar = spelareLista.size();
        aktivSpelare = 0;
        spelStartat = false;
        btnNyRunda.setEnabled(false);
        knapparInvert(spelareKvar, true);
        repaint();
    }//GEN-LAST:event_btnNyRundaActionPerformed

    private void cbnSpelare1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbnSpelare1ActionPerformed
        cbnSpelare2.setSelected(false);
        cbnSpelare3.setSelected(false);
        spelareBettar = 0;
        sldBet.setMaximum(spelareLista.get(0).pengar);
        cbnSpelare1.setText("Spelare: " + spelareLista.get(0).namn);
        repaint();
    }//GEN-LAST:event_cbnSpelare1ActionPerformed

    private void cbnSpelare2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbnSpelare2ActionPerformed
        cbnSpelare1.setSelected(false);
        cbnSpelare3.setSelected(false);
        spelareBettar = 1;
        sldBet.setMaximum(spelareLista.get(1).pengar);
        cbnSpelare2.setText("Spelare: " + spelareLista.get(1).namn);
        repaint();
    }//GEN-LAST:event_cbnSpelare2ActionPerformed

    private void cbnSpelare3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbnSpelare3ActionPerformed
        cbnSpelare1.setSelected(false);
        cbnSpelare2.setSelected(false);
        spelareBettar = 2;
        sldBet.setMaximum(spelareLista.get(2).pengar);
        cbnSpelare3.setText("Spelare: " + spelareLista.get(2).namn);
        repaint();
    }//GEN-LAST:event_cbnSpelare3ActionPerformed

    private void sldBetStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldBetStateChanged
        txfBetMängd.setText("Bet: " + sldBet.getValue());
        bet = sldBet.getValue();
        repaint();
    }//GEN-LAST:event_sldBetStateChanged

    private void btnBetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBetActionPerformed
        if (bet > 0 && bet <= spelareLista.get(spelareBettar).pengar) {
            spelareLista.get(spelareBettar).pengar -= bet;
            spelareLista.get(spelareBettar).bet = bet;
            txfBetMängd.setText("Du bettade " + bet);
        }
        repaint();
    }//GEN-LAST:event_btnBetActionPerformed

    private void btnDoubleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDoubleActionPerformed
        if (spelareLista.get(aktivSpelare).pengar + spelareLista.get(aktivSpelare).bet >= spelareLista.get(aktivSpelare).bet * 2) {
            spelareLista.get(aktivSpelare).fårKort(lek.geKort());
            spelareLista.get(aktivSpelare).pengar = spelareLista.get(aktivSpelare).pengar - spelareLista.get(aktivSpelare).bet;
            spelareLista.get(aktivSpelare).bet = spelareLista.get(aktivSpelare).bet * 2;
            aktivSpelare++;
            spelareKvar--;
        } else {
            txfBetMängd.setText("Inte till räckligt med pengar för att dubbla");
        }
        repaint();
    }//GEN-LAST:event_btnDoubleActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBet;
    private javax.swing.JButton btnDouble;
    private javax.swing.JButton btnHit;
    private javax.swing.JButton btnLäggTillSpelare;
    private javax.swing.JButton btnNyRunda;
    private javax.swing.JButton btnStand;
    private javax.swing.JButton btnStarta;
    private javax.swing.JCheckBox cbnSpelare1;
    private javax.swing.JCheckBox cbnSpelare2;
    private javax.swing.JCheckBox cbnSpelare3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider sldBet;
    private javax.swing.JTextArea txaHighscore;
    private javax.swing.JTextField txfBetMängd;
    private javax.swing.JTextField txfSpelareNamn;
    // End of variables declaration//GEN-END:variables
}
